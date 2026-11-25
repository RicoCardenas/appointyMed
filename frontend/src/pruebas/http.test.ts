import { afterEach, describe, expect, it, vi } from 'vitest'
import { ApiError, calentarServicios, configurarSesion, construirQuery, llamar } from '../api/http'

function responder(estado: number, cuerpo?: unknown) {
  return vi.fn().mockResolvedValue({ ok: estado >= 200 && estado < 300, status: estado, json: async () => cuerpo })
}

afterEach(() => { vi.unstubAllGlobals(); configurarSesion(() => null, () => {}) })

describe('construirQuery', () => {
  it('omite vacios y codifica', () => {
    expect(construirQuery({ a: 'x y', b: undefined, c: '', d: 0, e: false })).toBe('?a=x+y&d=0&e=false')
    expect(construirQuery()).toBe('')
    expect(construirQuery({ a: undefined })).toBe('')
  })
})

describe('llamar', () => {
  it('arma la ruta por servicio, con token y correlacion', async () => {
    const f = responder(200, { ok: true })
    vi.stubGlobal('fetch', f)
    configurarSesion(() => 'tok', () => {})
    await llamar('catalog', '/profesionales', { query: { page: 1 } })
    const [url, init] = f.mock.calls[0]
    expect(url).toBe('/svc/catalog/api/v1/profesionales?page=1')
    expect(init.headers.Authorization).toBe('Bearer tok')
    expect(init.headers['X-Correlation-Id']).toMatch(/[0-9a-f-]{36}/)
    expect(init.method).toBe('GET')
  })

  it('envia el cuerpo como JSON y no pone Authorization sin sesion', async () => {
    const f = responder(201, { id: 1 })
    vi.stubGlobal('fetch', f)
    await llamar('auth', '/auth/login', { metodo: 'POST', cuerpo: { correo: 'a' } })
    const init = f.mock.calls[0][1]
    expect(init.body).toBe('{"correo":"a"}')
    expect(init.headers['Content-Type']).toBe('application/json')
    expect(init.headers.Authorization).toBeUndefined()
  })

  it('204 devuelve undefined', async () => {
    vi.stubGlobal('fetch', responder(204))
    expect(await llamar('auth', '/auth/logout', { metodo: 'POST' })).toBeUndefined()
  })

  it('traduce el error uniforme del backend', async () => {
    vi.stubGlobal('fetch', responder(409, { codigo: 'FRANJA_NO_DISPONIBLE', mensaje: 'ocupada', detalle: ['x'] }))
    await expect(llamar('appointment', '/citas', { metodo: 'POST', cuerpo: {} })).rejects.toMatchObject({ estado: 409, codigo: 'FRANJA_NO_DISPONIBLE', detalle: ['x'] })
  })

  it('un cuerpo ilegible en un error igual produce ApiError', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({ ok: false, status: 500, json: async () => { throw new Error('no json') } }))
    const e = (await llamar('auth', '/x').catch((x) => x)) as ApiError
    expect(e).toBeInstanceOf(ApiError)
    expect(e.codigo).toBe('HTTP_500')
  })

  it('un 401 NO_AUTENTICADO con sesion avisa para cerrarla', async () => {
    const expirar = vi.fn()
    configurarSesion(() => 'tok', expirar)
    vi.stubGlobal('fetch', responder(401, { codigo: 'NO_AUTENTICADO', mensaje: 'x' }))
    await expect(llamar('auth', '/pacientes/me')).rejects.toBeInstanceOf(ApiError)
    expect(expirar).toHaveBeenCalledOnce()
  })

  it('un 401 de credenciales en el login no cierra nada', async () => {
    const expirar = vi.fn()
    configurarSesion(() => null, expirar)
    vi.stubGlobal('fetch', responder(401, { codigo: 'CREDENCIALES_INVALIDAS', mensaje: 'x' }))
    await expect(llamar('auth', '/auth/login', { metodo: 'POST', cuerpo: {} })).rejects.toMatchObject({ codigo: 'CREDENCIALES_INVALIDAS' })
    expect(expirar).not.toHaveBeenCalled()
  })

  it('sin red es SIN_CONEXION', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('fallo')))
    await expect(llamar('auth', '/x')).rejects.toMatchObject({ estado: 0, codigo: 'SIN_CONEXION' })
  })
})

describe('servicio dormido (Render gratuito)', () => {
  const sinJson = (estado: number) => vi.fn().mockResolvedValue({ ok: false, status: estado, json: async () => { throw new Error('no es JSON') } })

  it.each([502, 503, 504])('un %i sin error uniforme se informa como servicio despertando', async (estado) => {
    vi.stubGlobal('fetch', sinJson(estado))
    const error = (await llamar('auth', '/auth/login', { metodo: 'POST', cuerpo: {} }).catch((e) => e)) as ApiError
    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({ estado, codigo: 'SERVICIO_DESPERTANDO' })
    expect(error.message).toMatch(/despertando/i)
  })

  it('un 503 con error uniforme del backend conserva su codigo y mensaje', async () => {
    vi.stubGlobal('fetch', responder(503, { codigo: 'APPOINTMENT_NO_DISPONIBLE', mensaje: 'appointment no respondio' }))
    await expect(llamar('catalog', '/x')).rejects.toMatchObject({ estado: 503, codigo: 'APPOINTMENT_NO_DISPONIBLE', message: 'appointment no respondio' })
  })

  it('un 500 sin JSON sigue siendo un error inesperado', async () => {
    vi.stubGlobal('fetch', sinJson(500))
    await expect(llamar('auth', '/x')).rejects.toMatchObject({ estado: 500, codigo: 'HTTP_500' })
  })
})

describe('calentarServicios', () => {
  it('pide la salud de los 5 servicios sin esperar y sin fallar si alguno no responde', async () => {
    const f = vi.fn().mockRejectedValue(new Error('dormido'))
    vi.stubGlobal('fetch', f)
    expect(() => calentarServicios()).not.toThrow()
    await Promise.resolve()
    const urls = f.mock.calls.map((c) => c[0]).sort()
    expect(urls).toEqual(['appointment', 'auth', 'catalog', 'clinical', 'notification'].map((s) => `/svc/${s}/actuator/health`).sort())
  })
})
