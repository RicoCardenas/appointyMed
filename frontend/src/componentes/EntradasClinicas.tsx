import type { Entrada, Resumen } from '../api/tipos'
import { instante } from '../util/formato'
import { Insignia } from './ui'

export function ResumenCifras({ r }: { r: Resumen }) {
  return (
    <div className="cifras">
      <div className="cifra"><strong>{r.atenciones}</strong><span>{r.atenciones === 1 ? 'atencion' : 'atenciones'}</span></div>
      <div className="cifra"><strong>{r.correcciones}</strong><span>{r.correcciones === 1 ? 'correccion' : 'correcciones'}</span></div>
      <div className="cifra"><strong>{r.ultimaAtencionEn ? instante(r.ultimaAtencionEn) : 'sin registro'}</strong><span>ultima atencion</span></div>
    </div>
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
