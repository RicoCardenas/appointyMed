import { useEffect, useRef, useState, type FormEvent } from 'react'
import { clinica } from '../api/servicios'
import type { Diagnostico, Entrada } from '../api/tipos'
import { mensajeDeError } from '../util/formato'
import { Aviso, AreaTexto, Boton, Campo } from './ui'

interface Valores { motivo: string; diagnostico: string; notas: string; indicaciones: string }

/** Diagnostico con busqueda en CIE-10 (API externa). Solo se envia el termino; si la API falla se puede escribir a mano. */
function DiagnosticoCampo({ valor, alCambiar }: { valor: string; alCambiar: (v: string) => void }) {
  const [sugerencias, setSugerencias] = useState<Diagnostico[]>([])
  const [aviso, setAviso] = useState<string | null>(null)
  const [abierto, setAbierto] = useState(false)
  const omitir = useRef(false)

  useEffect(() => {
    if (omitir.current) { omitir.current = false; return }
    const termino = valor.trim()
    if (termino.length < 2) { setSugerencias([]); setAviso(null); return }
    const control = new AbortController()
    const t = setTimeout(() => {
      clinica.diagnosticos(termino, control.signal).then(
        (d) => { setSugerencias(d); setAviso(d.length ? null : 'Sin coincidencias en CIE-10: puedes dejar tu texto.'); setAbierto(true) },
        (e) => { if (!(e instanceof DOMException)) { setSugerencias([]); setAviso(mensajeDeError(e)) } },
      )
    }, 350)
    return () => { clearTimeout(t); control.abort() }
  }, [valor])

  return (
    <div style={{ position: 'relative' }}>
      <Campo id="dx" etiqueta="Diagnostico" ayuda={aviso ?? 'Escribe para buscar en el catalogo CIE-10 (en ingles) o redacta el diagnostico.'} value={valor} onChange={(e) => { setAbierto(true); alCambiar(e.target.value) }} required maxLength={5000} autoComplete="off"
        role="combobox" aria-expanded={abierto && sugerencias.length > 0} aria-controls="dx-lista" />
      {abierto && sugerencias.length > 0 && (
        <ul className="sugerencias" id="dx-lista" role="listbox">
          {sugerencias.map((s) => (
            <li key={s.codigo} role="option" aria-selected={false}>
              <button type="button" onClick={() => { omitir.current = true; alCambiar(`${s.codigo} - ${s.descripcion}`); setAbierto(false); setSugerencias([]) }}>
                <span className="codigo">{s.codigo}</span><span>{s.descripcion}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export function AtencionForm({ citaId, entradaACorregir, alGuardar, alCancelar }: {
  citaId?: string; entradaACorregir?: Entrada; alGuardar: (e: Entrada) => void; alCancelar: () => void
}) {
  const [v, setV] = useState<Valores>({
    motivo: entradaACorregir?.motivo ?? '', diagnostico: entradaACorregir?.diagnostico ?? '', notas: entradaACorregir?.notas ?? '', indicaciones: entradaACorregir?.indicaciones ?? '',
  })
  const [guardando, setGuardando] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const poner = (k: keyof Valores) => (e: { target: { value: string } }) => setV((x) => ({ ...x, [k]: e.target.value }))

  async function enviar(e: FormEvent) {
    e.preventDefault()
    setGuardando(true)
    setError(null)
    try {
      const cuerpo = { motivo: v.motivo.trim(), diagnostico: v.diagnostico.trim(), notas: v.notas.trim() || undefined, indicaciones: v.indicaciones.trim() || undefined }
      const guardada = entradaACorregir ? await clinica.corregir(entradaACorregir.id, cuerpo) : await clinica.registrar({ citaId: citaId!, ...cuerpo })
      alGuardar(guardada)
    } catch (err) {
      setError(mensajeDeError(err))
    } finally {
      setGuardando(false)
    }
  }

  return (
    <form className="form" style={{ maxWidth: 640, flexBasis: '100%' }} onSubmit={enviar}>
      <p className="ayuda">{entradaACorregir ? 'La correccion crea una entrada nueva; la original se conserva en la historia.' : 'La atencion no se puede borrar ni editar: si hay un error, se corrige con una entrada nueva.'}</p>
      {error && <Aviso tipo="error">{error}</Aviso>}
      <Campo id="motivo" etiqueta="Motivo de consulta" value={v.motivo} onChange={poner('motivo')} required maxLength={5000} />
      <DiagnosticoCampo valor={v.diagnostico} alCambiar={(d) => setV((x) => ({ ...x, diagnostico: d }))} />
      <AreaTexto id="notas" etiqueta="Notas (opcional)" value={v.notas} onChange={poner('notas')} maxLength={5000} />
      <AreaTexto id="ind" etiqueta="Indicaciones (opcional)" value={v.indicaciones} onChange={poner('indicaciones')} maxLength={5000} />
      <div className="acciones">
        <Boton type="submit" cargando={guardando} disabled={!v.motivo.trim() || !v.diagnostico.trim()}>{entradaACorregir ? 'Guardar correccion' : 'Registrar atencion'}</Boton>
        <Boton type="button" variante="fantasma" onClick={alCancelar}>Cancelar</Boton>
      </div>
    </form>
  )
}
