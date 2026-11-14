import { describe, expect, it } from 'vitest'
import { aFechaLocal, fechaLarga, hoyIso, idCorto, instante, mensajeDeError, sumarDias } from '../util/formato'

describe('fechas', () => {
  it('una fecha ISO no se corre un dia por la zona', () => {
    const d = aFechaLocal('2026-12-01')
    expect([d.getFullYear(), d.getMonth(), d.getDate()]).toEqual([2026, 11, 1])
    expect(fechaLarga('2026-12-01')).toContain('1')
    expect(fechaLarga(null)).toBe('sin fecha')
  })
  it('suma dias cruzando mes y anio', () => {
    expect(sumarDias('2026-12-31', 1)).toBe('2027-01-01')
    expect(sumarDias('2026-03-01', -1)).toBe('2026-02-28')
  })
  it('hoy tiene formato ISO', () => expect(hoyIso()).toMatch(/^\d{4}-\d{2}-\d{2}$/))
  it('los instantes se muestran en hora de Bogota', () => {
    expect(instante('2026-11-20T13:00:00Z')).toMatch(/8:00|08:00/)
    expect(instante(null)).toBe('sin registro')
  })
  it('id corto', () => expect(idCorto('12345678-aaaa')).toBe('12345678'))
})

describe('mensajeDeError', () => {
  it('usa el texto propio para los codigos conocidos', () => {
    expect(mensajeDeError({ codigo: 'FRANJA_NO_DISPONIBLE', message: 'x' })).toContain('Elige otra')
  })
  it('para el resto usa el mensaje del servidor y su detalle', () => {
    expect(mensajeDeError({ codigo: 'OTRO', message: 'Hay datos invalidos', detalle: ['clave: corta'] })).toBe('Hay datos invalidos (clave: corta)')
    expect(mensajeDeError(null)).toBe('Ocurrio un error inesperado')
  })
})
