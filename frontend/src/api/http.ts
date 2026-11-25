/** Cliente HTTP comun: base por servicio, JWT, id de correlacion y errores uniformes (codigo, mensaje, detalle). */

export type Servicio = 'auth' | 'catalog' | 'appointment' | 'clinical' | 'notification'

export class ApiError extends Error {
  constructor(
    public estado: number,
    public codigo: string,
    mensaje: string,
    public detalle: string[] = [],
  ) {
    super(mensaje)
  }
}

let leerToken: () => string | null = () => null
let alExpirar: () => void = () => {}

/** La sesion registra aqui como obtener el token y que hacer ante un 401 con sesion abierta. */
export function configurarSesion(token: () => string | null, expirar: () => void) {
  leerToken = token
  alExpirar = expirar
}

type Query = Record<string, string | number | boolean | undefined | null>

export interface Opciones {
  metodo?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  cuerpo?: unknown
  query?: Query
  signal?: AbortSignal
}

export function construirQuery(query?: Query): string {
  if (!query) return ''
  const p = new URLSearchParams()
  for (const [k, v] of Object.entries(query)) {
    if (v !== undefined && v !== null && v !== '') p.set(k, String(v))
  }
  const s = p.toString()
  return s ? `?${s}` : ''
}

export async function llamar<T>(servicio: Servicio, ruta: string, o: Opciones = {}): Promise<T> {
  const headers: Record<string, string> = { 'X-Correlation-Id': crypto.randomUUID() }
  const token = leerToken()
  if (token) headers.Authorization = `Bearer ${token}`
  if (o.cuerpo !== undefined) headers['Content-Type'] = 'application/json'
  let respuesta: Response
  try {
    respuesta = await fetch(`/svc/${servicio}/api/v1${ruta}${construirQuery(o.query)}`, {
      method: o.metodo ?? 'GET',
      headers,
      body: o.cuerpo !== undefined ? JSON.stringify(o.cuerpo) : undefined,
      signal: o.signal,
    })
  } catch (e) {
    if (e instanceof DOMException && e.name === 'AbortError') throw e
    throw new ApiError(0, 'SIN_CONEXION', 'No se pudo conectar con el servidor. Revisa tu conexion e intenta de nuevo.')
  }
  if (respuesta.status === 204) return undefined as T
  let cuerpo: unknown = null
  try {
    cuerpo = await respuesta.json()
  } catch {
    cuerpo = null
  }
  if (!respuesta.ok) {
    const e = (cuerpo ?? {}) as { codigo?: string; mensaje?: string; detalle?: string[] }
    // 502/503/504 sin error uniforme: no responde el backend sino el proxy de la plataforma (servicio dormido o arrancando)
    if ([502, 503, 504].includes(respuesta.status) && !e.codigo) {
      throw new ApiError(respuesta.status, 'SERVICIO_DESPERTANDO', 'El servicio se esta despertando (el plan gratuito lo duerme tras 15 minutos sin uso). Espera un minuto e intenta de nuevo.')
    }
    if (respuesta.status === 401 && token && e.codigo === 'NO_AUTENTICADO') alExpirar()
    throw new ApiError(respuesta.status, e.codigo ?? `HTTP_${respuesta.status}`, e.mensaje ?? 'Ocurrio un error inesperado', e.detalle ?? [])
  }
  return cuerpo as T
}

const SERVICIOS: Servicio[] = ['auth', 'catalog', 'appointment', 'clinical', 'notification']

/** Despierta los servicios dormidos al abrir la app: pide su salud sin esperar la respuesta ni mostrar errores. */
export function calentarServicios(): void {
  for (const s of SERVICIOS) {
    fetch(`/svc/${s}/actuator/health`, { cache: 'no-store' }).catch(() => {})
  }
}

export interface Pagina<T> {
  contenido: T[]
  pagina: number
  tamano: number
  totalElementos: number
  totalPaginas: number
}
