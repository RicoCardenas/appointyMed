import { useState, type FormEvent } from 'react'
import { Link, Navigate } from 'react-router-dom'
import { auth } from '../../api/servicios'
import { Entrada } from '../../componentes/Entrada'
import { Aviso, Boton, Campo } from '../../componentes/ui'
import { INICIO_POR_ROL, useSesion } from '../../sesion/Sesion'
import { mensajeDeError } from '../../util/formato'

export function Login() {
  const { sesion, iniciar, aviso } = useSesion()
  const [correo, setCorreo] = useState('')
  const [clave, setClave] = useState('')
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)

  if (sesion) return <Navigate to={INICIO_POR_ROL[sesion.rol]} replace />

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setCargando(true)
    setError(null)
    try {
      iniciar(await auth.login(correo.trim(), clave))
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setCargando(false)
    }
  }

  return (
    <Entrada>
        <div>
          <h1 style={{ marginBottom: 6 }}>Iniciar sesion</h1>
          <p className="desc" style={{ marginBottom: 20 }}>Agenda, reprograma o cancela tus citas, o entra a tu espacio de trabajo.</p>
          {aviso && <Aviso tipo="aviso">{aviso}</Aviso>}
          {error && <Aviso tipo="error">{error}</Aviso>}
          <form className="form" onSubmit={enviar} noValidate>
            <Campo id="correo" etiqueta="Correo" type="email" autoComplete="username" value={correo} onChange={(e) => setCorreo(e.target.value)} required autoFocus />
            <Campo id="clave" etiqueta="Clave" type="password" autoComplete="current-password" value={clave} onChange={(e) => setClave(e.target.value)} required />
            <Boton type="submit" cargando={cargando} disabled={!correo || !clave}>Entrar</Boton>
          </form>
        </div>
        <p className="ayuda" style={{ marginTop: 20 }}>Eres paciente y no tienes cuenta? <Link to="/registro">Crea la tuya</Link></p>
    </Entrada>
  )
}
