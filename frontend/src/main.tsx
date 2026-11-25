import '@fontsource-variable/figtree'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { calentarServicios } from './api/http'
import App from './App'
import { ToastProvider } from './componentes/Toasts'
import { SesionProvider } from './sesion/Sesion'
import './estilos.css'

calentarServicios()

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
