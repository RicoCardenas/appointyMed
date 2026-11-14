import { useEffect, useState, type FormEvent } from 'react'
import { auth, catalogo } from '../../api/servicios'
import type { Profesional } from '../../api/tipos'
import { useToast } from '../../componentes/Toasts'
import { Aviso, Boton, Campo, ErrorAviso, Esqueleto, Insignia, Pagina, Paginador, Seleccion, Vacio } from '../../componentes/ui'
import { DIAS, mensajeDeError } from '../../util/formato'
import { useAsync } from '../../util/useAsync'

export function Profesionales() {
  const [texto, setTexto] = useState('')
  const [buscado, setBuscado] = useState('')
  const [pagina, setPagina] = useState(0)
  const [creando, setCreando] = useState(false)
  const [abierto, setAbierto] = useState<string | null>(null)
  const lista = useAsync(() => catalogo.profesionales({ page: pagina, size: 10, texto: buscado }), [pagina, buscado])

  useEffect(() => { const t = setTimeout(() => { setBuscado(texto); setPagina(0) }, 300); return () => clearTimeout(t) }, [texto])

  return (
    <Pagina titulo="Profesionales" descripcion="Especialidades, centros y horarios de cada profesional." acciones={<Boton onClick={() => setCreando(!creando)}>{creando ? 'Cerrar' : 'Nuevo profesional'}</Boton>}>
      {creando && <div className="panel" style={{ marginBottom: 20 }}><h2>Nuevo profesional</h2><CrearProfesional alCrear={() => { setCreando(false); lista.recargar() }} /></div>}
      <div className="fila-filtros"><Campo id="f-texto" etiqueta="Buscar" placeholder="Nombre, apellido o registro" value={texto} onChange={(e) => setTexto(e.target.value)} /></div>
      {lista.cargando ? <Esqueleto /> : lista.error != null ? <ErrorAviso error={lista.error} alReintentar={lista.recargar} /> : !lista.datos?.contenido.length ? (
        <Vacio titulo="No hay profesionales" >{buscado ? 'Prueba con otro texto.' : 'Crea una cuenta de profesional en Cuentas y luego registralo aqui.'}</Vacio>
      ) : (
        <>
          <ul className="lista" style={{ listStyle: 'none', margin: 0, padding: 0 }}>
            {lista.datos.contenido.map((p) => (
              <li key={p.id} className="item">
                <div><div className="item-titulo">{p.nombres} {p.apellidos}</div><div className="item-sub">Registro {p.registroMedico}</div></div>
                <div className="acciones">
                  <Insignia tono={p.activo ? 'exito' : 'neutro'}>{p.activo ? 'Activo' : 'Inactivo'}</Insignia>
                  <Boton variante="secundario" className="btn-chico" aria-expanded={abierto === p.id} onClick={() => setAbierto(abierto === p.id ? null : p.id)}>Configurar</Boton>
                </div>
                {abierto === p.id && <Detalle profesional={p} alCambiar={lista.recargar} />}
              </li>
            ))}
          </ul>
          <Paginador pagina={lista.datos.pagina} totalPaginas={lista.datos.totalPaginas} total={lista.datos.totalElementos} alCambiar={setPagina} />
        </>
      )}
    </Pagina>
  )
}

function CrearProfesional({ alCrear }: { alCrear: () => void }) {
  const cuentas = useAsync(() => auth.usuarios({ rol: 'PROFESIONAL', activo: true, size: 100 }), [])
  const [f, setF] = useState({ usuarioId: '', nombres: '', apellidos: '', registroMedico: '' })
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const avisar = useToast()
  const poner = (k: keyof typeof f) => (e: { target: { value: string } }) => setF((x) => ({ ...x, [k]: e.target.value }))

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setGuardando(true)
    setError(null)
    try {
      await catalogo.crearProfesional(f)
      avisar('Profesional creado. Ahora asigna especialidades, centros y horario.')
      alCrear()
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setGuardando(false)
    }
  }

  return (
    <form className="form" onSubmit={enviar}>
      {error && <Aviso tipo="error">{error}</Aviso>}
      <Seleccion id="cuenta" etiqueta="Cuenta de profesional" ayuda="La cuenta se crea antes en Cuentas." value={f.usuarioId} onChange={poner('usuarioId')} required>
        <option value="">{cuentas.cargando ? 'Cargando...' : 'Elige una cuenta'}</option>
        {cuentas.datos?.contenido.map((c) => <option key={c.usuarioId} value={c.usuarioId}>{c.correo}</option>)}
      </Seleccion>
      <div className="rejilla">
        <Campo id="pn" etiqueta="Nombres" value={f.nombres} onChange={poner('nombres')} required maxLength={100} />
        <Campo id="pa" etiqueta="Apellidos" value={f.apellidos} onChange={poner('apellidos')} required maxLength={100} />
      </div>
      <Campo id="pr" etiqueta="Registro medico" ayuda="De 3 a 30 letras, numeros o guiones." value={f.registroMedico} onChange={poner('registroMedico')} required pattern="[A-Za-z0-9\-]{3,30}" />
      <div><Boton type="submit" cargando={guardando}>Crear profesional</Boton></div>
    </form>
  )
}

function Detalle({ profesional, alCambiar }: { profesional: Profesional; alCambiar: () => void }) {
  const detalle = useAsync(() => catalogo.profesional(profesional.id), [profesional.id])
  const especialidades = useAsync(() => catalogo.especialidades(), [])
  const centros = useAsync(() => catalogo.centros(), [])
  const horarios = useAsync(() => catalogo.horarios(profesional.id), [profesional.id])
  const [espSel, setEspSel] = useState<string[] | null>(null)
  const [cenSel, setCenSel] = useState<string[] | null>(null)
  const [error, setError] = useState<string | null>(null)
  const avisar = useToast()
  const esp = espSel ?? detalle.datos?.especialidadIds ?? []
  const cen = cenSel ?? detalle.datos?.centroIds ?? []
  const alternar = (l: string[], id: string) => (l.includes(id) ? l.filter((x) => x !== id) : [...l, id])

  async function guardar(tipo: 'esp' | 'cen') {
    setError(null)
    try {
      await (tipo === 'esp' ? catalogo.asociarEspecialidades(profesional.id, esp) : catalogo.asociarCentros(profesional.id, cen))
      avisar(tipo === 'esp' ? 'Especialidades guardadas.' : 'Centros guardados.')
      detalle.recargar(); horarios.recargar()
    } catch (e) {
      setError(mensajeDeError(e))
    }
  }

  async function desactivar() {
    try { await catalogo.desactivarProfesional(profesional.id); avisar('Profesional desactivado.'); alCambiar() } catch (e) { setError(mensajeDeError(e)) }
  }

  return (
    <div style={{ flexBasis: '100%', display: 'grid', gap: 18, paddingTop: 14, borderTop: '1px solid var(--line)' }}>
      {error && <Aviso tipo="error">{error}</Aviso>}
      {detalle.cargando ? <Esqueleto filas={2} /> : (
        <div className="dos-col">
          <fieldset style={{ border: 0, padding: 0, margin: 0 }}>
            <legend style={{ fontWeight: 650, marginBottom: 8 }}>Especialidades</legend>
            {especialidades.datos?.map((e) => (
              <label key={e.id} style={{ display: 'flex', gap: 8, alignItems: 'center', minHeight: 32 }}>
                <input type="checkbox" style={{ width: 'auto', minHeight: 0 }} checked={esp.includes(e.id)} onChange={() => setEspSel(alternar(esp, e.id))} /> {e.nombre}
              </label>
            ))}
            <Boton className="btn-chico" variante="secundario" onClick={() => guardar('esp')} style={{ marginTop: 8 }}>Guardar especialidades</Boton>
          </fieldset>
          <fieldset style={{ border: 0, padding: 0, margin: 0 }}>
            <legend style={{ fontWeight: 650, marginBottom: 8 }}>Centros donde atiende</legend>
            {centros.datos?.map((c) => (
              <label key={c.id} style={{ display: 'flex', gap: 8, alignItems: 'center', minHeight: 32 }}>
                <input type="checkbox" style={{ width: 'auto', minHeight: 0 }} checked={cen.includes(c.id)} onChange={() => setCenSel(alternar(cen, c.id))} /> {c.nombre}
              </label>
            ))}
            {!centros.datos?.length && <p className="ayuda">Aun no hay centros: crealos en Catalogo.</p>}
            <Boton className="btn-chico" variante="secundario" onClick={() => guardar('cen')} style={{ marginTop: 8 }}>Guardar centros</Boton>
          </fieldset>
        </div>
      )}
      <div>
        <h3 style={{ marginBottom: 8 }}>Horario semanal</h3>
        {horarios.datos?.length ? (
          <ul style={{ margin: '0 0 12px', paddingLeft: 18 }}>
            {horarios.datos.map((h) => <li key={h.id}>{DIAS[h.diaSemana]} {h.horaInicio} a {h.horaFin}, franjas de {h.duracionFranjaMin} min {!h.activo && <Insignia>inactivo</Insignia>}</li>)}
          </ul>
        ) : <p className="ayuda" style={{ marginBottom: 12 }}>Sin horarios todavia.</p>}
        <NuevoHorario profesionalId={profesional.id} centros={(centros.datos ?? []).filter((c) => cen.includes(c.id))} alCrear={() => horarios.recargar()} />
      </div>
      <div><Boton variante="fantasma" className="btn-chico" onClick={desactivar}>Desactivar profesional</Boton></div>
    </div>
  )
}

function NuevoHorario({ profesionalId, centros, alCrear }: { profesionalId: string; centros: { id: string; nombre: string }[]; alCrear: () => void }) {
  const [f, setF] = useState({ centroId: '', diaSemana: '1', horaInicio: '08:00', horaFin: '12:00', duracionFranjaMin: '30' })
  const [guardando, setGuardando] = useState(false)
  const [resultado, setResultado] = useState<{ tipo: 'exito' | 'aviso' | 'error'; texto: string } | null>(null)
  const poner = (k: keyof typeof f) => (e: { target: { value: string } }) => setF((x) => ({ ...x, [k]: e.target.value }))

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setGuardando(true)
    setResultado(null)
    try {
      const r = await catalogo.definirHorario(profesionalId, { centroId: f.centroId, diaSemana: Number(f.diaSemana), horaInicio: f.horaInicio, horaFin: f.horaFin, duracionFranjaMin: Number(f.duracionFranjaMin) })
      setResultado({ tipo: r.franjasGeneradas ? 'exito' : 'aviso', texto: `Horario guardado. ${r.mensaje}${r.franjasGeneradas ? ` (${r.franjasCreadas} franjas nuevas)` : ''}` })
      alCrear()
    } catch (err) {
      setResultado({ tipo: 'error', texto: mensajeDeError(err) })
    } finally {
      setGuardando(false)
    }
  }

  return (
    <form className="form" style={{ maxWidth: 'none' }} onSubmit={enviar}>
      {resultado && <Aviso tipo={resultado.tipo}>{resultado.texto}</Aviso>}
      <div className="rejilla">
        <Seleccion id={`h-c-${profesionalId}`} etiqueta="Centro" value={f.centroId} onChange={poner('centroId')} required>
          <option value="">{centros.length ? 'Elige un centro' : 'Asigna centros primero'}</option>
          {centros.map((c) => <option key={c.id} value={c.id}>{c.nombre}</option>)}
        </Seleccion>
        <Seleccion id={`h-d-${profesionalId}`} etiqueta="Dia" value={f.diaSemana} onChange={poner('diaSemana')}>
          {DIAS.slice(1).map((d, i) => <option key={d} value={i + 1}>{d}</option>)}
        </Seleccion>
        <Campo id={`h-i-${profesionalId}`} etiqueta="Desde" type="time" value={f.horaInicio} onChange={poner('horaInicio')} required />
        <Campo id={`h-f-${profesionalId}`} etiqueta="Hasta" type="time" value={f.horaFin} onChange={poner('horaFin')} required />
        <Campo id={`h-m-${profesionalId}`} etiqueta="Minutos por franja" type="number" min={5} max={240} value={f.duracionFranjaMin} onChange={poner('duracionFranjaMin')} />
      </div>
      <div><Boton type="submit" cargando={guardando} variante="secundario" disabled={!f.centroId}>Agregar horario y generar franjas</Boton></div>
    </form>
  )
}
