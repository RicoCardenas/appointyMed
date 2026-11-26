import { Eye, EyeOff, Loader2 } from 'lucide-react'
import { useEffect, useRef, useState, type ButtonHTMLAttributes, type InputHTMLAttributes, type ReactNode, type SelectHTMLAttributes, type TextareaHTMLAttributes } from 'react'
import { mensajeDeError } from '../util/formato'

type Variante = 'primario' | 'secundario' | 'fantasma' | 'peligro'

export function Boton({ variante = 'primario', cargando, children, disabled, className, ...r }: ButtonHTMLAttributes<HTMLButtonElement> & { variante?: Variante; cargando?: boolean }) {
  return (
    <button className={`btn btn-${variante}${className ? ` ${className}` : ''}`} disabled={disabled || cargando} aria-busy={cargando || undefined} {...r}>
      {cargando && <Loader2 className="girar" size={16} aria-hidden />}
      {children}
    </button>
  )
}

interface CampoBase { etiqueta: string; ayuda?: string; error?: string | null }

function Envoltura({ etiqueta, ayuda, error, id, children }: CampoBase & { id: string; children: ReactNode }) {
  return (
    <div className="campo">
      <label htmlFor={id}>{etiqueta}</label>
      {children}
      {ayuda && !error && <p className="ayuda" id={`${id}-ayuda`}>{ayuda}</p>}
      {error && <p className="error-campo" id={`${id}-error`} role="alert">{error}</p>}
    </div>
  )
}

export function Campo({ etiqueta, ayuda, error, id, type, ...r }: CampoBase & InputHTMLAttributes<HTMLInputElement> & { id: string }) {
  const [visible, setVisible] = useState(false)
  const esClave = type === 'password'
  const input = (
    <input id={id} type={esClave && visible ? 'text' : type} aria-invalid={!!error || undefined} aria-describedby={error ? `${id}-error` : ayuda ? `${id}-ayuda` : undefined} {...r} />
  )
  return (
    <Envoltura etiqueta={etiqueta} ayuda={ayuda} error={error} id={id}>
      {esClave ? (
        <div className="campo-clave">
          {input}
          <button type="button" className="ver-clave" aria-label={`Mostrar ${etiqueta.toLowerCase()}`} aria-pressed={visible} onClick={() => setVisible((v) => !v)}>
            {visible ? <EyeOff size={18} aria-hidden /> : <Eye size={18} aria-hidden />}
          </button>
        </div>
      ) : input}
    </Envoltura>
  )
}

export function AreaTexto({ etiqueta, ayuda, error, id, ...r }: CampoBase & TextareaHTMLAttributes<HTMLTextAreaElement> & { id: string }) {
  return (
    <Envoltura etiqueta={etiqueta} ayuda={ayuda} error={error} id={id}>
      <textarea id={id} rows={3} aria-invalid={!!error || undefined} {...r} />
    </Envoltura>
  )
}

export function Seleccion({ etiqueta, ayuda, error, id, children, ...r }: CampoBase & SelectHTMLAttributes<HTMLSelectElement> & { id: string }) {
  return (
    <Envoltura etiqueta={etiqueta} ayuda={ayuda} error={error} id={id}>
      <select id={id} aria-invalid={!!error || undefined} {...r}>{children}</select>
    </Envoltura>
  )
}

export function Aviso({ tipo = 'info', children, accion }: { tipo?: 'info' | 'exito' | 'aviso' | 'error'; children: ReactNode; accion?: ReactNode }) {
  return (
    <div className={`aviso aviso-${tipo}`} role={tipo === 'error' ? 'alert' : 'status'}>
      <div>{children}</div>
      {accion}
    </div>
  )
}

export function ErrorAviso({ error, alReintentar }: { error: unknown; alReintentar?: () => void }) {
  return (
    <Aviso tipo="error" accion={alReintentar && <Boton variante="secundario" onClick={alReintentar}>Reintentar</Boton>}>
      {mensajeDeError(error)}
    </Aviso>
  )
}

interface Cifra { clave: string; n: ReactNode; texto: string }

/** Franja de cifras. Con `alElegir` cada cifra es un filtro (boton con aria-pressed); sin el, solo informa. */
export function Cifras({ items, etiqueta, activa, alElegir }: { items: Cifra[]; etiqueta?: string; activa?: (clave: string) => boolean; alElegir?: (clave: string) => void }) {
  return (
    <div className="cifras" role={alElegir ? 'group' : undefined} aria-label={alElegir ? etiqueta : undefined}>
      {items.map(({ clave, n, texto }) => alElegir ? (
        <button key={clave} type="button" className="cifra" aria-pressed={activa?.(clave) ?? false} onClick={() => alElegir(clave)}><strong>{n}</strong><span>{texto}</span></button>
      ) : (
        <div key={clave} className="cifra"><strong>{n}</strong><span>{texto}</span></div>
      ))}
    </div>
  )
}

export function Insignia({ tono = 'neutro', children }: { tono?: 'neutro' | 'exito' | 'aviso' | 'peligro' | 'acento'; children: ReactNode }) {
  return <span className={`insignia insignia-${tono}`}>{children}</span>
}

export function Esqueleto({ filas = 3 }: { filas?: number }) {
  return (
    <div className="esqueleto" aria-hidden>
      {Array.from({ length: filas }, (_, i) => <div key={i} className="esqueleto-fila" />)}
    </div>
  )
}

export function Vacio({ titulo, children }: { titulo: string; children?: ReactNode }) {
  return (
    <div className="vacio">
      <h3>{titulo}</h3>
      {children && <p>{children}</p>}
    </div>
  )
}

export function Pagina({ titulo, descripcion, acciones, children }: { titulo: string; descripcion?: string; acciones?: ReactNode; children: ReactNode }) {
  return (
    <section className="pagina">
      <header className="pagina-cab">
        <div>
          <h1>{titulo}</h1>
          {descripcion && <p className="desc">{descripcion}</p>}
        </div>
        {acciones && <div className="acciones">{acciones}</div>}
      </header>
      {children}
    </section>
  )
}

export function Paginador({ pagina, totalPaginas, total, alCambiar }: { pagina: number; totalPaginas: number; total: number; alCambiar: (p: number) => void }) {
  return (
    <nav className="paginador" aria-label="Paginacion">
      <span className="ayuda">{total} {total === 1 ? 'resultado' : 'resultados'}</span>
      <div className="acciones">
        <Boton variante="secundario" disabled={pagina <= 0} onClick={() => alCambiar(pagina - 1)}>Anterior</Boton>
        <span className="paginador-pos">Pagina {totalPaginas === 0 ? 0 : pagina + 1} de {totalPaginas}</span>
        <Boton variante="secundario" disabled={pagina + 1 >= totalPaginas} onClick={() => alCambiar(pagina + 1)}>Siguiente</Boton>
      </div>
    </nav>
  )
}

/** Dialogo nativo: solo para confirmaciones destructivas. Cierra con Escape y devuelve el foco. */
export function Confirmar({ abierto, titulo, texto, textoAccion, peligro, cargando, alConfirmar, alCancelar }: {
  abierto: boolean; titulo: string; texto: ReactNode; textoAccion: string; peligro?: boolean; cargando?: boolean; alConfirmar: () => void; alCancelar: () => void
}) {
  const ref = useRef<HTMLDialogElement>(null)
  useEffect(() => {
    const d = ref.current
    if (!d) return
    if (abierto && !d.open) d.showModal()
    if (!abierto && d.open) d.close()
  }, [abierto])
  return (
    <dialog ref={ref} className="dialogo" onCancel={(e) => { e.preventDefault(); alCancelar() }} aria-labelledby="dialogo-titulo">
      <h2 id="dialogo-titulo">{titulo}</h2>
      <div className="dialogo-texto">{texto}</div>
      <div className="acciones fin">
        <Boton variante="secundario" onClick={alCancelar}>Volver</Boton>
        <Boton variante={peligro ? 'peligro' : 'primario'} cargando={cargando} onClick={alConfirmar}>{textoAccion}</Boton>
      </div>
    </dialog>
  )
}
