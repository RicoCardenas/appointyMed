import { Navigate, Route, Routes } from 'react-router-dom'
import type { Rol } from './api/tipos'
import { Shell } from './componentes/Shell'
import { INICIO_POR_ROL, useSesion } from './sesion/Sesion'
import { Login } from './paginas/publicas/Login'
import { Registro } from './paginas/publicas/Registro'
import { Reservar } from './paginas/paciente/Reservar'
import { MisCitas } from './paginas/paciente/MisCitas'
import { MiHistoria } from './paginas/paciente/MiHistoria'
import { Perfil } from './paginas/paciente/Perfil'
import { Agenda } from './paginas/profesional/Agenda'
import { Atenciones } from './paginas/profesional/Atenciones'
import { HistoriaPaciente } from './paginas/profesional/HistoriaPaciente'
import { CitasAdmin } from './paginas/admin/CitasAdmin'
import { Cuentas } from './paginas/admin/Cuentas'
import { Profesionales } from './paginas/admin/Profesionales'
import { Catalogo } from './paginas/admin/Catalogo'
import { NotificacionesAdmin } from './paginas/admin/NotificacionesAdmin'
import { Parametros } from './paginas/admin/Parametros'

function Privada({ rol, children }: { rol: Rol; children: React.ReactNode }) {
  const { sesion } = useSesion()
  if (!sesion) return <Navigate to="/login" replace />
  if (sesion.rol !== rol) return <Navigate to={INICIO_POR_ROL[sesion.rol]} replace />
  return <Shell>{children}</Shell>
}

function Inicio() {
  const { sesion } = useSesion()
  return <Navigate to={sesion ? INICIO_POR_ROL[sesion.rol] : '/login'} replace />
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Inicio />} />
      <Route path="/login" element={<Login />} />
      <Route path="/registro" element={<Registro />} />

      <Route path="/reservar" element={<Privada rol="PACIENTE"><Reservar /></Privada>} />
      <Route path="/mis-citas" element={<Privada rol="PACIENTE"><MisCitas /></Privada>} />
      <Route path="/mi-historia" element={<Privada rol="PACIENTE"><MiHistoria /></Privada>} />
      <Route path="/perfil" element={<Privada rol="PACIENTE"><Perfil /></Privada>} />

      <Route path="/agenda" element={<Privada rol="PROFESIONAL"><Agenda /></Privada>} />
      <Route path="/atenciones" element={<Privada rol="PROFESIONAL"><Atenciones /></Privada>} />
      <Route path="/pacientes/:pacienteId" element={<Privada rol="PROFESIONAL"><HistoriaPaciente /></Privada>} />

      <Route path="/admin/citas" element={<Privada rol="ADMINISTRATIVO"><CitasAdmin /></Privada>} />
      <Route path="/admin/cuentas" element={<Privada rol="ADMINISTRATIVO"><Cuentas /></Privada>} />
      <Route path="/admin/profesionales" element={<Privada rol="ADMINISTRATIVO"><Profesionales /></Privada>} />
      <Route path="/admin/catalogo" element={<Privada rol="ADMINISTRATIVO"><Catalogo /></Privada>} />
      <Route path="/admin/notificaciones" element={<Privada rol="ADMINISTRATIVO"><NotificacionesAdmin /></Privada>} />
      <Route path="/admin/parametros" element={<Privada rol="ADMINISTRATIVO"><Parametros /></Privada>} />

      <Route path="*" element={<Inicio />} />
    </Routes>
  )
}
