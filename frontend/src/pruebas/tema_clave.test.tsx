import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it } from 'vitest'
import { TemaBoton } from '../componentes/TemaBoton'
import { Campo } from '../componentes/ui'
import { aplicarTema, iniciarTema, temaActual } from '../util/tema'

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
  it('el boton alterna el modo, lo aplica al documento y lo guarda', async () => {
    aplicarTema('claro')
    render(<TemaBoton />)
    await userEvent.click(screen.getByRole('button', { name: 'Cambiar a modo oscuro' }))
    expect(document.documentElement.dataset.tema).toBe('oscuro')
    expect(localStorage.getItem('appointy.tema')).toBe('oscuro')
    await userEvent.click(screen.getByRole('button', { name: 'Cambiar a modo claro' }))
    expect(temaActual()).toBe('claro')
  })

  it('iniciarTema recupera la eleccion guardada', () => {
    localStorage.setItem('appointy.tema', 'oscuro')
    iniciarTema()
    expect(document.documentElement.dataset.tema).toBe('oscuro')
  })
})
