import { useState, type FormEvent } from 'react'
import { auth } from '../../api/servicios'
import type { Cuenta, Rol } from '../../api/tipos'
import { useToast } from '../../componentes/Toasts'
import { Aviso, Boton, Campo, Cifras, ErrorAviso, Esqueleto, Insignia, Pagina, Paginador, Seleccion, Vacio } from '../../componentes/ui'
import { instante, mensajeDeError } from '../../util/formato'
import { useAsync } from '../../util/useAsync'

const ROLES: Record<Rol, string> = { PACIENTE: 'Paciente', PROFESIONAL: 'Profesional', ADMINISTRATIVO: 'Administrativo' }

export function Cuentas() {
  const [rol, setRol] = useState('')
  const [activo, setActivo] = useState('')
  const [pagina, setPagina] = useState(0)
  const [creando, setCreando] = useState(false)
  const lista = useAsync(() => auth.usuarios({ page: pagina, size: 10, rol, activo: activo === '' ? undefined : activo === 'true' }), [pagina, rol, activo])
  const stats = useAsync(() => auth.estadisticas(), [lista.datos])
  const avisar = useToast()

  async function cambiar(c: Cuenta) {
    try {
      await (c.activo ? auth.desactivar(c.usuarioId) : auth.activar(c.usuarioId))
      avisar(c.activo ? 'Cuenta desactivada.' : 'Cuenta activada.')
      lista.recargar()
    } catch (e) {
      avisar(mensajeDeError(e), 'error')
    }
  }

  return (
    <Pagina titulo="Cuentas" descripcion="Pacientes, profesionales y personal administrativo." acciones={<Boton onClick={() => setCreando(!creando)}>{creando ? 'Cerrar' : 'Nueva cuenta de personal'}</Boton>}>
      {creando && <div className="panel" style={{ marginBottom: 20 }}><h2>Nueva cuenta de personal</h2><CrearCuenta alCrear={() => { setCreando(false); lista.recargar() }} /></div>}
      {stats.datos && (
        <Cifras etiqueta="Filtrar cuentas" activa={(k) => (k === 'INACTIVAS' ? activo === 'false' : rol === k)}
          alElegir={(k) => { if (k === 'INACTIVAS') setActivo(activo === 'false' ? '' : 'false'); else setRol(rol === k ? '' : k); setPagina(0) }}
          items={[
            { clave: 'PACIENTE', n: stats.datos.pacientes, texto: 'pacientes' },
            { clave: 'PROFESIONAL', n: stats.datos.profesionales, texto: 'profesionales' },
            { clave: 'ADMINISTRATIVO', n: stats.datos.administrativos, texto: 'administrativos' },
            { clave: 'INACTIVAS', n: stats.datos.inactivos, texto: 'inactivas' },
          ]} />
      )}
      <div className="fila-filtros">
        <Seleccion id="f-rol" etiqueta="Rol" value={rol} onChange={(e) => { setRol(e.target.value); setPagina(0) }}>
          <option value="">Todos</option>{Object.entries(ROLES).map(([k, v]) => <option key={k} value={k}>{v}</option>)}
        </Seleccion>
        <Seleccion id="f-act" etiqueta="Estado" value={activo} onChange={(e) => { setActivo(e.target.value); setPagina(0) }}>
          <option value="">Todos</option><option value="true">Activas</option><option value="false">Inactivas</option>
        </Seleccion>
      </div>
      {lista.cargando ? <Esqueleto /> : lista.error != null ? <ErrorAviso error={lista.error} alReintentar={lista.recargar} /> : !lista.datos?.contenido.length ? <Vacio titulo="No hay cuentas con ese filtro" /> : (
        <>
          <div className="tabla-envoltura">
            <table className="tabla-apilar">
              <thead><tr><th>Correo</th><th>Rol</th><th>Creada</th><th>Estado</th><th><span className="solo-lectores">Acciones</span></th></tr></thead>
              <tbody>
                {lista.datos.contenido.map((c) => (
                  <tr key={c.usuarioId}>
                    <td data-etiqueta="Correo">{c.correo}</td>
                    <td data-etiqueta="Rol"><Insignia tono={c.rol === 'ADMINISTRATIVO' ? 'acento' : 'neutro'}>{ROLES[c.rol]}</Insignia></td>
                    <td data-etiqueta="Creada">{instante(c.creadoEn)}</td>
                    <td data-etiqueta="Estado"><Insignia tono={c.activo ? 'exito' : 'neutro'}>{c.activo ? 'Activa' : 'Inactiva'}</Insignia></td>
                    <td><Boton variante="fantasma" className="btn-chico" onClick={() => cambiar(c)}>{c.activo ? 'Desactivar' : 'Activar'}</Boton></td>
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

function CrearCuenta({ alCrear }: { alCrear: () => void }) {
  const [correo, setCorreo] = useState('')
  const [clave, setClave] = useState('')
  const [rol, setRol] = useState<Rol>('PROFESIONAL')
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const avisar = useToast()

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setGuardando(true)
    setError(null)
    try {
      await auth.crearCuenta({ correo: correo.trim(), clave, rol })
      avisar('Cuenta creada.')
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
      <Campo id="n-correo" etiqueta="Correo" type="email" value={correo} onChange={(e) => setCorreo(e.target.value)} required />
      <Campo id="n-clave" etiqueta="Clave inicial" type="password" autoComplete="new-password" ayuda="Al menos 8 caracteres, con una letra y un numero." value={clave} onChange={(e) => setClave(e.target.value)} required minLength={8} />
      <Seleccion id="n-rol" etiqueta="Rol" value={rol} onChange={(e) => setRol(e.target.value as Rol)}>
        <option value="PROFESIONAL">Profesional</option><option value="ADMINISTRATIVO">Administrativo</option>
      </Seleccion>
      <div><Boton type="submit" cargando={guardando}>Crear cuenta</Boton></div>
    </form>
  )
}
