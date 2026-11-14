/** Fechas de calendario (yyyy-MM-dd) sin zona: se interpretan como fecha local para que no se corran un dia. */
export function aFechaLocal(iso: string): Date {
  const [a, m, d] = iso.split('-').map(Number)
  return new Date(a, m - 1, d)
}

export function fechaLarga(iso: string | null | undefined): string {
  if (!iso) return 'sin fecha'
  return new Intl.DateTimeFormat('es-CO', { weekday: 'short', day: 'numeric', month: 'short', year: 'numeric' }).format(aFechaLocal(iso))
}

export function fechaCorta(iso: string): string {
  return new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short' }).format(aFechaLocal(iso))
}

/** Instantes UTC del servidor a hora de la clinica (Bogota). */
export function instante(iso: string | null | undefined): string {
  if (!iso) return 'sin registro'
  return new Intl.DateTimeFormat('es-CO', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'America/Bogota' }).format(new Date(iso))
}

export function hoyIso(): string {
  const d = new Date()
  const mes = String(d.getMonth() + 1).padStart(2, '0')
  const dia = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${mes}-${dia}`
}

export function sumarDias(iso: string, dias: number): string {
  const d = aFechaLocal(iso)
  d.setDate(d.getDate() + dias)
  const mes = String(d.getMonth() + 1).padStart(2, '0')
  const dia = String(d.getDate()).padStart(2, '0')
  return `${d.getFullYear()}-${mes}-${dia}`
}

export const DIAS = ['', 'Lunes', 'Martes', 'Miercoles', 'Jueves', 'Viernes', 'Sabado', 'Domingo']

export function idCorto(id: string): string {
  return id.slice(0, 8)
}

export const ESTADOS_CITA: Record<string, string> = {
  CONFIRMADA: 'Confirmada', CANCELADA: 'Cancelada', COMPLETADA: 'Atendida', NO_ASISTIDA: 'No asistio',
}

/** Mensajes que nombran el problema y la salida, por codigo del backend. El resto usa el mensaje del servidor. */
const MENSAJES: Record<string, string> = {
  SIN_CONEXION: 'No se pudo conectar con el servidor. Revisa tu conexion e intenta de nuevo.',
  CREDENCIALES_INVALIDAS: 'El correo o la clave no coinciden. Revisalos e intenta de nuevo.',
  FRANJA_NO_DISPONIBLE: 'Esa franja ya no esta disponible. Elige otra de la lista.',
  CITA_CAMBIO: 'La cita cambio mientras la procesabamos. Recarga e intenta de nuevo.',
  CORREO_YA_REGISTRADO: 'Ese correo ya tiene una cuenta. Inicia sesion o usa otro correo.',
  CEDULA_YA_REGISTRADA: 'Esa cedula ya esta registrada. Si es tuya, inicia sesion.',
  CLAVE_DEBIL: 'La clave debe tener al menos 8 caracteres con una letra y un numero.',
  CLAVE_ACTUAL_INCORRECTA: 'La clave actual no es correcta.',
  CLAVE_IGUAL: 'La clave nueva debe ser distinta de la actual.',
  NO_AUTODESACTIVAR: 'No puedes desactivar tu propia cuenta.',
  ATENCION_YA_REGISTRADA: 'Esta cita ya tiene una atencion registrada. Para cambiarla, crea una correccion.',
  SIN_RELACION_CON_PACIENTE: 'Solo ves la historia de pacientes con los que tienes o tuviste citas.',
  FESTIVOS_NO_DISPONIBLES: 'El servicio de festivos no responde ahora. Intenta de nuevo en un momento.',
  CIE10_NO_DISPONIBLE: 'El catalogo CIE-10 no responde ahora. Puedes escribir el diagnostico a mano.',
}

export function mensajeDeError(e: unknown): string {
  const x = e as { codigo?: string; message?: string; detalle?: string[] }
  if (x?.codigo && MENSAJES[x.codigo]) return MENSAJES[x.codigo]
  const detalle = x?.detalle?.length ? ` (${x.detalle.join('; ')})` : ''
  return `${x?.message ?? 'Ocurrio un error inesperado'}${detalle}`
}
