import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import App from '../App'
import { Paginador } from '../componentes/ui'
import { ToastProvider } from '../componentes/Toasts'
import { SesionProvider } from '../sesion/Sesion'

function json(estado: number, cuerpo: unknown) {
  return { ok: estado < 400, status: estado, json: async () => cuerpo }
}

function montar(ruta = '/') {
  return render(<MemoryRouter initialEntries={[ruta]}><SesionProvider><ToastProvider><App /></ToastProvider></SesionProvider></MemoryRouter>)
}

beforeEach(() => sessionStorage.clear())
afterEach(() => vi.unstubAllGlobals())

describe('Login', () => {
  it('sin sesion cualquier ruta privada lleva al login', () => {
    montar('/admin/citas')
    expect(screen.getByRole('heading', { name: 'Iniciar sesion' })).toBeInTheDocument()
  })

  it('un login correcto de paciente entra a reservar', async () => {
    const f = vi.fn(async (url: string) => {
      if (url.endsWith('/auth/login')) return json(200, { accessToken: 't', tokenType: 'Bearer', usuarioId: 'u1', rol: 'PACIENTE', expiraEn: new Date(Date.now() + 900_000).toISOString() })
      if (url.includes('/especialidades')) return json(200, [{ id: 'e1', nombre: 'Cardiologia', activo: true }])
      return json(200, [])
    })
    vi.stubGlobal('fetch', f)
    montar('/login')
    await userEvent.type(screen.getByLabelText('Correo'), 'ana@correo.co')
    await userEvent.type(screen.getByLabelText('Clave'), 'Clave1234')
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }))
    expect(await screen.findByRole('heading', { name: 'Reservar una cita' })).toBeInTheDocument()
    expect(await screen.findByRole('option', { name: 'Cardiologia' })).toBeInTheDocument()
    expect(screen.getByRole('link', { name: /Mis citas/ })).toBeInTheDocument()
    expect(screen.queryByRole('link', { name: /Cuentas/ })).not.toBeInTheDocument()
  })

  it('credenciales malas muestran el mensaje y no entran', async () => {
    vi.stubGlobal('fetch', vi.fn(async () => json(401, { codigo: 'CREDENCIALES_INVALIDAS', mensaje: 'Correo o clave incorrectos' })))
    montar('/login')
    await userEvent.type(screen.getByLabelText('Correo'), 'ana@correo.co')
    await userEvent.type(screen.getByLabelText('Clave'), 'mala')
    await userEvent.click(screen.getByRole('button', { name: 'Entrar' }))
    expect(await screen.findByRole('alert')).toHaveTextContent('no coinciden')
    expect(screen.getByRole('heading', { name: 'Iniciar sesion' })).toBeInTheDocument()
  })
})

describe('Rutas por rol', () => {
  it('un paciente que entra a una ruta de admin vuelve a su inicio', async () => {
    sessionStorage.setItem('appointy.sesion', JSON.stringify({ token: 't', usuarioId: 'u1', rol: 'PACIENTE', expiraEn: Date.now() + 600_000 }))
    vi.stubGlobal('fetch', vi.fn(async () => json(200, [])))
    montar('/admin/cuentas')
    expect(await screen.findByRole('heading', { name: 'Reservar una cita' })).toBeInTheDocument()
  })

  it('el administrativo ve su navegacion y la tabla paginada', async () => {
    sessionStorage.setItem('appointy.sesion', JSON.stringify({ token: 't', usuarioId: 'a1', rol: 'ADMINISTRATIVO', expiraEn: Date.now() + 600_000 }))
    vi.stubGlobal('fetch', vi.fn(async (url: string) => {
      if (url.includes('/citas/estadisticas')) return json(200, { confirmadas: 3, canceladas: 1, completadas: 2, noAsistidas: 0 })
      if (url.includes('/citas')) return json(200, { contenido: [{ id: 'c1', pacienteId: 'aaaaaaaa-1', profesionalId: 'p1', especialidadId: 'e1', centroId: 'c1', franjaId: 'f', estado: 'CONFIRMADA', fecha: '2026-12-01', horaInicio: '08:00', horaFin: '08:30', creadoEn: '', modificadoEn: '' }], pagina: 0, tamano: 10, totalElementos: 1, totalPaginas: 1 })
      return json(404, { codigo: 'NO', mensaje: 'x' })
    }))
    montar('/admin/citas')
    expect(await screen.findByRole('heading', { name: 'Citas' })).toBeInTheDocument()
    await waitFor(() => expect(screen.getByText('Confirmada', { selector: 'span' })).toBeInTheDocument())
    expect(screen.getByRole('link', { name: /Notificaciones/ })).toBeInTheDocument()
    expect(screen.getByText('1 resultado')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: 'Cancelar' })).toBeInTheDocument()
  })
})

describe('Paginador', () => {
  it('deshabilita los extremos y avisa el cambio', async () => {
    const cambiar = vi.fn()
    const { rerender } = render(<Paginador pagina={0} totalPaginas={3} total={25} alCambiar={cambiar} />)
    expect(screen.getByRole('button', { name: 'Anterior' })).toBeDisabled()
    await userEvent.click(screen.getByRole('button', { name: 'Siguiente' }))
    expect(cambiar).toHaveBeenCalledWith(1)
    rerender(<Paginador pagina={2} totalPaginas={3} total={25} alCambiar={cambiar} />)
    expect(screen.getByRole('button', { name: 'Siguiente' })).toBeDisabled()
    expect(screen.getByText('Pagina 3 de 3')).toBeInTheDocument()
  })
})
