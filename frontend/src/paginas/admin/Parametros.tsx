import { useEffect, useState, type FormEvent } from 'react'
import { catalogo } from '../../api/servicios'
import { useToast } from '../../componentes/Toasts'
import { Aviso, Boton, Campo, ErrorAviso, Esqueleto, Pagina } from '../../componentes/ui'
import { mensajeDeError } from '../../util/formato'
import { useAsync } from '../../util/useAsync'

const ETIQUETAS: Record<string, string> = {
  anticipacion_minima_horas: 'Anticipacion minima (horas)',
  limite_cancelacion_horas: 'Limite para cancelar (horas antes)',
  horizonte_max_dias: 'Horizonte de agendamiento (dias)',
  duracion_franja_min: 'Duracion de la franja (minutos)',
}

export function Parametros() {
  const lista = useAsync(() => catalogo.parametros(), [])
  const [valores, setValores] = useState<Record<string, string>>({})
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const avisar = useToast()
  useEffect(() => { if (lista.datos) setValores(Object.fromEntries(lista.datos.map((p) => [p.clave, p.valor]))) }, [lista.datos])

  async function enviar(e: FormEvent) {
    e.preventDefault(); setGuardando(true); setError(null)
    try {
      const cambiados = Object.fromEntries(Object.entries(valores).filter(([k, v]) => lista.datos?.find((p) => p.clave === k)?.valor !== v))
      if (!Object.keys(cambiados).length) { avisar('No hay cambios que guardar.'); return }
      await catalogo.configurarParametros(cambiados)
      avisar('Parametros guardados.')
      lista.recargar()
    } catch (err) { setError(mensajeDeError(err)) } finally { setGuardando(false) }
  }

  return (
    <Pagina titulo="Parametros" descripcion="Reglas del servicio: anticipacion minima, limite de cancelacion, horizonte de agendamiento y duracion de franja.">
      {lista.cargando ? <Esqueleto /> : lista.error != null ? <ErrorAviso error={lista.error} alReintentar={lista.recargar} /> : (
        <form className="panel form-parametros" onSubmit={enviar}>
          {error && <Aviso tipo="error">{error}</Aviso>}
          {lista.datos?.map((p) => (
            <Campo key={p.clave} id={`par-${p.clave}`} etiqueta={ETIQUETAS[p.clave] ?? p.clave.replaceAll('_', ' ')} inputMode="numeric" ayuda={p.descripcion ?? undefined} value={valores[p.clave] ?? ''} onChange={(e) => setValores((v) => ({ ...v, [p.clave]: e.target.value }))} required />
          ))}
          <div><Boton type="submit" cargando={guardando}>Guardar parametros</Boton></div>
        </form>
      )}
    </Pagina>
  )
}
