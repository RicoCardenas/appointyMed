import { useCallback, useEffect, useRef, useState } from 'react'

export interface Asincrono<T> { datos: T | null; cargando: boolean; error: unknown; recargar: () => void }

/** Carga de datos con estado de carga/error. Ignora respuestas de llamadas anteriores si cambian las dependencias. */
export function useAsync<T>(fn: () => Promise<T>, deps: unknown[]): Asincrono<T> {
  const [datos, setDatos] = useState<T | null>(null)
  const [cargando, setCargando] = useState(true)
  const [error, setError] = useState<unknown>(null)
  const [version, setVersion] = useState(0)
  const ultima = useRef(0)

  useEffect(() => {
    const id = ++ultima.current
    setCargando(true)
    setError(null)
    fn().then(
      (d) => { if (id === ultima.current) { setDatos(d); setCargando(false) } },
      (e) => { if (id === ultima.current) { setError(e); setCargando(false) } },
    )
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...deps, version])

  const recargar = useCallback(() => setVersion((v) => v + 1), [])
  return { datos, cargando, error, recargar }
}
