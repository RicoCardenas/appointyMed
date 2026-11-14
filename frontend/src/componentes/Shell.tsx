import { Bell, Building2, CalendarDays, CalendarPlus, ClipboardList, FileText, LayoutList, LogOut, Settings, Stethoscope, User, Users, type LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'
import { Link, NavLink } from 'react-router-dom'
import type { Rol } from '../api/tipos'
import { INICIO_POR_ROL, useSesion } from '../sesion/Sesion'
import { Boton } from './ui'

const NAV: Record<Rol, { a: string; texto: string; Icono: LucideIcon }[]> = {
  PACIENTE: [
    { a: '/reservar', texto: 'Reservar', Icono: CalendarPlus },
    { a: '/mis-citas', texto: 'Mis citas', Icono: CalendarDays },
    { a: '/mi-historia', texto: 'Mi historia', Icono: FileText },
    { a: '/perfil', texto: 'Perfil', Icono: User },
  ],
  PROFESIONAL: [
    { a: '/agenda', texto: 'Agenda', Icono: CalendarDays },
    { a: '/atenciones', texto: 'Atenciones', Icono: ClipboardList },
  ],
  ADMINISTRATIVO: [
    { a: '/admin/citas', texto: 'Citas', Icono: LayoutList },
    { a: '/admin/cuentas', texto: 'Cuentas', Icono: Users },
    { a: '/admin/profesionales', texto: 'Profesionales', Icono: Stethoscope },
    { a: '/admin/catalogo', texto: 'Catalogo', Icono: Building2 },
    { a: '/admin/notificaciones', texto: 'Notificaciones', Icono: Bell },
    { a: '/admin/parametros', texto: 'Parametros', Icono: Settings },
  ],
}

const ROLES: Record<Rol, string> = { PACIENTE: 'Paciente', PROFESIONAL: 'Profesional', ADMINISTRATIVO: 'Administrativo' }

export function Shell({ children }: { children: ReactNode }) {
  const { sesion, cerrar } = useSesion()
  if (!sesion) return null
  return (
    <div className="app">
      <header className="barra">
        <Link className="marca" to={INICIO_POR_ROL[sesion.rol]}>
          <span className="marca-icono" aria-hidden><Stethoscope size={16} /></span>
          Appointy Med
        </Link>
        <div className="usuario">
          <span className="rol">{ROLES[sesion.rol]}</span>
          <Boton variante="fantasma" onClick={() => cerrar()} aria-label="Cerrar sesion"><LogOut size={16} aria-hidden /> Salir</Boton>
        </div>
      </header>
      <aside className="lateral">
        <nav aria-label="Principal">
          {NAV[sesion.rol].map(({ a, texto, Icono }) => (
            <NavLink key={a} to={a} className="enlace-nav">
              <Icono size={18} aria-hidden />
              {texto}
            </NavLink>
          ))}
        </nav>
      </aside>
      <main className="contenido" id="contenido">{children}</main>
    </div>
  )
}
