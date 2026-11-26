import { useEffect, useState, type FormEvent } from 'react'
import { auth } from '../../api/servicios'
import { useToast } from '../../componentes/Toasts'
import { Aviso, Boton, Campo, ErrorAviso, Esqueleto, Pagina } from '../../componentes/ui'
import { fechaLarga, mensajeDeError } from '../../util/formato'
import { useAsync } from '../../util/useAsync'

export function Perfil() {
  const perfil = useAsync(() => auth.miPerfil(), [])
  return (
    <Pagina titulo="Mi perfil">
      {perfil.cargando ? <Esqueleto /> : perfil.error != null ? <ErrorAviso error={perfil.error} alReintentar={perfil.recargar} /> : perfil.datos && (
        <>
          <div className="perfil-cab">
            <span className="avatar" aria-hidden>{(perfil.datos.nombres[0] ?? '') + (perfil.datos.apellidos[0] ?? '')}</span>
            <div>
              <h2>{perfil.datos.nombres} {perfil.datos.apellidos}</h2>
              <p className="item-sub">Cedula {perfil.datos.cedula} · Nacimiento {fechaLarga(perfil.datos.fechaNacimiento)}</p>
              <p className="ayuda" style={{ marginTop: 4 }}>Estos datos no se pueden cambiar aqui. Si hay un error, pide la correccion al personal administrativo.</p>
            </div>
          </div>
          <div className="dos-col">
            <div className="panel">
              <h2>Datos de contacto</h2>
              <Contacto inicial={perfil.datos} alGuardar={perfil.recargar} />
            </div>
            <div className="panel"><h2>Cambiar clave</h2><CambiarClave /></div>
          </div>
        </>
      )}
    </Pagina>
  )
}

function Contacto({ inicial, alGuardar }: { inicial: { correo: string; telefono?: string | null; direccion?: string | null; ciudad?: string | null }; alGuardar: () => void }) {
  const [f, setF] = useState({ correo: inicial.correo, telefono: inicial.telefono ?? '', direccion: inicial.direccion ?? '', ciudad: inicial.ciudad ?? '' })
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const avisar = useToast()
  useEffect(() => setF({ correo: inicial.correo, telefono: inicial.telefono ?? '', direccion: inicial.direccion ?? '', ciudad: inicial.ciudad ?? '' }), [inicial])
  const poner = (k: keyof typeof f) => (e: { target: { value: string } }) => setF((x) => ({ ...x, [k]: e.target.value }))

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setGuardando(true)
    setError(null)
    try {
      const cuerpo: Record<string, string> = {}
      if (f.correo !== inicial.correo) cuerpo.correo = f.correo.trim()
      if (f.telefono && f.telefono !== (inicial.telefono ?? '')) cuerpo.telefono = f.telefono
      if (f.direccion && f.direccion !== (inicial.direccion ?? '')) cuerpo.direccion = f.direccion
      if (f.ciudad && f.ciudad !== (inicial.ciudad ?? '')) cuerpo.ciudad = f.ciudad
      if (!Object.keys(cuerpo).length) { avisar('No hay cambios que guardar.'); return }
      await auth.actualizarPerfil(cuerpo)
      avisar('Datos actualizados.')
      alGuardar()
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setGuardando(false)
    }
  }

  return (
    <form className="form" onSubmit={enviar}>
      {error && <Aviso tipo="error">{error}</Aviso>}
      <Campo id="p-correo" etiqueta="Correo" type="email" value={f.correo} onChange={poner('correo')} required />
      <Campo id="p-tel" etiqueta="Telefono" type="tel" value={f.telefono} onChange={poner('telefono')} pattern="[0-9+() \-]{7,20}" />
      <Campo id="p-ciudad" etiqueta="Ciudad" value={f.ciudad} onChange={poner('ciudad')} maxLength={100} />
      <Campo id="p-dir" etiqueta="Direccion" value={f.direccion} onChange={poner('direccion')} maxLength={200} />
      <div><Boton type="submit" cargando={guardando}>Guardar cambios</Boton></div>
    </form>
  )
}

function CambiarClave() {
  const [actual, setActual] = useState('')
  const [nueva, setNueva] = useState('')
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const avisar = useToast()

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setGuardando(true)
    setError(null)
    try {
      await auth.cambiarClave(actual, nueva)
      avisar('Clave cambiada. Usala la proxima vez que entres.')
      setActual(''); setNueva('')
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setGuardando(false)
    }
  }

  return (
    <form className="form" onSubmit={enviar}>
      {error && <Aviso tipo="error">{error}</Aviso>}
      <Campo id="c-actual" etiqueta="Clave actual" type="password" autoComplete="current-password" value={actual} onChange={(e) => setActual(e.target.value)} required />
      <Campo id="c-nueva" etiqueta="Clave nueva" type="password" autoComplete="new-password" ayuda="Al menos 8 caracteres, con una letra y un numero." value={nueva} onChange={(e) => setNueva(e.target.value)} required minLength={8} maxLength={72} />
      <div><Boton type="submit" cargando={guardando} disabled={!actual || !nueva}>Cambiar clave</Boton></div>
    </form>
  )
}
