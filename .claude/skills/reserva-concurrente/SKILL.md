---
name: reserva-concurrente
description: Protocolo para reservar, reprogramar y cancelar citas sin dobles reservas en appointment-service. Usalo al implementar o modificar ReservarCita, ReprogramarCita, CancelarCita, el manejo de conflictos 409 o la prueba de concurrencia (RNF-01, RNF-02, CU-18 a CU-20).
---

# Reserva concurrente de citas

Fuente de verdad: `docs/especificacion.md`, CU-18, CU-19, CU-20 y seccion "Concurrencia".

## Orden obligatorio del caso de uso

1. Validar la entrada (Bean Validation en el controller).
2. Antes de abrir la transaccion: consultar a catalog (profesional asociado a especialidad y centro, parametros) con timeout. Si no responde: 503 y no se guarda nada. Se hace fuera de la transaccion para no retener el bloqueo durante una llamada de red; la franja se reverifica bajo bloqueo (RF-18). **Decision propia, pendiente de validar con el docente.**
3. Abrir UN solo `@Transactional`, en el caso de uso (`application/usecase`), nunca en el controller ni en un adaptador.
4. Recargar la franja con `@Lock(PESSIMISTIC_WRITE)` y un timeout (`jakarta.persistence.lock.timeout`); verificar DISPONIBLE y futura.
5. Aplicar las reglas (`List<ReglaReserva>`) sobre la franja ya bloqueada.
6. Marcar la franja OCUPADA, crear la cita CONFIRMADA y escribir `historial_cita`, todo en la misma transaccion.
7. Commit y responder 201 (reserva), 200 (reprogramar) o 200/204 (cancelar).
8. Despues del commit: evento con `@TransactionalEventListener(AFTER_COMMIT)` y `@Async`. Un fallo del listener se registra y nunca revierte ni propaga.

## Reprogramar y cancelar

- Reprogramar: validar primero la franja nueva; bloquear ambas franjas **en orden ascendente de id** (evita deadlocks entre reprogramaciones cruzadas); ocupar la nueva, liberar la anterior, actualizar la cita y registrar historial. Si algo falla, la cita original queda intacta.
- Cancelar: dueno o administrativo; dentro del limite parametrizable; liberar la franja y registrar quien y cuando.
- Toda mutacion de estado escribe una fila de historial (append-only; nunca UPDATE ni DELETE).

## Conflictos: de la excepcion al HTTP

| Causa | Excepcion | Respuesta |
|---|---|---|
| Franja ya ocupada o no DISPONIBLE | regla de dominio | 409 |
| Espera de bloqueo agotada | `PessimisticLockingFailureException` | 409 |
| Version de franja o cita desfasada | `ObjectOptimisticLockingFailureException` | 409 |
| Indice `uq_cita_franja_confirmada` | `DataIntegrityViolationException` | 409 solo si la restriccion violada es esa |
| Cruce con otra cita del paciente | regla de dominio | 409 |
| Catalog no responde | error del cliente REST | 503 |
| Anticipacion o limite de cancelacion | regla de dominio | 422 |

No conviertas cualquier `DataIntegrityViolationException` en 409: identifica el nombre de la restriccion; otras violaciones son 400 o 500. (El codigo anterior del proyecto lo hacia mal.)

## Puntos finos

- El bloqueo de una franja no impide que un mismo paciente reserve a la vez dos franjas distintas que se cruzan. Propuesta (a validar): `pg_advisory_xact_lock` por `paciente_id` al inicio de la transaccion.
- El indice parcial va en `V1__init.sql`: `CREATE UNIQUE INDEX uq_cita_franja_confirmada ON cita (franja_id) WHERE estado = 'CONFIRMADA'`. Es la red de seguridad, no la defensa principal.
- `@Version` en franja y cita como segunda defensa.

## Prueba de concurrencia (RNF-09)

- Contra PostgreSQL real (Testcontainers o el local), no H2: H2 no valida el indice parcial ni el bloqueo igual.
- N hilos (`ExecutorService` + `CountDownLatch` para soltarlos a la vez) reservando la misma franja.
- Debe haber exactamente 1 exito y N-1 conflictos (409); al final, 1 cita CONFIRMADA y la franja OCUPADA.
- Segunda prueba: dos reprogramaciones cruzadas (A→B y B→A) terminan sin deadlock.
