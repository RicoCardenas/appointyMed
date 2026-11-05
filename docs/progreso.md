# Appointy Med: progreso

Rama `main`. El código anterior está en el historial hasta el commit `efb6897`; consúltalo con `git show efb6897:<ruta>`. Última actualización: 2026-11-05.

## Fases

| Fase | Contenido | Estado |
|---|---|---|
| 0 | Base: rama, especificación, POM de 5 módulos, schemas, skills, documentación | Hecha salvo la verificación con JDK 21 y Flyway (ver abajo) |
| 1 | auth-service: CU-01 a CU-07 | Pendiente |
| 2 | catalog-service: CU-08 a CU-16 | Pendiente |
| 3 | appointment-service: CU-17 a CU-27 y prueba de concurrencia | Pendiente |
| 4 | clinical-history-service: CU-28 a CU-32 | Pendiente |
| 5 | notification-service: CU-33 a CU-35 | Pendiente |
| 6 | Cierre: OpenAPI, colección Postman, cobertura 70 %, correlación de logs | Pendiente |
| 7 | Interfaz (proyecto aparte en `frontend/`) | Pendiente, por decidir stack |
| 8 | Despliegue en AWS | Pendiente, por decidir alcance y presupuesto |

## Validado en la Fase 0

- `mvn -DskipTests compile` de los 5 módulos con JDK 21.0.12 y Maven 3.9.12: exitoso.
- No verificado: arranque de los servicios y Flyway contra PostgreSQL 18. Las cinco variables `*_DB_PASSWORD` están vacías en `.env`, así que no hay forma de conectar todavía.

## Pendiente del equipo

1. Completar en `.env` las cinco `*_DB_PASSWORD` con las mismas claves usadas en `db/init.sql` (si `init.sql` aún no se ejecutó, ejecutarlo primero).

## Decisiones propias a validar con el docente

1. Idioma: dominio en español; sufijos técnicos en inglés (`ReservarCitaUseCase`, `FranjaRepositoryPort`).
2. Spring Boot 3.5.16 (la especificación dice 3.x).
3. `MODIFICADA` fuera del CHECK de `cita`; el cambio de franja queda en `historial_cita` (recomendación del docente).
4. Códigos 422 y 503 además de los de la convención, porque los casos de uso los piden.
5. appointment consulta a catalog antes de abrir la transacción y reverifica la franja bajo bloqueo.
6. Candado `pg_advisory_xact_lock` por paciente contra citas cruzadas en franjas distintas.
7. El correo del paciente cuando cancela un administrativo se obtiene de auth por `/internal`.
8. Recordatorio: crear la fila al confirmar y añadir el estado CANCELADA a `notificacion`.
9. Los tokens de acceso revocados siguen válidos en otros servicios hasta expirar; mantener expiración corta.
10. `HorarioAtencion` vive en catalog (la especificación lo lista también en appointment).

## Pedir al docente

- La lista de reglas de negocio RN-xx (el enunciado cita RN-06, 09, 11, 13 y 17).
- Si el despliegue en AWS se evalúa y con qué presupuesto.

## Pendientes menores

- Revisar `.github/modernize/` (restos de una herramienta de actualización de Java, sin relación con el proyecto).
- Skills de interfaz y AWS: se copian al empezar sus fases (ver `CLAUDE.md`).
