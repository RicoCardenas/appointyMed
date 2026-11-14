import { render, screen, waitFor } from '@testing-library/react'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { useNombresPacientes } from '../util/useNombresPacientes'

function Sonda({ ids }: { ids: string[] }) {
  const nombre = useNombresPacientes(ids)
  return <ul>{ids.map((i, n) => <li key={n}>{nombre(i)}</li>)}</ul>
}

afterEach(() => vi.unstubAllGlobals())

describe('useNombresPacientes', () => {
  it('pide los nombres en un solo lote y muestra el id corto mientras llegan o si faltan', async () => {
    const f = vi.fn().mockResolvedValue({ ok: true, status: 200, json: async () => [{ usuarioId: 'aaaaaaaa-1111', nombres: 'Ana', apellidos: 'Perez' }] })
    vi.stubGlobal('fetch', f)
    render(<Sonda ids={['aaaaaaaa-1111', 'bbbbbbbb-2222', 'aaaaaaaa-1111']} />)
    expect(screen.getAllByText(/^Paciente /).length).toBeGreaterThan(0)
    await waitFor(() => expect(screen.getAllByText('Ana Perez')).toHaveLength(2))
    expect(screen.getByText('Paciente bbbbbbbb')).toBeInTheDocument()
    expect(f).toHaveBeenCalledOnce()
    expect(JSON.parse(f.mock.calls[0][1].body).ids).toEqual(['aaaaaaaa-1111', 'bbbbbbbb-2222'])
  })

  it('si auth falla no rompe la pantalla', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('sin red')))
    render(<Sonda ids={['cccccccc-3333']} />)
    await waitFor(() => expect(screen.getByText('Paciente cccccccc')).toBeInTheDocument())
  })
})
