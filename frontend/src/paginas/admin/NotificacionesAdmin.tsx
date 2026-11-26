import { useState } from 'react'
import { notificaciones } from '../../api/servicios'
import { useToast } from '../../componentes/Toasts'
import { Boton, Cifras, ErrorAviso, Esqueleto, Insignia, Pagina, Paginador, Seleccion, Vacio } from '../../componentes/ui'
import { instante, mensajeDeError } from '../../util/formato'
import { useAsync } from '../../util/useAsync'

const TONO: Record<string, 'exito' | 'peligro' | 'aviso' | 'neutro'> = { ENVIADA: 'exito', FALLIDA: 'peligro', PENDIENTE: 'aviso', CANCELADA: 'neutro' }
const TIPOS: Record<string, string> = { CONFIRMACION: 'Confirmacion', REPROGRAMACION: 'Reprogramacion', CANCELACION: 'Cancelacion', RECORDATORIO: 'Recordatorio' }

export function NotificacionesAdmin() {
  const [estado, setEstado] = useState('')
  const [tipo, setTipo] = useState('')
  const [pagina, setPagina] = useState(0)
  const lista = useAsync(() => notificaciones.listar({ page: pagina, size: 10, estado, tipo }), [pagina, estado, tipo])
  const stats = useAsync(() => notificaciones.estadisticas(), [lista.datos])
  const [ejecutando, setEjecutando] = useState<string | null>(null)
  const avisar = useToast()

  async function ejecutar(cual: 'recordatorios' | 'reintentos') {
    setEjecutando(cual)
    try {
      const r = await notificaciones[cual]()
      avisar(`${r.procesadas} procesadas: ${r.enviadas} enviadas, ${r.fallidas} fallidas.`)
      lista.recargar()
    } catch (e) { avisar(mensajeDeError(e), 'error') } finally { setEjecutando(null) }
  }

  async function reenviar(id: string) {
    try { await notificaciones.reenviar(id); avisar('Notificacion reenviada.'); lista.recargar() } catch (e) { avisar(mensajeDeError(e), 'error') }
  }

  return (
    <Pagina titulo="Notificaciones" descripcion="Correos de confirmacion, cambio, cancelacion y recordatorio. Los fallidos se reintentan solos hasta 3 veces."
      acciones={<>
        <Boton variante="secundario" cargando={ejecutando === 'recordatorios'} onClick={() => ejecutar('recordatorios')}>Enviar recordatorios ahora</Boton>
        <Boton variante="secundario" cargando={ejecutando === 'reintentos'} onClick={() => ejecutar('reintentos')}>Reintentar fallidas ahora</Boton>
      </>}>
      {stats.datos && (
        <Cifras etiqueta="Filtrar por estado" activa={(k) => estado === k} alElegir={(k) => { setEstado(estado === k ? '' : k); setPagina(0) }}
          items={[
            { clave: 'ENVIADA', n: stats.datos.enviadas, texto: 'enviadas' },
            { clave: 'PENDIENTE', n: stats.datos.pendientes, texto: 'pendientes' },
            { clave: 'FALLIDA', n: stats.datos.fallidas, texto: 'fallidas' },
            { clave: 'CANCELADA', n: stats.datos.canceladas, texto: 'canceladas' },
          ]} />
      )}
      <div className="fila-filtros">
        <Seleccion id="n-estado" etiqueta="Estado" value={estado} onChange={(e) => { setEstado(e.target.value); setPagina(0) }}>
          <option value="">Todos</option>{Object.keys(TONO).map((k) => <option key={k} value={k}>{k[0] + k.slice(1).toLowerCase()}</option>)}
        </Seleccion>
        <Seleccion id="n-tipo" etiqueta="Tipo" value={tipo} onChange={(e) => { setTipo(e.target.value); setPagina(0) }}>
          <option value="">Todos</option>{Object.entries(TIPOS).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </Seleccion>
      </div>
      {lista.cargando ? <Esqueleto /> : lista.error != null ? <ErrorAviso error={lista.error} alReintentar={lista.recargar} /> : !lista.datos?.contenido.length ? (
        <Vacio titulo="No hay notificaciones con ese filtro">Se crean solas cuando alguien reserva, reprograma o cancela una cita.</Vacio>
      ) : (
        <>
          <div className="tabla-envoltura">
            <table className="tabla-apilar">
              <thead><tr><th>Tipo</th><th>Destinatario</th><th>Estado</th><th>Programada</th><th className="num">Intentos</th><th><span className="solo-lectores">Acciones</span></th></tr></thead>
              <tbody>
                {lista.datos.contenido.map((n) => (
                  <tr key={n.id}>
                    <td data-etiqueta="Tipo">{TIPOS[n.tipo] ?? n.tipo}</td>
                    <td data-etiqueta="Destinatario">{n.destinatario}</td>
                    <td data-etiqueta="Estado"><Insignia tono={TONO[n.estado]}>{n.estado[0] + n.estado.slice(1).toLowerCase()}</Insignia></td>
                    <td data-etiqueta="Programada">{instante(n.programadaPara)}</td>
                    <td data-etiqueta="Intentos" className="num">{n.intentos}</td>
                    <td>{(n.estado === 'FALLIDA' || n.estado === 'PENDIENTE') && <Boton variante="fantasma" className="btn-chico" onClick={() => reenviar(n.id)}>Reenviar</Boton>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Paginador pagina={lista.datos.pagina} totalPaginas={lista.datos.totalPaginas} total={lista.datos.totalElementos} alCambiar={setPagina} />
        </>
      )}
    </Pagina>
  )
}
