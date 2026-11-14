import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'

interface Toast { id: number; texto: string; tipo: 'exito' | 'error' }
const Ctx = createContext<(texto: string, tipo?: Toast['tipo']) => void>(() => {})

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  const avisar = useCallback((texto: string, tipo: Toast['tipo'] = 'exito') => {
    const id = Date.now() + Math.random()
    setToasts((t) => [...t, { id, texto, tipo }])
    setTimeout(() => setToasts((t) => t.filter((x) => x.id !== id)), 5000)
  }, [])
  return (
    <Ctx.Provider value={avisar}>
      {children}
      <div className="toasts" aria-live="polite">
        {toasts.map((t) => <div key={t.id} className={`toast toast-${t.tipo}`}>{t.texto}</div>)}
      </div>
    </Ctx.Provider>
  )
}

export const useToast = () => useContext(Ctx)
