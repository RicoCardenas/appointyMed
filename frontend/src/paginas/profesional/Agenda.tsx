import { useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { citas } from '../../api/servicios'
import type { Cita } from '../../api/tipos'
import { AtencionForm } from '../../componentes/AtencionForm'
import { useToast } from '../../componentes/Toasts'
import { Aviso, Boton, ErrorAviso, Esqueleto, Insignia, Pagina, Vacio } from '../../componentes/ui'
import { ESTADOS_CITA, fechaLarga, hoyIso, mensajeDeError, sumarDias } from '../../util/formato'
import { useAsync } from '../../util/useAsync'
import { useNombres } from '../../util/useNombres'
import { useNombresPacientes } from '../../util/useNombresPacientes'

export function Agenda() {
  const [fecha, setFecha] = useState(hoyIso())
  const [todas, setTodas] = useState(false)
  const lista = useAsync(() => citas.agenda(todas ? undefined : fecha), [fecha, todas])
  const nombres = useNombres(useMemo(() => lista.datos ?? [], [lista.datos]))
  const paciente = useNombresPacientes((lista.datos ?? []).map((c) => c.pacienteId))
  const [registrando, setRegistrando] = useState<string | null>(null)
  const [pendienteMarcar, setPendienteMarcar] = useState<Cita | null>(null)
  const [error, setError] = useState<string | null>(null)
  const avisar = useToast()

  async function marcar(c: Cita, estado: 'COMPLETADA' | 'NO_ASISTIDA') {
    setError(null)
    try {
      await citas.marcar(c.id, estado)
      avisar(estado === 'COMPLETADA' ? 'Cita marcada como atendida.' : 'Cita marcada como no asistida.')
      setPendienteMarcar(null)
      lista.recargar()
    } catch (e) {
      setError(mensajeDeError(e))
    }
  }

  return (
    <Pagina titulo="Agenda" descripcion="Tus citas confirmadas. Despues de atender, registra la atencion y marca la cita." acciones={
      <div className="acciones">
        <Boton variante="secundario" onClick={() => { setTodas(false); setFecha(sumarDias(fecha, -1)) }} aria-label="Dia anterior">←</Boton>
        <div style={{ width: 170 }}><label className="solo-lectores" htmlFor="ag-fecha">Fecha</label><input id="ag-fecha" type="date" value={fecha} onChange={(e) => { setTodas(false); setFecha(e.target.value) }} /></div>
        <Boton variante="secundario" onClick={() => { setTodas(false); setFecha(sumarDias(fecha, 1)) }} aria-label="Dia siguiente">→</Boton>
        <Boton variante={todas ? 'primario' : 'secundario'} aria-pressed={todas} onClick={() => setTodas(!todas)}>Desde hoy</Boton>
      </div>
    }>
      {error && <Aviso tipo="error">{error}</Aviso>}
      <p className="paso-resumen" style={{ marginBottom: 12 }}>{todas ? 'Todas tus citas confirmadas desde hoy' : fechaLarga(fecha)}</p>
      {lista.cargando ? <Esqueleto /> : lista.error != null ? <ErrorAviso error={lista.error} alReintentar={lista.recargar} /> : !lista.datos?.length ? (
        <Vacio titulo="Sin citas confirmadas">{todas ? 'No tienes citas confirmadas por venir.' : 'No tienes citas ese dia. Prueba otro dia o mira todas desde hoy.'}</Vacio>
      ) : (
        <ul className="lista" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
          {lista.datos.map((c) => (
            <li key={c.id} className="item">
              <div>
                <div className="item-titulo">{todas && c.fecha ? `${fechaLarga(c.fecha)} · ` : ''}{c.horaInicio} a {c.horaFin}</div>
                <div className="item-sub">
                  <Link to={`/pacientes/${c.pacienteId}`}>{paciente(c.pacienteId)}</Link> · {nombres.especialidad(c.especialidadId)} · {nombres.centro(c.centroId)}
                </div>
              </div>
              <div className="acciones">
                <Insignia tono="acento">{ESTADOS_CITA[c.estado]}</Insignia>
                <Boton className="btn-chico" onClick={() => setRegistrando(registrando === c.id ? null : c.id)}>Registrar atencion</Boton>
                <Boton variante="secundario" className="btn-chico" onClick={() => marcar(c, 'COMPLETADA')}>Atendida</Boton>
                <Boton variante="fantasma" className="btn-chico" onClick={() => setPendienteMarcar(c)}>No asistio</Boton>
              </div>
              {pendienteMarcar?.id === c.id && (
                <Aviso tipo="aviso" accion={<div className="acciones"><Boton variante="peligro" className="btn-chico" onClick={() => marcar(c, 'NO_ASISTIDA')}>Si, no asistio</Boton><Boton variante="fantasma" className="btn-chico" onClick={() => setPendienteMarcar(null)}>Volver</Boton></div>}>
                  Marcar que el paciente no asistio? Esto cierra la cita.
                </Aviso>
              )}
              {registrando === c.id && (
                <AtencionForm citaId={c.id} alCancelar={() => setRegistrando(null)} alGuardar={() => { avisar('Atencion registrada.'); setRegistrando(null) }} />
              )}
            </li>
          ))}
        </ul>
      )}
    </Pagina>
  )
}
