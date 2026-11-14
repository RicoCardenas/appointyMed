import { Stethoscope } from 'lucide-react'
import { useState, type FormEvent } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { auth } from '../../api/servicios'
import { Aviso, Boton, Campo } from '../../componentes/ui'
import { useToast } from '../../componentes/Toasts'
import { hoyIso, mensajeDeError } from '../../util/formato'

const VACIO = { correo: '', clave: '', cedula: '', nombres: '', apellidos: '', fechaNacimiento: '', telefono: '', direccion: '', ciudad: '' }

export function Registro() {
  const [f, setF] = useState(VACIO)
  const [cargando, setCargando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const navegar = useNavigate()
  const avisar = useToast()
  const poner = (k: keyof typeof VACIO) => (e: { target: { value: string } }) => setF((x) => ({ ...x, [k]: e.target.value }))

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setCargando(true)
    setError(null)
    try {
      const cuerpo: Record<string, string> = { ...f, correo: f.correo.trim() }
      for (const k of ['telefono', 'direccion', 'ciudad']) if (!cuerpo[k]) delete cuerpo[k]
      await auth.registrar(cuerpo)
      avisar('Cuenta creada. Ya puedes iniciar sesion.')
      navegar('/login')
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setCargando(false)
    }
  }

  return (
    <main className="entrada">
      <div className="entrada-caja" style={{ width: 'min(620px, 100%)' }}>
        <div className="marca"><span className="marca-icono" aria-hidden><Stethoscope size={16} /></span>Appointy Med</div>
        <div className="panel">
          <h1 style={{ marginBottom: 6 }}>Crear cuenta de paciente</h1>
          <p className="desc" style={{ marginBottom: 20 }}>La cedula, los nombres y la fecha de nacimiento no se pueden cambiar despues: solo el personal administrativo puede corregirlos.</p>
          {error && <Aviso tipo="error">{error}</Aviso>}
          <form className="form" style={{ maxWidth: 'none' }} onSubmit={enviar}>
            <div className="rejilla">
              <Campo id="nombres" etiqueta="Nombres" value={f.nombres} onChange={poner('nombres')} required maxLength={100} autoComplete="given-name" />
              <Campo id="apellidos" etiqueta="Apellidos" value={f.apellidos} onChange={poner('apellidos')} required maxLength={100} autoComplete="family-name" />
              <Campo id="cedula" etiqueta="Cedula" ayuda="De 5 a 20 letras, numeros o guiones." value={f.cedula} onChange={poner('cedula')} required pattern="[0-9A-Za-z\-]{5,20}" />
              <Campo id="nacimiento" etiqueta="Fecha de nacimiento" type="date" max={hoyIso()} value={f.fechaNacimiento} onChange={poner('fechaNacimiento')} required autoComplete="bday" />
              <Campo id="correo" etiqueta="Correo" type="email" value={f.correo} onChange={poner('correo')} required maxLength={150} autoComplete="email" />
              <Campo id="clave" etiqueta="Clave" type="password" ayuda="Al menos 8 caracteres, con una letra y un numero." value={f.clave} onChange={poner('clave')} required minLength={8} maxLength={72} autoComplete="new-password" />
              <Campo id="telefono" etiqueta="Telefono (opcional)" type="tel" value={f.telefono} onChange={poner('telefono')} pattern="[0-9+() \-]{7,20}" autoComplete="tel" />
              <Campo id="ciudad" etiqueta="Ciudad (opcional)" value={f.ciudad} onChange={poner('ciudad')} maxLength={100} autoComplete="address-level2" />
            </div>
            <Campo id="direccion" etiqueta="Direccion (opcional)" value={f.direccion} onChange={poner('direccion')} maxLength={200} autoComplete="street-address" />
            <div className="acciones"><Boton type="submit" cargando={cargando}>Crear cuenta</Boton><Link to="/login">Ya tengo cuenta</Link></div>
          </form>
        </div>
      </div>
    </main>
  )
}
