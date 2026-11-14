/// <reference types="vitest/config" />
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vitest/config'

/** Cada servicio escucha en su puerto; el front los ve como /svc/<servicio> (sin CORS). En produccion lo hace el proxy inverso. */
const puertos: Record<string, number> = { auth: 8081, catalog: 8082, appointment: 8083, clinical: 8084, notification: 8085 }

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: Object.fromEntries(
      Object.entries(puertos).map(([nombre, puerto]) => [
        `/svc/${nombre}`,
        { target: `http://localhost:${puerto}`, changeOrigin: true, rewrite: (ruta: string) => ruta.replace(`/svc/${nombre}`, '') },
      ]),
    ),
  },
  test: { environment: 'jsdom', setupFiles: './src/pruebas/preparar.ts', globals: true, css: false },
})
