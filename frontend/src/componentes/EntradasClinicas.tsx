import type { Entrada, Resumen } from '../api/tipos'
import { instante } from '../util/formato'
import { Cifras, Insignia } from './ui'

export function ResumenCifras({ r }: { r: Resumen }) {
  return (
    <Cifras items={[
      { clave: 'a', n: r.atenciones, texto: r.atenciones === 1 ? 'atencion' : 'atenciones' },
      { clave: 'c', n: r.correcciones, texto: r.correcciones === 1 ? 'correccion' : 'correcciones' },
      { clave: 'u', n: r.ultimaAtencionEn ? instante(r.ultimaAtencionEn) : 'sin registro', texto: 'ultima atencion' },
    ]} />
  )
}

/** Lectura limpia: la historia no se copia a otros lugares ni se guarda en el navegador (solo vive en memoria). */
export function ListaEntradas({ entradas, accion }: { entradas: Entrada[]; accion?: (e: Entrada) => React.ReactNode }) {
  return (
    <div>
      {[...entradas].reverse().map((e) => (
        <article key={e.id} className="entrada-clinica">
          <div style={{ display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap' }}>
            <strong>{instante(e.creadoEn)}</strong>
            {e.corrigeAId && <Insignia tono="aviso">Correccion</Insignia>}
            {accion?.(e)}
          </div>
          <dl>
            <dt>Motivo</dt><dd>{e.motivo}</dd>
            <dt>Diagnostico</dt><dd>{e.diagnostico}</dd>
            {e.notas && <><dt>Notas</dt><dd>{e.notas}</dd></>}
            {e.indicaciones && <><dt>Indicaciones</dt><dd>{e.indicaciones}</dd></>}
          </dl>
        </article>
      ))}
    </div>
  )
}
