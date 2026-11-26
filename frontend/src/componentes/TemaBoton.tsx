import { Monitor, Moon, Sun } from 'lucide-react'
import { useState } from 'react'
import { aplicarPreferencia, preferenciaActual, siguientePreferencia, type Preferencia } from '../util/tema'

const NOMBRE: Record<Preferencia, string> = { auto: 'automatico', claro: 'claro', oscuro: 'oscuro' }
const ICONO = { auto: Monitor, claro: Sun, oscuro: Moon }

/** Recorre automatico, claro y oscuro. El icono muestra la preferencia en uso. */
export function TemaBoton() {
  const [pref, setPref] = useState<Preferencia>(preferenciaActual)
  const siguiente = siguientePreferencia(pref)
  const Icono = ICONO[pref]
  function cambiar() {
    aplicarPreferencia(siguiente)
    setPref(siguiente)
  }
  const texto = `Tema ${NOMBRE[pref]}. Cambiar a ${NOMBRE[siguiente]}`
  return (
    <button type="button" className="btn btn-fantasma btn-icono" onClick={cambiar} aria-label={texto} title={texto}>
      <Icono size={18} aria-hidden />
    </button>
  )
}
