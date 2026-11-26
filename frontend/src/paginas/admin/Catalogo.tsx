import { useState, type FormEvent } from 'react'
import { catalogo } from '../../api/servicios'
import type { Centro, Especialidad } from '../../api/tipos'
import { useToast } from '../../componentes/Toasts'
import { Aviso, Boton, Campo, ErrorAviso, Esqueleto, Insignia, Pagina, Vacio } from '../../componentes/ui'
import { fechaLarga, mensajeDeError } from '../../util/formato'
import { useAsync } from '../../util/useAsync'

type Pestana = 'especialidades' | 'centros' | 'festivos'

export function Catalogo() {
  const [pestana, setPestana] = useState<Pestana>('especialidades')
  const PESTANAS: [Pestana, string][] = [['especialidades', 'Especialidades'], ['centros', 'Centros'], ['festivos', 'Festivos']]
  return (
    <Pagina titulo="Catalogo" descripcion="Especialidades y centros de atencion. Los festivos vienen de un servicio externo y no se agendan citas en ellos.">
      <div className="pestanas" role="tablist" aria-label="Catalogo">
        {PESTANAS.map(([k, t]) => (
          <button key={k} type="button" role="tab" className="pestana" aria-selected={pestana === k} onClick={() => setPestana(k)}>{t}</button>
        ))}
      </div>
      {pestana === 'especialidades' && <Especialidades />}
      {pestana === 'centros' && <Centros />}
      {pestana === 'festivos' && <Festivos />}
    </Pagina>
  )
}

function Especialidades() {
  const lista = useAsync(() => catalogo.especialidades(true), [])
  const [editando, setEditando] = useState<Especialidad | 'nueva' | null>(null)
  const avisar = useToast()
  async function desactivar(e: Especialidad) {
    try { await catalogo.desactivarEspecialidad(e.id); avisar('Especialidad desactivada.'); lista.recargar() } catch (err) { avisar(mensajeDeError(err), 'error') }
  }
  return (
    <>
      <div className="acciones" style={{ marginBottom: 14 }}><Boton onClick={() => setEditando('nueva')}>Nueva especialidad</Boton></div>
      {editando && <div className="panel" style={{ marginBottom: 20 }}>
        <FormEspecialidad inicial={editando === 'nueva' ? undefined : editando} alTerminar={() => { setEditando(null); lista.recargar() }} />
      </div>}
      {lista.cargando ? <Esqueleto /> : lista.error != null ? <ErrorAviso error={lista.error} alReintentar={lista.recargar} /> : !lista.datos?.length ? <Vacio titulo="Sin especialidades">Crea la primera para poder asignarla a los profesionales.</Vacio> : (
        <ul className="lista" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
          {lista.datos.map((e) => (
            <li key={e.id} className="item">
              <div><div className="item-titulo">{e.nombre}</div>{e.descripcion && <div className="item-sub">{e.descripcion}</div>}</div>
              <div className="acciones">
                <Insignia tono={e.activo ? 'exito' : 'neutro'}>{e.activo ? 'Activa' : 'Inactiva'}</Insignia>
                <Boton variante="secundario" className="btn-chico" onClick={() => setEditando(e)}>Editar</Boton>
                {e.activo && <Boton variante="fantasma" className="btn-chico" onClick={() => desactivar(e)}>Desactivar</Boton>}
              </div>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}

function FormEspecialidad({ inicial, alTerminar }: { inicial?: Especialidad; alTerminar: () => void }) {
  const [nombre, setNombre] = useState(inicial?.nombre ?? '')
  const [descripcion, setDescripcion] = useState(inicial?.descripcion ?? '')
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const avisar = useToast()
  async function enviar(e: FormEvent) {
    e.preventDefault(); setGuardando(true); setError(null)
    try {
      const d = { nombre: nombre.trim(), descripcion: descripcion.trim() || undefined }
      await (inicial ? catalogo.actualizarEspecialidad(inicial.id, d) : catalogo.crearEspecialidad(d))
      avisar('Especialidad guardada.'); alTerminar()
    } catch (err) { setError(mensajeDeError(err)) } finally { setGuardando(false) }
  }
  return (
    <form className="form" onSubmit={enviar}>
      <h2>{inicial ? 'Editar especialidad' : 'Nueva especialidad'}</h2>
      {error && <Aviso tipo="error">{error}</Aviso>}
      <Campo id="e-n" etiqueta="Nombre" value={nombre} onChange={(e) => setNombre(e.target.value)} required maxLength={100} />
      <Campo id="e-d" etiqueta="Descripcion (opcional)" value={descripcion} onChange={(e) => setDescripcion(e.target.value)} maxLength={300} />
      <div className="acciones"><Boton type="submit" cargando={guardando}>Guardar</Boton><Boton type="button" variante="fantasma" onClick={alTerminar}>Cancelar</Boton></div>
    </form>
  )
}

function Centros() {
  const lista = useAsync(() => catalogo.centros(true), [])
  const [editando, setEditando] = useState<Centro | 'nuevo' | null>(null)
  const avisar = useToast()
  async function desactivar(c: Centro) {
    try { await catalogo.desactivarCentro(c.id); avisar('Centro desactivado.'); lista.recargar() } catch (err) { avisar(mensajeDeError(err), 'error') }
  }
  return (
    <>
      <div className="acciones" style={{ marginBottom: 14 }}><Boton onClick={() => setEditando('nuevo')}>Nuevo centro</Boton></div>
      {editando && <div className="panel" style={{ marginBottom: 20 }}>
        <FormCentro inicial={editando === 'nuevo' ? undefined : editando} alTerminar={() => { setEditando(null); lista.recargar() }} />
      </div>}
      {lista.cargando ? <Esqueleto /> : lista.error != null ? <ErrorAviso error={lista.error} alReintentar={lista.recargar} /> : !lista.datos?.length ? <Vacio titulo="Sin centros">Crea el primer centro de atencion.</Vacio> : (
        <ul className="lista" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
          {lista.datos.map((c) => (
            <li key={c.id} className="item">
              <div><div className="item-titulo">{c.nombre}</div><div className="item-sub">{c.direccion}, {c.ciudad}{c.telefono ? ` · ${c.telefono}` : ''}</div></div>
              <div className="acciones">
                <Insignia tono={c.activo ? 'exito' : 'neutro'}>{c.activo ? 'Activo' : 'Inactivo'}</Insignia>
                <Boton variante="secundario" className="btn-chico" onClick={() => setEditando(c)}>Editar</Boton>
                {c.activo && <Boton variante="fantasma" className="btn-chico" onClick={() => desactivar(c)}>Desactivar</Boton>}
              </div>
            </li>
          ))}
        </ul>
      )}
    </>
  )
}

function FormCentro({ inicial, alTerminar }: { inicial?: Centro; alTerminar: () => void }) {
  const [f, setF] = useState({ nombre: inicial?.nombre ?? '', direccion: inicial?.direccion ?? '', ciudad: inicial?.ciudad ?? '', telefono: inicial?.telefono ?? '' })
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const avisar = useToast()
  const poner = (k: keyof typeof f) => (e: { target: { value: string } }) => setF((x) => ({ ...x, [k]: e.target.value }))
  async function enviar(e: FormEvent) {
    e.preventDefault(); setGuardando(true); setError(null)
    try {
      const d = { nombre: f.nombre.trim(), direccion: f.direccion.trim(), ciudad: f.ciudad.trim(), telefono: f.telefono.trim() || undefined }
      await (inicial ? catalogo.actualizarCentro(inicial.id, d) : catalogo.crearCentro(d))
      avisar('Centro guardado.'); alTerminar()
    } catch (err) { setError(mensajeDeError(err)) } finally { setGuardando(false) }
  }
  return (
    <form className="form" onSubmit={enviar}>
      <h2>{inicial ? 'Editar centro' : 'Nuevo centro'}</h2>
      {error && <Aviso tipo="error">{error}</Aviso>}
      <Campo id="c-n" etiqueta="Nombre" value={f.nombre} onChange={poner('nombre')} required maxLength={100} />
      <Campo id="c-d" etiqueta="Direccion" value={f.direccion} onChange={poner('direccion')} required maxLength={200} />
      <div className="rejilla">
        <Campo id="c-c" etiqueta="Ciudad" value={f.ciudad} onChange={poner('ciudad')} required maxLength={100} />
        <Campo id="c-t" etiqueta="Telefono (opcional)" type="tel" value={f.telefono} onChange={poner('telefono')} pattern="[0-9+() \-]{7,20}" />
      </div>
      <div className="acciones"><Boton type="submit" cargando={guardando}>Guardar</Boton><Boton type="button" variante="fantasma" onClick={alTerminar}>Cancelar</Boton></div>
    </form>
  )
}

function Festivos() {
  const [anio, setAnio] = useState(new Date().getFullYear())
  const lista = useAsync(() => catalogo.festivos(anio), [anio])
  return (
    <>
      <div className="fila-filtros"><Campo id="anio" etiqueta="Anio" type="number" min={2000} max={2100} value={anio} onChange={(e) => setAnio(Number(e.target.value))} /></div>
      {lista.cargando ? <Esqueleto /> : lista.error != null ? <ErrorAviso error={lista.error} alReintentar={lista.recargar} /> : (
        <>
          <Aviso>Al generar franjas se omiten estos dias. Las franjas ya generadas antes no se retiran.</Aviso>
          <div className="lista">{lista.datos?.map((f) => <div key={f.fecha} className="item item-festivo"><span className="item-titulo">{fechaLarga(f.fecha)}</span><span>{f.nombre}</span></div>)}</div>
        </>
      )}
    </>
  )
}
