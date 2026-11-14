import { useMemo, useState } from 'react'
import { citas } from '../../api/servicios'
import type { Cita } from '../../api/tipos'
import { useToast } from '../../componentes/Toasts'
import { Boton, Confirmar, ErrorAviso, Esqueleto, Insignia, Pagina, Paginador, Seleccion, Vacio } from '../../componentes/ui'
import { ESTADOS_CITA, fechaLarga, idCorto, mensajeDeError } from '../../util/formato'
import { useAsync } from '../../util/useAsync'
import { useNombres } from '../../util/useNombres'

const TONO: Record<string, 'acento' | 'neutro' | 'exito' | 'aviso'> = { CONFIRMADA: 'acento', CANCELADA: 'neutro', COMPLETADA: 'exito', NO_ASISTIDA: 'aviso' }

export function CitasAdmin() {
  const [estado, setEstado] = useState('')
  const [pagina, setPagina] = useState(0)
  const lista = useAsync(() => citas.listar({ page: pagina, size: 10, estado }), [pagina, estado])
  const stats = useAsync(() => citas.estadisticas(), [lista.datos])
  const nombres = useNombres(useMemo(() => lista.datos?.contenido ?? [], [lista.datos]))
  const [aCancelar, setACancelar] = useState<Cita | null>(null)
  const [cancelando, setCancelando] = useState(false)
  const avisar = useToast()

  async function cancelar() {
    if (!aCancelar) return
    setCancelando(true)
    try {
      await citas.cancelar(aCancelar.id, 'Cancelada por administracion')
      avisar('Cita cancelada.')
      lista.recargar()
    } catch (e) {
      avisar(mensajeDeError(e), 'error')
    } finally {
      setCancelando(false)
      setACancelar(null)
    }
  }

  return (
    <Pagina titulo="Citas" descripcion="Todas las citas del sistema, de la mas reciente a la mas antigua.">
      {stats.datos && (
        <div className="cifras">
          <div className="cifra"><strong>{stats.datos.confirmadas}</strong><span>confirmadas</span></div>
          <div className="cifra"><strong>{stats.datos.completadas}</strong><span>atendidas</span></div>
          <div className="cifra"><strong>{stats.datos.noAsistidas}</strong><span>no asistieron</span></div>
          <div className="cifra"><strong>{stats.datos.canceladas}</strong><span>canceladas</span></div>
        </div>
      )}
      <div className="fila-filtros">
        <Seleccion id="f-estado" etiqueta="Estado" value={estado} onChange={(e) => { setEstado(e.target.value); setPagina(0) }}>
          <option value="">Todos</option>
          {Object.entries(ESTADOS_CITA).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </Seleccion>
      </div>
      {lista.cargando ? <Esqueleto /> : lista.error != null ? <ErrorAviso error={lista.error} alReintentar={lista.recargar} /> : !lista.datos?.contenido.length ? (
        <Vacio titulo="No hay citas con ese filtro">Prueba con otro estado.</Vacio>
      ) : (
        <>
          <div className="tabla-envoltura">
            <table className="tabla-apilar">
              <thead><tr><th>Fecha y hora</th><th>Profesional</th><th>Paciente</th><th>Centro</th><th>Estado</th><th><span className="solo-lectores">Acciones</span></th></tr></thead>
              <tbody>
                {lista.datos.contenido.map((c) => (
                  <tr key={c.id}>
                    <td data-etiqueta="Fecha y hora">{fechaLarga(c.fecha)} · {c.horaInicio}</td>
                    <td data-etiqueta="Profesional">{nombres.profesional(c.profesionalId)}</td>
                    <td data-etiqueta="Paciente">{idCorto(c.pacienteId)}</td>
                    <td data-etiqueta="Centro">{nombres.centro(c.centroId)}</td>
                    <td data-etiqueta="Estado"><Insignia tono={TONO[c.estado]}>{ESTADOS_CITA[c.estado]}</Insignia></td>
                    <td>{c.estado === 'CONFIRMADA' && <Boton variante="fantasma" className="btn-chico" onClick={() => setACancelar(c)}>Cancelar</Boton>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginador pagina={lista.datos.pagina} totalPaginas={lista.datos.totalPaginas} total={lista.datos.totalElementos} alCambiar={setPagina} />
        </>
      )}
      <Confirmar abierto={!!aCancelar} titulo="Cancelar esta cita?" peligro cargando={cancelando} textoAccion="Si, cancelar" alConfirmar={cancelar} alCancelar={() => setACancelar(null)}
        texto="La franja quedara libre y se notificara al paciente. Esta accion no se puede deshacer." />
    </Pagina>
  )
}
