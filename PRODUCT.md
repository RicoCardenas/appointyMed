# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Stack
React 19 + Vite 8 + TypeScript, sin librería de componentes (CSS propio con tokens). Decidido con el equipo al iniciar la Fase 7 (propuesta aceptada). El front habla con los 5 servicios a través de un proxy por prefijo (`/svc/<servicio>`), sin CORS.

## Users
- **Paciente**: persona que agenda, reprograma y cancela citas desde el celular o el computador, y lee su propia historia clínica. Entra pocas veces y quiere terminar rápido.
- **Profesional de la salud**: consulta su agenda del día, marca citas como atendidas, registra la atención (motivo, diagnóstico CIE-10, notas) y lee la historia de pacientes con los que tuvo cita. Trabaja entre consulta y consulta, con prisa.
- **Administrativo**: gestiona cuentas, catálogo (especialidades, centros, profesionales, horarios, parámetros), supervisa citas y notificaciones. Usa tablas y filtros todo el día.

## Product Purpose
Appointy Med es un sistema de gestión de agenda médica: el paciente reserva sin dobles reservas, el profesional ve su agenda e historia clínica, el administrativo mantiene el catálogo y los horarios. Éxito: reservar una cita en menos de un minuto y que nadie vea datos que no le corresponden.

## Positioning
Las reglas del servicio están en el sistema, no en la pantalla: una franja ocupada se rechaza con 409 aunque dos personas la pidan a la vez, y cada rol ve solo lo suyo (RNF-02, RNF-05). La interfaz lo muestra con claridad en vez de esconderlo.

## Operating Context
- Proyecto académico (profundización) con especificación del docente en `docs/especificacion.md`; el back son 5 microservicios Spring Boot con JWT de 15 minutos y sin refresh token.
- La zona horaria de la clínica es America/Bogota; las fechas y horas de franjas llegan en hora local de la clínica.
- Los endpoints devuelven errores con `codigo`, `mensaje`, `detalle` y `timestamp`; el front los traduce a mensajes que nombran el problema y la salida.
- Algunos datos no existen en la API y no se inventan: el profesional no ve el nombre del paciente (solo su id), y los centros se listan solo para el administrativo.

## Capabilities and Constraints
- Sesión de 15 minutos: al vencer, el front vuelve al login avisando (no hay refresh).
- Paginación de 0 en 0 (`page`, `size` hasta 100) en los listados de administración.
- Sin datos clínicos en logs ni en URLs; el front no guarda historia clínica fuera de memoria.
- Terminología: cita, franja, centro, especialidad, profesional, atención, historia clínica, administrativo.

## Brand Commitments
Nombre: Appointy Med. Voz: español neutro de Colombia, directo y respetuoso, sin emojis.

## Evidence on Hand
Sin logo, testimonios ni imágenes. No se inventan cifras, clínicas ni profesionales: los datos salen de la API.

## Product Principles
1. La tarea primero: la interfaz desaparece detrás de lo que la persona vino a hacer.
2. Un error nombra el problema y la salida; un 409 de franja ocupada ofrece otras franjas, no un aviso rojo mudo.
3. Cada rol ve solo su mundo: navegación, rutas y acciones cambian con el rol.
4. Lo clínico se trata con cuidado: lectura sin ruido, sin copiar a lugares inesperados, sin guardarlo en el navegador.
5. Si la API no da un dato, se dice y no se inventa.

## Accessibility & Inclusion
Teclado completo, foco visible, contraste 4.5:1, objetivos táctiles de al menos 44 px en móvil, `prefers-reduced-motion` respetado, estados que no dependen solo del color. Personas mayores y poca experiencia digital entre los pacientes.
