import { useEffect, useMemo, useState } from 'react'
import { auth } from '../api/servicios'

const nombres = new Map<string, string>()

/**
 * Nombres de pacientes para profesionales y administrativos (auth devuelve solo nombre y apellido). Se piden en lote y se guardan en
 * memoria. Si no se puede resolver, se muestra "Paciente" con el id corto: no se inventa ni se bloquea la pantalla.
 */
export function useNombresPacientes(ids: string[]): (id: string) => string {
  const [, forzar] = useState(0)
  const clave = useMemo(() => [...new Set(ids)].filter((id) => !nombres.has(id)).sort().join(','), [ids])
  useEffect(() => {
    if (!clave) return
    let vivo = true
    const pendientes = clave.split(',')
    const lotes: string[][] = []
    for (let i = 0; i < pendientes.length; i += 50) lotes.push(pendientes.slice(i, i + 50))
    void Promise.all(lotes.map((l) => auth.nombresPacientes(l).then(
      (r) => r.forEach((n) => nombres.set(n.usuarioId, `${n.nombres} ${n.apellidos}`)),
      () => {},
    ))).then(() => { if (vivo) forzar((n) => n + 1) })
    return () => { vivo = false }
  }, [clave])
  return (id: string) => nombres.get(id) ?? `Paciente ${id.slice(0, 8)}`
}
