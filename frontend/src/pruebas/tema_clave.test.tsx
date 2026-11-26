import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { ToastProvider } from '../componentes/Toasts'
import { Catalogo } from '../paginas/admin/Catalogo'
import { Entrada } from '../componentes/Entrada'
import { TemaBoton } from '../componentes/TemaBoton'
import { Campo, Cifras } from '../componentes/ui'
import { iniciarTema, preferenciaActual } from '../util/tema'

afterEach(() => { delete document.documentElement.dataset.tema; localStorage.clear() })

describe('Campo de clave', () => {
  it('oculta la clave por defecto y alterna con el ojo', async () => {
    render(<Campo id="c" etiqueta="Clave" type="password" />)
    const campo = screen.getByLabelText('Clave')
    const ojo = screen.getByRole('button', { name: 'Mostrar clave' })
    expect(campo).toHaveAttribute('type', 'password')
    expect(ojo).toHaveAttribute('aria-pressed', 'false')
    await userEvent.click(ojo)
    expect(campo).toHaveAttribute('type', 'text')
    expect(ojo).toHaveAttribute('aria-pressed', 'true')
    await userEvent.click(ojo)
    expect(campo).toHaveAttribute('type', 'password')
  })

  it('los campos que no son clave no llevan ojo', () => {
    render(<Campo id="e" etiqueta="Correo" type="email" />)
    expect(screen.queryByRole('button')).not.toBeInTheDocument()
  })
})

describe('Tema', () => {
  it('el boton recorre automatico, claro, oscuro y vuelve a automatico', async () => {
    render(<TemaBoton />)
    await userEvent.click(screen.getByRole('button', { name: 'Tema automatico. Cambiar a claro' }))
    expect(document.documentElement.dataset.tema).toBe('claro')
    await userEvent.click(screen.getByRole('button', { name: 'Tema claro. Cambiar a oscuro' }))
    expect(document.documentElement.dataset.tema).toBe('oscuro')
    expect(localStorage.getItem('appointy.tema')).toBe('oscuro')
    await userEvent.click(screen.getByRole('button', { name: 'Tema oscuro. Cambiar a automatico' }))
    expect(document.documentElement.dataset.tema).toBeUndefined()
    expect(localStorage.getItem('appointy.tema')).toBeNull()
    expect(preferenciaActual()).toBe('auto')
  })

  it('iniciarTema recupera la eleccion guardada', () => {
    localStorage.setItem('appointy.tema', 'oscuro')
    iniciarTema()
    expect(document.documentElement.dataset.tema).toBe('oscuro')
  })
})

describe('Cifras', () => {
  it('sin alElegir solo informa y con alElegir cada cifra es un filtro', async () => {
    const items = [{ clave: 'a', n: 3, texto: 'activas' }, { clave: 'b', n: 1, texto: 'inactivas' }]
    const { rerender } = render(<Cifras items={items} />)
    expect(screen.queryByRole('button')).not.toBeInTheDocument()
    const elegir = vi.fn()
    rerender(<Cifras items={items} etiqueta="Filtrar" activa={(k) => k === 'b'} alElegir={elegir} />)
    expect(screen.getByRole('button', { name: /inactivas/ })).toHaveAttribute('aria-pressed', 'true')
    await userEvent.click(screen.getByRole('button', { name: /^3\s*activas$/ }))
    expect(elegir).toHaveBeenCalledWith('a')
  })
})

describe('Entrada', () => {
  it('muestra el mensaje de marca, el boton de tema y el contenido', () => {
    render(<Entrada><h1>Hola</h1></Entrada>)
    expect(screen.getByRole('heading', { name: 'Hola' })).toBeInTheDocument()
    expect(screen.getByText(/agenda medica/i)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Tema/ })).toBeInTheDocument()
  })
})

describe('Pestanas de Catalogo', () => {
  it('las flechas, Home y End mueven la seleccion y el panel sigue a la pestana', async () => {
    vi.stubGlobal('fetch', vi.fn(async () => ({ ok: true, status: 200, json: async () => [] })))
    render(<ToastProvider><Catalogo /></ToastProvider>)
    const esp = screen.getByRole('tab', { name: 'Especialidades' })
    expect(esp).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByRole('tab', { name: 'Centros' })).toHaveAttribute('tabindex', '-1')
    esp.focus()
    await userEvent.keyboard('{ArrowRight}')
    expect(screen.getByRole('tab', { name: 'Centros' })).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByRole('tabpanel')).toHaveAttribute('aria-labelledby', 'tab-centros')
    await userEvent.keyboard('{End}')
    expect(screen.getByRole('tab', { name: 'Festivos' })).toHaveAttribute('aria-selected', 'true')
    await userEvent.keyboard('{ArrowRight}')
    expect(esp).toHaveAttribute('aria-selected', 'true')
    vi.unstubAllGlobals()
  })
})
