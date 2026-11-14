import { useEffect, useState } from 'react'
import { catalogo } from '../api/servicios'

const profesionales = new Map<string, string>()
const centros = new Map<string, string>()
let especialidades: Record<string, string> | null = null

interface Refs { profesionalId: string; centroId: string; especialidadId?: string }

/**
 * Las citas solo traen ids. Se resuelven a nombres con catalog (profesional, sus centros y las especialidades) y se guardan en memoria.
 * Si un nombre no se puede resolver se muestra el id corto: no se inventa nada.
 */
export function useNombres(refs: Refs[]) {
  const [, forzar] = useState(0)
  useEffect(() => {
    if (especialidades) return
    catalogo.especialidades().then((l) => { especialidades = Object.fromEntries(l.map((e) => [e.id, e.nombre])); forzar((n) => n + 1) }).catch(() => {})
  }, [])
  useEffect(() => {
    const ids = [...new Set(refs.map((r) => r.profesionalId))].filter((id) => !profesionales.has(id))
    if (!ids.length) return
    let vivo = true
    void Promise.all(ids.map(async (id) => {
      try {
        const d = await catalogo.profesional(id)
        profesionales.set(id, `${d.profesional.nombres} ${d.profesional.apellidos}`)
        for (const c of await catalogo.centrosDe(id)) centros.set(c.id, c.nombre)
      } catch { /* queda el id corto */ }
    })).then(() => { if (vivo) forzar((n) => n + 1) })
    return () => { vivo = false }
  }, [refs])
  return {
    profesional: (id: string) => profesionales.get(id) ?? `Profesional ${id.slice(0, 8)}`,
    centro: (id: string) => centros.get(id) ?? `Centro ${id.slice(0, 8)}`,
    especialidad: (id?: string) => (id && especialidades?.[id]) || 'Especialidad',
  }
}
