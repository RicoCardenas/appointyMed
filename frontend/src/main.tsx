import '@fontsource-variable/figtree'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import App from './App'
import { ToastProvider } from './componentes/Toasts'
import { SesionProvider } from './sesion/Sesion'
import './estilos.css'

createRoot(document.getElementById('raiz')!).render(
  <StrictMode>
    <BrowserRouter>
      <SesionProvider>
        <ToastProvider>
          <App />
        </ToastProvider>
      </SesionProvider>
    </BrowserRouter>
  </StrictMode>,
)
