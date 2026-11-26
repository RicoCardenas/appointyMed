export type Tema = 'claro' | 'oscuro'

const CLAVE = 'appointy.tema'

/** Tema en uso: el elegido por la persona o, si no ha elegido, el del sistema. */
export function temaActual(): Tema {
  const elegido = document.documentElement.dataset.tema
  if (elegido === 'claro' || elegido === 'oscuro') return elegido
  return window.matchMedia?.('(prefers-color-scheme: dark)').matches ? 'oscuro' : 'claro'
}

export function aplicarTema(tema: Tema) {
  document.documentElement.dataset.tema = tema
  try { localStorage.setItem(CLAVE, tema) } catch { /* sin almacenamiento: el tema dura la visita */ }
}

/** Recupera la eleccion guardada antes de pintar. Solo guarda la preferencia visual, nada clinico. */
export function iniciarTema() {
  try {
    const guardado = localStorage.getItem(CLAVE)
    if (guardado === 'claro' || guardado === 'oscuro') document.documentElement.dataset.tema = guardado
  } catch { /* sin almacenamiento: se usa el tema del sistema */ }
}
