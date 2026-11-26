import { Moon, Sun } from 'lucide-react'
import { useState } from 'react'
import { aplicarTema, temaActual, type Tema } from '../util/tema'

/** Alterna entre modo claro y oscuro. El icono muestra a donde se cambia. */
export function TemaBoton() {
  const [tema, setTema] = useState<Tema>(temaActual)
  const destino: Tema = tema === 'oscuro' ? 'claro' : 'oscuro'
  function cambiar() {
    aplicarTema(destino)
    setTema(destino)
  }
  return (
    <button type="button" className="btn btn-fantasma btn-icono" onClick={cambiar} aria-label={`Cambiar a modo ${destino}`} title={`Modo ${destino}`}>
      {tema === 'oscuro' ? <Sun size={18} aria-hidden /> : <Moon size={18} aria-hidden />}
    </button>
  )
}
