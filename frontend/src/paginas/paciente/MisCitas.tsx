import { useMemo, useState } from 'react'
import { citas } from '../../api/servicios'
import type { Cita, Franja } from '../../api/tipos'
import { useToast } from '../../componentes/Toasts'
import { Aviso, Boton, Confirmar, ErrorAviso, Esqueleto, Insignia, Pagina, Vacio } from '../../componentes/ui'
import { ESTADOS_CITA, fechaLarga, hoyIso, mensajeDeError, sumarDias } from '../../util/formato'
import { useAsync } from '../../util/useAsync'
import { useNombres } from '../../util/useNombres'

const TONO: Record<string, 'acento' | 'neutro' | 'exito' | 'aviso'> = { CONFIRMADA: 'acento', CANCELADA: 'neutro', COMPLETADA: 'exito', NO_ASISTIDA: 'aviso' }

export function MisCitas() {
  const [periodo, setPeriodo] = useState<'PROXIMAS' | 'PASADAS'>('PROXIMAS')
  const lista = useAsync(() => citas.mias(periodo), [periodo])
  const nombres = useNombres(useMemo(() => lista.datos ?? [], [lista.datos]))
  const [aCancelar, setACancelar] = useState<Cita | null>(null)
  const [cancelando, setCancelando] = useState(false)
  const [aMover, setAMover] = useState<Cita | null>(null)
  const avisar = useToast()

  async function cancelar() {
    if (!aCancelar) return
    setCancelando(true)
    try {
      await citas.cancelar(aCancelar.id)
      avisar('La cita fue cancelada.')
      setACancelar(null)
      lista.recargar()
    } catch (e) {
      avisar(mensajeDeError(e), 'error')
      setACancelar(null)
    } finally {
      setCancelando(false)
    }
  }

  return (
    <Pagina titulo="Mis citas" acciones={
      <div className="acciones" role="group" aria-label="Periodo">
        <Boton variante={periodo === 'PROXIMAS' ? 'primario' : 'secundario'} onClick={() => setPeriodo('PROXIMAS')} aria-pressed={periodo === 'PROXIMAS'}>Proximas</Boton>
        <Boton variante={periodo === 'PASADAS' ? 'primario' : 'secundario'} onClick={() => setPeriodo('PASADAS')} aria-pressed={periodo === 'PASADAS'}>Pasadas</Boton>
      </div>
    }>
      {lista.cargando ? <Esqueleto /> : lista.error != null ? <ErrorAviso error={lista.error} alReintentar={lista.recargar} /> : !lista.datos?.length ? (
        <Vacio titulo={periodo === 'PROXIMAS' ? 'No tienes citas proximas' : 'Aun no tienes citas pasadas'}>
          {periodo === 'PROXIMAS' ? 'Cuando reserves una, la veras aqui con su fecha, hora y lugar.' : 'Aqui quedara el registro de tus citas anteriores.'}
        </Vacio>
      ) : (
        <ul className="lista" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
          {lista.datos.map((c) => (
            <li key={c.id} className="item">
              <div>
                <div className="item-titulo">{fechaLarga(c.fecha)} · {c.horaInicio}</div>
                <div className="item-sub">{nombres.profesional(c.profesionalId)} · {nombres.especialidad(c.especialidadId)} · {nombres.centro(c.centroId)}</div>
              </div>
              <div className="acciones">
                <Insignia tono={TONO[c.estado]}>{ESTADOS_CITA[c.estado]}</Insignia>
                {c.estado === 'CONFIRMADA' && (
                  <>
                    <Boton variante="secundario" className="btn-chico" onClick={() => setAMover(aMover?.id === c.id ? null : c)}>Reprogramar</Boton>
                    <Boton variante="fantasma" className="btn-chico" onClick={() => setACancelar(c)}>Cancelar</Boton>
                  </>
                )}
              </div>
              {aMover?.id === c.id && <Reprogramar cita={c} alCerrar={() => setAMover(null)} alListo={() => { setAMover(null); lista.recargar() }} />}
            </li>
          ))}
        </ul>
      )}
      <Confirmar abierto={!!aCancelar} titulo="Cancelar esta cita?" peligro cargando={cancelando} textoAccion="Si, cancelar" alConfirmar={cancelar} alCancelar={() => setACancelar(null)}
        texto={aCancelar && <>La cita del {fechaLarga(aCancelar.fecha)} a las {aCancelar.horaInicio} se cancelara y la hora quedara libre para otra persona. La cancelacion tiene un plazo minimo: si ya paso, el sistema te lo dira.</>} />
    </Pagina>
  )
}

function Reprogramar({ cita, alCerrar, alListo }: { cita: Cita; alCerrar: () => void; alListo: () => void }) {
  const [fecha, setFecha] = useState(sumarDias(hoyIso(), 1))
  const [elegida, setElegida] = useState('')
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const avisar = useToast()
  const franjas = useAsync<Franja[]>(() => citas.disponibilidad(cita.profesionalId, cita.centroId, fecha), [fecha])

  async function mover() {
    setGuardando(true)
    setError(null)
    try {
      await citas.reprogramar(cita.id, elegida)
      avisar('Cita reprogramada.')
      alListo()
    } catch (e) {
      setError(mensajeDeError(e))
      setElegida('')
      franjas.recargar()
    } finally {
      setGuardando(false)
    }
  }

  return (
    <div style={{ flexBasis: '100%', display: 'grid', gap: 12, paddingTop: 12, borderTop: '1px solid var(--line)' }}>
      <p className="item-sub">Elige una nueva hora con el mismo profesional y centro.</p>
      <div style={{ maxWidth: 220 }}>
        <label className="solo-lectores" htmlFor={`f-${cita.id}`}>Nueva fecha</label>
        <input id={`f-${cita.id}`} type="date" min={hoyIso()} value={fecha} onChange={(e) => { setFecha(e.target.value); setElegida('') }} />
      </div>
      {franjas.cargando ? <Esqueleto filas={1} /> : franjas.datos?.length ? (
        <div className="franjas" role="group" aria-label="Horas disponibles">
          {franjas.datos.map((f) => <button key={f.id} type="button" className="franja" aria-pressed={elegida === f.id} onClick={() => setElegida(f.id)}>{f.horaInicio}</button>)}
        </div>
      ) : <p className="item-sub">No hay horas libres ese dia.</p>}
      {error && <Aviso tipo="error">{error}</Aviso>}
      <div className="acciones">
        <Boton disabled={!elegida} cargando={guardando} onClick={mover}>Mover mi cita</Boton>
        <Boton variante="fantasma" onClick={alCerrar}>Cerrar</Boton>
      </div>
    </div>
  )
}
