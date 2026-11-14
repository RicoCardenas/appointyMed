import { act, render, screen } from '@testing-library/react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { SesionProvider, leerSesionGuardada, useSesion } from '../sesion/Sesion'

function Sonda() {
  const { sesion, aviso, iniciar, cerrar } = useSesion()
  return (
    <div>
      <p data-testid="estado">{sesion ? `${sesion.rol}:${sesion.usuarioId}` : 'sin sesion'}</p>
      <p data-testid="aviso">{aviso ?? ''}</p>
      <button onClick={() => iniciar({ accessToken: 't', tokenType: 'Bearer', usuarioId: 'u1', rol: 'PACIENTE', expiraEn: new Date(Date.now() + 60_000).toISOString() })}>entrar</button>
      <button onClick={() => iniciar({ accessToken: 't', tokenType: 'Bearer', usuarioId: 'u2', rol: 'PROFESIONAL', expiraEn: new Date(Date.now() + 1_000).toISOString() })}>corta</button>
      <button onClick={() => cerrar()}>salir</button>
    </div>
  )
}

beforeEach(() => { sessionStorage.clear(); vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: true, status: 204, json: async () => null })) })
afterEach(() => { vi.useRealTimers(); vi.unstubAllGlobals() })

describe('Sesion', () => {
  it('guarda la sesion, la restaura y la cierra', async () => {
    const { unmount } = render(<SesionProvider><Sonda /></SesionProvider>)
    expect(screen.getByTestId('estado')).toHaveTextContent('sin sesion')
    await act(async () => screen.getByText('entrar').click())
    expect(screen.getByTestId('estado')).toHaveTextContent('PACIENTE:u1')
    expect(leerSesionGuardada()?.usuarioId).toBe('u1')
    unmount()
    render(<SesionProvider><Sonda /></SesionProvider>)
    expect(screen.getByTestId('estado')).toHaveTextContent('PACIENTE:u1')
    await act(async () => screen.getByText('salir').click())
    expect(screen.getByTestId('estado')).toHaveTextContent('sin sesion')
    expect(leerSesionGuardada()).toBeNull()
  })

  it('al vencer el token vuelve al login con un aviso', async () => {
    vi.useFakeTimers()
    render(<SesionProvider><Sonda /></SesionProvider>)
    await act(async () => screen.getByText('corta').click())
    expect(screen.getByTestId('estado')).toHaveTextContent('PROFESIONAL:u2')
    await act(async () => { vi.advanceTimersByTime(1500) })
    expect(screen.getByTestId('estado')).toHaveTextContent('sin sesion')
    expect(screen.getByTestId('aviso')).toHaveTextContent('expiro')
  })

  it('una sesion guardada ya vencida se descarta', () => {
    sessionStorage.setItem('appointy.sesion', JSON.stringify({ token: 't', usuarioId: 'u', rol: 'PACIENTE', expiraEn: Date.now() - 1 }))
    expect(leerSesionGuardada()).toBeNull()
    sessionStorage.setItem('appointy.sesion', 'no es json')
    expect(leerSesionGuardada()).toBeNull()
  })
})
