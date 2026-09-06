# Contrato de eventos y política de pagos

Este documento define el contrato técnico compartido por los microservicios.
Los eventos deben viajar como JSON. Ningún consumidor depende de clases Java
del productor.

## Envelope común, versión 1

Todos los eventos nuevos usan este envelope:

```json
{
  "eventId": "uuid",
  "eventType": "APPOINTMENT_BOOKED",
  "version": 1,
  "occurredAt": "2026-08-30T18:00:00Z",
  "producer": "appointment-service",
  "correlationId": "uuid",
  "causationId": "uuid",
  "payload": {}
}
```

Campos:

| Campo | Obligatorio | Regla |
|---|---|---|
| `eventId` | Sí | UUID único por evento publicado |
| `eventType` | Sí | Nombre estable en mayúsculas y guion bajo |
| `version` | Sí | Entero positivo; inicia en `1` |
| `occurredAt` | Sí | Fecha UTC ISO-8601 de ocurrencia del hecho |
| `producer` | Sí | Nombre estable del microservicio productor |
| `correlationId` | Sí | UUID que agrupa una operación completa |
| `causationId` | No | `eventId` que originó este evento |
| `payload` | Sí | Datos del evento, sin campos de infraestructura |

`correlationId` se genera al iniciar una operación. Un evento derivado conserva
ese valor y usa el evento anterior como `causationId`.

## Reglas de publicación y consumo

- El envelope pertenece al contrato de integración, no al modelo de `domain`.
- El productor construye el evento en `application` o `infrastructure`.
- El puerto del dominio expresa el hecho de negocio; no conoce SQS, JSON ni el
  envelope.
- El cuerpo de SQS contiene el JSON completo.
- `eventId`, `eventType` y `version` pueden repetirse como message attributes
  para filtrado, pero el body sigue siendo la fuente de verdad.
- Entrega es al menos una vez. Los consumidores deben tolerar duplicados.
- Deduplicación por `producer` + `eventId`, antes de ejecutar efectos externos.
- Error temporal: reintento con backoff.
- Error permanente o envelope inválido: dead-letter queue.
- Evento desconocido o versión incompatible: no se procesa silenciosamente; va
  a revisión/DLQ.
- Cambios compatibles agregan campos opcionales dentro de `payload`.
- Cambio incompatible incrementa `version` y mantiene la versión anterior
  durante la transición.
- Nunca publicar contraseñas, JWT de acceso, datos de tarjeta ni CVV.

## Eventos definidos

### Auth

Productor: `auth-service`.

| Evento | Consumidor inicial | Payload mínimo |
|---|---|---|
| `PASSWORD_RESET_REQUESTED` | `notification-service` | `userId`, `email`, `fullName`, `resetToken`, `expiresAt` |
| `USER_REGISTERED` | `notification-service` | `userId`, `email`, `fullName` |

El token de recuperación es secreto de un solo uso. Solo viaja hacia
`notification-service` por la cola protegida y nunca se registra en logs.

### Appointment

Productor: `appointment-service`.

| Evento | Consumidor inicial | Payload mínimo |
|---|---|---|
| `APPOINTMENT_BOOKED` | `notification-service` | `appointmentId`, `patientId`, `patientEmail`, `patientName`, `doctorId`, `doctorName`, `specialtyName`, `appointmentDateTime`, `endDateTime` |
| `APPOINTMENT_CANCELLED` | `notification-service` | `appointmentId`, `patientId`, `patientEmail`, `patientName`, `doctorId`, `doctorName`, `appointmentDateTime`, `endDateTime`, `reason` |
| `APPOINTMENT_RESCHEDULED` | `notification-service` | `appointmentId`, `patientId`, `patientEmail`, `patientName`, `doctorId`, `doctorName`, `specialtyName`, `appointmentDateTime`, `endDateTime` |

### Payment

Productor: `payment-service`.

| Evento | Consumidor inicial | Payload mínimo |
|---|---|---|
| `PAYMENT_AUTHORIZED` | `appointment-service`, `notification-service` | `paymentId`, `appointmentId`, `userId`, `amountMinor`, `currency` |
| `PAYMENT_FAILED` | `appointment-service`, `notification-service` | `paymentId`, `appointmentId`, `userId`, `reasonCode` |
| `PAYMENT_REFUNDED` | `appointment-service`, `notification-service` | `paymentId`, `appointmentId`, `userId`, `amountMinor`, `currency` |

Los eventos de payment son futuros. No se implementan consumidores hasta
aprobar la política de negocio.

## Migración desde eventos actuales

Antes de esta migración, `auth-service` y `appointment-service` publicaban
records planos en SQS. Esos mensajes legacy no son compatibles con el envelope
v1.

Estado actual de implementación: publishers ya envían envelope v1 y
`notification-service` ya consume v1. Mensajes planos antiguos deben agotarse o
reenviarse antes de eliminar cualquier compatibilidad temporal.

Orden de migración:

1. Crear tipos de contrato de integración fuera de `domain`.
2. Envolver cada payload actual con envelope v1.
3. Conservar nombres y datos actuales dentro de `payload`.
4. Probar publicación serializada y deduplicación.
5. Crear `notification-service` con lector v1.
6. Retirar lector plano solo después de completar transición.

No mezclar envelope con entidades JPA ni con value objects del dominio.

## Política de pagos para MVP

Estado: propuesta técnica compatible con el comportamiento actual; requiere
aprobación de negocio antes de implementar `payment-service`.

### Decisión base

El pago no bloquea la reserva en el MVP. La cita se crea en
`appointment-service` con estado `SCHEDULED`. Después, el cliente puede crear
una intención de pago en `payment-service`.

Motivo: el servicio actual no tiene estado `PENDING_PAYMENT`, ni confirmación
de pago integrada. Esta política evita cambiar el flujo existente antes de
contar con proveedor, precios y reglas comerciales definitivos.

### Reglas MVP

- `appointment-service` es dueño del estado de la cita.
- `payment-service` es dueño del estado financiero.
- Payment nunca escribe en la base de datos de appointments.
- Una intención requiere `appointmentId`, `userId`, `amountMinor`, `currency` e
  `idempotencyKey`.
- Dinero se representa en unidades menores enteras; nunca `float` o `double`.
- `currency` usa código ISO-4217 en mayúsculas.
- La misma `idempotencyKey` no puede crear dos intenciones distintas.
- Solo webhooks verificados o acciones autorizadas cambian estado financiero.
- No se almacenan número de tarjeta, CVV ni secretos del proveedor.
- Cancelar una cita no genera reembolso automático en MVP.
- Reembolso inicial: solicitud explícita, autorizada y total; reglas parciales
  quedan pendientes.
- Payment publica resultado; no ordena directamente cambios en otro servicio.

### Flujo MVP

1. Appointment crea la cita.
2. Cliente solicita intención de pago.
3. Payment crea intención idempotente y llama al proveedor mediante
   `PaymentGateway`.
4. Proveedor confirma mediante webhook verificado.
5. Payment persiste estado y publica `PAYMENT_AUTHORIZED` o `PAYMENT_FAILED`.

### Evolución para citas pagadas

Si negocio exige pago previo, no reutilizar `SCHEDULED` para representar pago
pendiente. Agregar estado explícito `PENDING_PAYMENT` en appointment y definir
saga/outbox con expiración, cancelación compensatoria e idempotencia antes de
activar ese flujo.

## Decisiones aún abiertas

- Aprobar o modificar política MVP de pago no bloqueante.
- Elegir proveedor de pagos.
- Definir tarifas, impuestos, moneda y reglas de reembolso.
- Elegir proveedor de correo, SMS y push.
- Definir retención y auditoría de datos de pago.
