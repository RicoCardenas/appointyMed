import { CalendarCheck, FileHeart, Smartphone, Stethoscope } from 'lucide-react'
import type { ReactNode } from 'react'

const PUNTOS = [
  { icono: CalendarCheck, texto: 'Eliges una franja libre y nadie mas puede tomarla al mismo tiempo.' },
  { icono: Smartphone, texto: 'Reprogramas o cancelas desde el celular, cuando lo necesites.' },
  { icono: FileHeart, texto: 'Tu historia clinica la ves solo tu y los profesionales que te atienden.' },
]

/** Marco de las pantallas sin sesion: panel de marca a la izquierda (compacto en movil) y formulario a la derecha. */
export function Entrada({ children, ancho }: { children: ReactNode; ancho?: number }) {
  return (
    <main className="entrada">
      <aside className="entrada-marca">
        <div className="marca marca-clara"><span className="marca-icono" aria-hidden><Stethoscope size={16} /></span>Appointy Med</div>
        <div className="entrada-mensaje">
          <p className="entrada-titulo">Tu agenda medica, en un solo lugar.</p>
          <ul>
            {PUNTOS.map(({ icono: Icono, texto }) => (
              <li key={texto}><Icono size={20} aria-hidden /><span>{texto}</span></li>
            ))}
          </ul>
        </div>
      </aside>
      <section className="entrada-form">
        <div className="entrada-caja" style={ancho ? { width: `min(${ancho}px, 100%)` } : undefined}>{children}</div>
      </section>
    </main>
  )
}
