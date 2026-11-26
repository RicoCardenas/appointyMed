import { CheckCircle2 } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { catalogo, citas } from '../../api/servicios'
import type { Centro, Cita, Festivo, Franja } from '../../api/tipos'
import { useToast } from '../../componentes/Toasts'
import { Aviso, Boton, Campo, ErrorAviso, Esqueleto, Pagina, Seleccion, Vacio } from '../../componentes/ui'
import { fechaLarga, hoyIso, mensajeDeError, sumarDias } from '../../util/formato'
import { useAsync } from '../../util/useAsync'

/** Reservar en un solo recorrido: especialidad, profesional, centro, fecha y franja. Cada paso se habilita con el anterior. */
export function Reservar() {
  const [especialidadId, setEspecialidadId] = useState('')
  const [profesionalId, setProfesionalId] = useState('')
  const [centroId, setCentroId] = useState('')
  const [fecha, setFecha] = useState(sumarDias(hoyIso(), 1))
  const [franjaId, setFranjaId] = useState('')
  const [reservando, setReservando] = useState(false)
  const [errorReserva, setErrorReserva] = useState<string | null>(null)
  const [hecha, setHecha] = useState<Cita | null>(null)
  const avisar = useToast()

  const especialidades = useAsync(() => catalogo.especialidades(), [])
  const profesionales = useAsync(() => (especialidadId ? catalogo.profesionalesDe(especialidadId) : Promise.resolve([])), [especialidadId])
  const centros = useAsync<Centro[]>(() => (profesionalId ? catalogo.centrosDe(profesionalId) : Promise.resolve([])), [profesionalId])
  const franjas = useAsync<Franja[]>(
    () => (profesionalId && centroId && fecha ? citas.disponibilidad(profesionalId, centroId, fecha) : Promise.resolve([])),
    [profesionalId, centroId, fecha],
  )

  // Festivos del anio elegido (API externa, con cache en catalog). Si no responde, se ignora y queda el aviso general.
  const anio = Number(fecha.slice(0, 4))
  const festivos = useAsync<Festivo[]>(
    () => (profesionalId && anio > 1999 ? catalogo.festivos(anio) : Promise.resolve([])),
    [profesionalId, anio],
  )
  const festivo = festivos.datos?.find((f) => f.fecha === fecha)

  function siguienteDiaLibre() {
    let d = sumarDias(fecha, 1)
    for (let i = 0; i < 14 && festivos.datos?.some((f) => f.fecha === d); i++) d = sumarDias(d, 1)
    setFecha(d)
    setFranjaId('')
  }

  // Con un solo centro no hay nada que elegir.
  useEffect(() => {
    if (centros.datos?.length === 1 && !centroId) setCentroId(centros.datos[0].id)
  }, [centros.datos, centroId])

  const especialidad = especialidades.datos?.find((e) => e.id === especialidadId)
  const profesional = profesionales.datos?.find((p) => p.id === profesionalId)
  const centro = centros.datos?.find((c) => c.id === centroId)
  const franja = franjas.datos?.find((f) => f.id === franjaId)

  async function reservar() {
    setReservando(true)
    setErrorReserva(null)
    try {
      setHecha(await citas.reservar({ especialidadId, profesionalId, centroId, franjaId }))
      avisar('Cita confirmada.')
    } catch (e) {
      setErrorReserva(mensajeDeError(e))
      setFranjaId('')
      franjas.recargar() // un 409 significa que otra persona gano la franja: se muestran las que quedan
    } finally {
      setReservando(false)
    }
  }

  function reiniciar() {
    setEspecialidadId(''); setProfesionalId(''); setCentroId(''); setFranjaId(''); setHecha(null); setErrorReserva(null)
  }

  if (hecha) {
    return (
      <Pagina titulo="Cita confirmada">
        <div className="panel" style={{ maxWidth: 560 }}>
          <p style={{ display: 'flex', gap: 10, alignItems: 'center', fontWeight: 650, marginBottom: 10 }}><CheckCircle2 aria-hidden /> Tu cita quedo agendada</p>
          <p>{fechaLarga(hecha.fecha)} a las {hecha.horaInicio}</p>
          <p className="item-sub" style={{ marginTop: 4 }}>{profesional ? `${profesional.nombres} ${profesional.apellidos}` : ''}{centro ? ` en ${centro.nombre}, ${centro.direccion}` : ''}</p>
          <p className="item-sub" style={{ marginTop: 10 }}>Te enviaremos un correo de confirmacion y un recordatorio el dia anterior.</p>
          <div className="acciones" style={{ marginTop: 18 }}>
            <Link to="/mis-citas" className="btn btn-primario">Ver mis citas</Link>
            <Boton variante="secundario" onClick={reiniciar}>Agendar otra</Boton>
          </div>
        </div>
      </Pagina>
    )
  }

  return (
    <Pagina titulo="Reservar una cita" descripcion="Elige especialidad y profesional; veras solo las franjas libres.">
      {especialidades.error != null && <ErrorAviso error={especialidades.error} alReintentar={especialidades.recargar} />}
      <div className="reserva">
      <div className="pasos">
        <div className="paso">
          <h2>1. Especialidad y profesional</h2>
          <div className="rejilla">
            <Seleccion id="esp" etiqueta="Especialidad" value={especialidadId} onChange={(e) => { setEspecialidadId(e.target.value); setProfesionalId(''); setCentroId(''); setFranjaId('') }}>
              <option value="">{especialidades.cargando ? 'Cargando...' : 'Elige una especialidad'}</option>
              {especialidades.datos?.map((e) => <option key={e.id} value={e.id}>{e.nombre}</option>)}
            </Seleccion>
            <Seleccion id="prof" etiqueta="Profesional" value={profesionalId} disabled={!especialidadId || profesionales.cargando} onChange={(e) => { setProfesionalId(e.target.value); setCentroId(''); setFranjaId('') }}>
              <option value="">{!especialidadId ? 'Primero la especialidad' : profesionales.cargando ? 'Cargando...' : 'Elige un profesional'}</option>
              {profesionales.datos?.map((p) => <option key={p.id} value={p.id}>{p.nombres} {p.apellidos}</option>)}
            </Seleccion>
          </div>
          {especialidadId && !profesionales.cargando && profesionales.datos?.length === 0 && (
            <Aviso tipo="aviso">Esta especialidad todavia no tiene profesionales disponibles. Prueba con otra.</Aviso>
          )}
        </div>

        {profesionalId && (
          <div className="paso">
            <h2>2. Centro y fecha</h2>
            <div className="rejilla">
              <Seleccion id="centro" etiqueta="Centro de atencion" value={centroId} disabled={centros.cargando} onChange={(e) => { setCentroId(e.target.value); setFranjaId('') }}>
                <option value="">{centros.cargando ? 'Cargando...' : 'Elige un centro'}</option>
                {centros.datos?.map((c) => <option key={c.id} value={c.id}>{c.nombre} · {c.ciudad}</option>)}
              </Seleccion>
              <Campo id="fecha" etiqueta="Fecha" type="date" min={hoyIso()} value={fecha} onChange={(e) => { setFecha(e.target.value); setFranjaId('') }} />
            </div>
            {centros.datos?.length === 0 && <Aviso tipo="aviso">Este profesional aun no tiene centros asignados.</Aviso>}
          </div>
        )}

        {profesionalId && centroId && fecha && (
          <div className="paso">
            <h2>3. Hora</h2>
            <p className="paso-resumen">{fechaLarga(fecha)}</p>
            {festivo ? (
              <Aviso tipo="aviso" accion={<Boton variante="secundario" className="btn-chico" onClick={siguienteDiaLibre}>Ver el dia siguiente</Boton>}>
                <span><strong>Ese dia es festivo: {festivo.nombre}.</strong> No se agendan citas en festivos; elige otra fecha.</span>
              </Aviso>
            ) : franjas.cargando ? <Esqueleto filas={2} /> : franjas.error != null ? (
              <ErrorAviso error={franjas.error} alReintentar={franjas.recargar} />
            ) : franjas.datos && franjas.datos.length > 0 ? (
              <div className="franjas" role="group" aria-label="Horas disponibles">
                {franjas.datos.map((f) => (
                  <button key={f.id} type="button" className="franja" aria-pressed={franjaId === f.id} onClick={() => setFranjaId(f.id)}>{f.horaInicio}</button>
                ))}
              </div>
            ) : (
              <Vacio titulo="No hay horas libres ese dia">Prueba con otra fecha: los profesionales atienden segun su horario semanal, y los festivos no tienen citas.</Vacio>
            )}
          </div>
        )}

        {errorReserva && <Aviso tipo="error">{errorReserva}</Aviso>}
      </div>

      <aside className="resumen" aria-label="Resumen de tu cita">
        <h2>Tu cita</h2>
        <dl>
          <dt>Especialidad</dt><dd>{especialidad?.nombre ?? <span className="pendiente">Por elegir</span>}</dd>
          <dt>Profesional</dt><dd>{profesional ? `${profesional.nombres} ${profesional.apellidos}` : <span className="pendiente">Por elegir</span>}</dd>
          <dt>Centro</dt><dd>{centro ? `${centro.nombre} · ${centro.ciudad}` : <span className="pendiente">Por elegir</span>}</dd>
          <dt>Fecha y hora</dt><dd>{franja ? `${fechaLarga(franja.fecha)}, ${franja.horaInicio} a ${franja.horaFin}` : <span className="pendiente">Elige una hora libre</span>}</dd>
        </dl>
        <Boton cargando={reservando} disabled={!franja} onClick={reservar}>Confirmar cita</Boton>
      </aside>
      </div>
    </Pagina>
  )
}
