/** Preferencia de tema: "auto" sigue al sistema; claro y oscuro lo fuerzan. */
export type Preferencia = 'auto' | 'claro' | 'oscuro'

const CLAVE = 'appointy.tema'
const ORDEN: Preferencia[] = ['auto', 'claro', 'oscuro']

export function preferenciaActual(): Preferencia {
  const d = document.documentElement.dataset.tema
  return d === 'claro' || d === 'oscuro' ? d : 'auto'
}

export function siguientePreferencia(p: Preferencia): Preferencia {
  return ORDEN[(ORDEN.indexOf(p) + 1) % ORDEN.length]
}

export function aplicarPreferencia(p: Preferencia) {
  if (p === 'auto') delete document.documentElement.dataset.tema
  else document.documentElement.dataset.tema = p
  try {
    if (p === 'auto') localStorage.removeItem(CLAVE)
    else localStorage.setItem(CLAVE, p)
  } catch { /* sin almacenamiento: la eleccion dura la visita */ }
}

/** Recupera la eleccion guardada antes de pintar. Solo guarda la preferencia visual, nada clinico. */
export function iniciarTema() {
  try {
    const guardado = localStorage.getItem(CLAVE)
    if (guardado === 'claro' || guardado === 'oscuro') document.documentElement.dataset.tema = guardado
  } catch { /* sin almacenamiento: se usa el tema del sistema */ }
}
