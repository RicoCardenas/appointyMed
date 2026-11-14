import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { auth } from '../api/servicios'
import { configurarSesion } from '../api/http'
import type { LoginRespuesta, Rol } from '../api/tipos'

export interface DatosSesion { token: string; usuarioId: string; rol: Rol; expiraEn: number }

interface Contexto {
  sesion: DatosSesion | null
  aviso: string | null
  iniciar: (r: LoginRespuesta) => void
  cerrar: (aviso?: string) => void
  limpiarAviso: () => void
}

const CLAVE = 'appointy.sesion'
const Ctx = createContext<Contexto | null>(null)

/** La sesion vive en sessionStorage (se borra al cerrar la pestana): el JWT dura 15 minutos y no hay refresh token. */
export function leerSesionGuardada(): DatosSesion | null {
  try {
    const crudo = sessionStorage.getItem(CLAVE)
    if (!crudo) return null
    const s = JSON.parse(crudo) as DatosSesion
    return s.expiraEn > Date.now() ? s : null
  } catch {
    return null
  }
}

export function SesionProvider({ children }: { children: ReactNode }) {
  const [sesion, setSesion] = useState<DatosSesion | null>(() => leerSesionGuardada())
  const [aviso, setAviso] = useState<string | null>(null)

  const cerrar = useCallback((mensaje?: string) => {
    setSesion((actual) => {
      if (actual) void auth.logout().catch(() => {})
      return null
    })
    try { sessionStorage.removeItem(CLAVE) } catch { /* sin almacenamiento */ }
    setAviso(mensaje ?? null)
  }, [])

  const iniciar = useCallback((r: LoginRespuesta) => {
    const datos: DatosSesion = { token: r.accessToken, usuarioId: r.usuarioId, rol: r.rol, expiraEn: new Date(r.expiraEn).getTime() }
    try { sessionStorage.setItem(CLAVE, JSON.stringify(datos)) } catch { /* sin almacenamiento */ }
    setAviso(null)
    setSesion(datos)
  }, [])

  useEffect(() => {
    configurarSesion(() => sesion?.token ?? null, () => {
      try { sessionStorage.removeItem(CLAVE) } catch { /* sin almacenamiento */ }
      setSesion(null)
      setAviso('Tu sesion expiro. Inicia sesion de nuevo para continuar.')
    })
  }, [sesion])

  useEffect(() => {
    if (!sesion) return
    const resta = sesion.expiraEn - Date.now()
    const t = setTimeout(() => cerrar('Tu sesion expiro. Inicia sesion de nuevo para continuar.'), Math.max(resta, 0))
    return () => clearTimeout(t)
  }, [sesion, cerrar])

  const valor = useMemo(() => ({ sesion, aviso, iniciar, cerrar, limpiarAviso: () => setAviso(null) }), [sesion, aviso, iniciar, cerrar])
  return <Ctx.Provider value={valor}>{children}</Ctx.Provider>
}

export function useSesion(): Contexto {
  const c = useContext(Ctx)
  if (!c) throw new Error('useSesion fuera de SesionProvider')
  return c
}

export const INICIO_POR_ROL: Record<Rol, string> = { PACIENTE: '/reservar', PROFESIONAL: '/agenda', ADMINISTRATIVO: '/admin/citas' }
