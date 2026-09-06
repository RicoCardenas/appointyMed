# Appointy Med: arquitectura y límites por servicio

Este documento fija responsabilidades, contratos visibles y dependencias. Todo
lo marcado como "propuesto" pertenece a servicios todavía no implementados.

Contrato detallado de eventos y política de pagos:
[`event-contracts.md`](event-contracts.md).

## Reglas transversales

Cada microservicio usa tres capas:

- `domain`: modelos, value objects, reglas de negocio, casos de uso, puertos y
  excepciones.
- `application`: DTOs, mapeos y orquestación de casos de uso.
- `infrastructure`: REST, seguridad, persistencia, mensajería y proveedores.

Dirección de dependencias:

`infrastructure` depende de `application`; `application` depende de `domain`.
`domain` no depende de una capa externa.

Reglas de `domain`:

- No imports de Java, Spring, Jakarta, JPA, AWS ni librerías externas.
- Tipos propios del dominio para fechas, horas, identificadores, listas y
  opcionales.
- Interfaces de repositorio y mensajería dentro de `domain`.
- Conversión entre tipos de borde (`UUID`, `Instant`, `LocalDateTime`, DTOs y
  entidades JPA) fuera de `domain`.
- Ningún microservicio comparte clases, entidades ni base de datos con otro.
- IDs externos viajan como valores simples del contrato y se convierten en el
  borde del servicio receptor.

## Servicios y responsabilidades

| Servicio | Estado | Responsabilidad principal | Puerto |
|---|---|---|---:|
| `auth-service` | Implementado | Identidad, credenciales, JWT, roles, permisos y auditoría | 8081 |
| `appointment-service` | Implementado | Pacientes, médicos, especialidades, horarios y citas | 8082 |
| `notification-service` | MVP implementado | Consume eventos y simula entrega de correo, SMS y push | 8083 |
| `payment-service` | Pendiente | Intenciones de pago, cobros, reembolsos y webhooks | Por definir |

`payment-gateway` será el nombre del puerto/adaptador que conecte
`payment-service` con un proveedor externo. No será el nombre del
microservicio.

## `auth-service`

### Responsabilidad

- Registrar usuarios y almacenar contraseñas cifradas con BCrypt.
- Autenticar usuarios.
- Emitir, rotar, validar y revocar tokens JWT y refresh tokens.
- Cambiar y recuperar contraseñas.
- Administrar roles y permisos.
- Registrar operaciones sensibles en auditoría.
- Publicar solicitudes de notificación relacionadas con identidad.

No administra pacientes, médicos, especialidades ni citas.

### Dependencias

- Base de datos propia `auth_db`.
- JWT de acceso usado por los servicios protegidos.
- SQS para publicar eventos hacia `notification-service`.
- No realiza llamadas directas a `appointment-service`.

### API actual

Base: `/api/v1`

| Método | Ruta | Acceso | Función |
|---|---|---|---|
| `POST` | `/auth/register` | Público | Registrar usuario |
| `POST` | `/auth/login` | Público | Autenticar y emitir tokens |
| `POST` | `/auth/refresh` | Público | Rotar refresh token |
| `POST` | `/auth/logout` | Público | Revocar refresh token |
| `POST` | `/auth/forgot-password` | Público | Solicitar recuperación |
| `POST` | `/auth/reset-password` | Público | Restablecer contraseña |
| `POST` | `/auth/change-password` | Usuario autenticado | Cambiar contraseña propia |
| `GET` | `/users` | `ADMIN`, `DEVELOPER` | Listar usuarios paginados |
| `GET` | `/users/{userId}` | `ADMIN` o propietario | Consultar usuario |
| `POST` | `/users/{userId}/roles` | `ADMIN` | Asignar rol |
| `PUT` | `/users/{userId}/roles` | `ADMIN`, `DEVELOPER` | Reemplazar roles |
| `DELETE` | `/users/{userId}/roles/{roleName}` | `ADMIN` | Revocar rol |
| `GET` | `/users/{userId}/permissions` | `ADMIN` o propietario | Consultar permisos |
| `GET` | `/roles` | `ADMIN` | Listar roles |

### Eventos actuales

Cola actual: `notification-queue`.

Formato actual: envelope v1. Mensajes legacy planos no deben volver a
publicarse. Contrato en [`event-contracts.md`](event-contracts.md).

- `PASSWORD_RESET_REQUESTED`: `userId`, correo, nombre, token y expiración.
- `USER_REGISTERED`: `userId`, correo y nombre. Publisher disponible en
  infraestructura.
- `APPOINTMENT_BOOKED`: publisher disponible en infraestructura; su emisión
  debe permanecer coordinada con el contrato de eventos de appointments.

El flujo usado directamente por auth hoy es `PASSWORD_RESET_REQUESTED`.

## `appointment-service`

### Responsabilidad

- Administrar especialidades y duración de consulta.
- Administrar perfiles de médicos y pacientes.
- Administrar horarios semanales.
- Calcular disponibilidad.
- Reservar, cancelar y reprogramar citas.
- Consultar citas próximas, históricas y por médico.
- Impedir solapamientos en dominio y persistencia.
- Publicar eventos de citas para notificaciones.

No autentica credenciales ni procesa pagos. Valida el JWT recibido en el borde
de infraestructura.

### Dependencias

- Base de datos propia `appointment_db`.
- Secreto/configuración del JWT emitido por `auth-service`.
- SQS para publicar eventos hacia `notification-service`.
- No accede a tablas de `auth_db`.

### API actual

Base: `/api/v1`. Todas las rutas requieren autenticación, salvo documentación,
actuator permitido y recursos estáticos configurados explícitamente.

| Método | Ruta | Acceso | Función |
|---|---|---|---|
| `POST` | `/appointments` | Autenticado | Reservar cita |
| `GET` | `/appointments/{id}` | Autenticado | Consultar cita |
| `POST` | `/appointments/{id}/cancel` | Autenticado | Cancelar cita |
| `POST` | `/appointments/{id}/reschedule` | Autenticado | Reprogramar cita |
| `GET` | `/appointments/patient/{patientId}` | Roles médicos/operativos/paciente | Listar citas del paciente |
| `GET` | `/appointments/patient/{patientId}/upcoming` | Autenticado | Listar próximas citas |
| `GET` | `/appointments/patient/{patientId}/history` | Roles médicos/operativos/paciente | Consultar historial |
| `GET` | `/appointments/doctor/{doctorId}` | Autenticado | Consultar citas del médico |
| `POST` | `/doctors` | `ADMIN`, `DEVELOPER` | Registrar médico |
| `GET` | `/doctors` | Autenticado | Listar médicos, opcionalmente por especialidad |
| `GET` | `/doctors/{id}` | Autenticado | Consultar médico |
| `GET` | `/doctors/user/{userId}` | Autenticado | Consultar médico por usuario de auth |
| `PUT` | `/doctors/{id}/schedules` | `ADMIN`, `DEVELOPER`, `DOCTOR` | Actualizar horario |
| `GET` | `/doctors/{id}/availability` | Autenticado | Consultar disponibilidad |
| `POST` | `/patients` | `ADMIN`, `DEVELOPER`, `RECEPTIONIST`, `CLIENT`, `PATIENT` | Registrar paciente |
| `PUT` | `/patients/{id}` | `ADMIN`, `DEVELOPER`, `RECEPTIONIST`, `CLIENT`, `PATIENT` | Actualizar paciente |
| `GET` | `/patients/{id}` | Autenticado | Consultar paciente |
| `GET` | `/patients/user/{userId}` | Autenticado | Consultar paciente por usuario de auth |
| `GET` | `/patients` | `ADMIN`, `DEVELOPER`, `RECEPTIONIST`, `DOCTOR` | Listar pacientes paginados |
| `POST` | `/specialties` | `ADMIN`, `DEVELOPER` | Crear especialidad |
| `GET` | `/specialties` | Autenticado | Listar especialidades activas |
| `GET` | `/specialties/{id}` | Autenticado | Consultar especialidad |

Los permisos de propietario y relación médico-paciente deben mantenerse como
regla explícita de aplicación/dominio cuando el caso de uso lo requiera; no se
deben confiar únicamente a la anotación del controlador.

### Eventos actuales

Cola actual: `notification-queue`.

Formato actual: envelope v1. Mensajes legacy planos no deben volver a
publicarse. Contrato en [`event-contracts.md`](event-contracts.md).

- `APPOINTMENT_BOOKED`: cita, paciente, correo, médico, especialidad,
  inicio, fin, mensaje y timestamp.
- `APPOINTMENT_CANCELLED`: cita, paciente, correo, médico, inicio, fin,
  motivo, mensaje y timestamp.
- `APPOINTMENT_RESCHEDULED`: cita, paciente, correo, médico, especialidad,
  inicio, fin, mensaje y timestamp.

## `notification-service`

### Responsabilidad actual

- Consumir eventos de `auth-service` y `appointment-service`.
- Validar envelope, tipo y versión de cada evento.
- Seleccionar canal y plantilla.
- Entregar correo, SMS o push.
- Registrar resultado, reintentos y mensajes fallidos.

El adaptador actual simula entrega mediante logs. Proveedor real de correo, SMS
o push queda pendiente de selección.

No contiene reglas de usuarios, roles, pacientes ni agenda. No debe recibir
contraseñas ni tokens JWT de acceso.

### Contrato inicial

- Entrada: SQS `notification-queue`.
- API pública de negocio: ninguna en primera versión.
- Eventos consumidos: `PASSWORD_RESET_REQUESTED`, `USER_REGISTERED`,
  `APPOINTMENT_BOOKED`, `APPOINTMENT_CANCELLED` y
  `APPOINTMENT_RESCHEDULED`.
- Fallos temporales: el mensaje no se elimina y SQS reintenta.
- Eventos inválidos: SQS reintenta y los mueve a la DLQ configurada después de
  tres intentos.

El consumidor debe implementar deduplicación por `producer` + `eventId` y
aceptar únicamente envelope v1.

## `payment-service` — pendiente

### Responsabilidad propuesta

- Crear intención de pago asociada a una cita.
- Mantener estados e idempotencia.
- Confirmar cobros y procesar reembolsos.
- Recibir webhooks del proveedor.
- Publicar resultado de pago para otros servicios.

El proveedor externo solo se conoce en `infrastructure`, mediante un puerto
`PaymentGateway` definido en `domain`. `domain` no importa SDK del proveedor.

### Contrato inicial propuesto

- `POST /api/v1/payments/intents`: crear intención idempotente.
- `GET /api/v1/payments/{id}`: consultar estado propio o autorizado.
- `POST /api/v1/payments/{id}/confirm`: confirmar pago.
- `POST /api/v1/payments/{id}/refund`: solicitar reembolso autorizado.
- `POST /api/v1/payments/webhooks/{provider}`: recibir notificaciones del
  proveedor.

Estos endpoints quedan pendientes de definir con el proveedor de pago y las
reglas de autorización del negocio.

Política MVP propuesta: el pago no bloquea la creación de la cita. La cita
permanece `SCHEDULED`; payment administra su propio estado y no genera
reembolsos automáticos por cancelación. Detalle en
[`event-contracts.md`](event-contracts.md).

Eventos propuestos:

- `PAYMENT_AUTHORIZED`.
- `PAYMENT_FAILED`.
- `PAYMENT_REFUNDED`.

## Flujos entre servicios

### Recuperación de contraseña

1. Cliente llama `auth-service`.
2. Auth valida usuario y genera token de recuperación.
3. Auth publica `PASSWORD_RESET_REQUESTED` en SQS.
4. Notifications entrega mensaje por canal configurado.

### Cita

1. Cliente envía JWT a `appointment-service`.
2. Appointment valida identidad, reglas de agenda y solapamientos.
3. Appointment persiste la cita.
4. Appointment publica evento de cita en SQS.
5. Notifications entrega confirmación, cancelación o reprogramación.

### Pago futuro

1. Cliente solicita intención en `payment-service`.
2. Payment valida autorización e idempotencia.
3. Payment usa `PaymentGateway` mediante adaptador de infraestructura.
4. Payment recibe webhook y actualiza estado.
5. Payment publica resultado para consumidores autorizados.

## Decisiones pendientes antes de crear los servicios nuevos

- Publishers actuales migrados al envelope v1.
- Consumer v1 implementado con reintentos, DLQ e idempotencia en memoria.
- Persistencia durable de eventos procesados queda pendiente antes de escalar
  horizontalmente.
- Elegir proveedor y canales de notifications.
- Elegir proveedor de pagos y política de reembolso.
- Definir autorización exacta de cancelación, reprogramación y pago.
- Aprobar política MVP de pago no bloqueante o reemplazarla por flujo con
  `PENDING_PAYMENT`.
