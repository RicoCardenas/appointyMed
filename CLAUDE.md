# Appointy Med (backend, trabajo académico)

Español, profesional y conciso. Sin emojis ni relleno. Responde primero; no repitas código ya escrito.

## Fuente de verdad
- `docs/especificacion.md`: requisitos, casos de uso, datos y reglas del docente. Prevalece sobre cualquier otro texto. Léela por secciones con `rg`, no completa.
- `docs/progreso.md`: fases y decisiones pendientes. Léelo al empezar y actualízalo solo al cerrar una fase.
- Si la especificación no cubre algo o se contradice, pregunta; no inventes reglas de negocio.
- El código anterior sigue en el historial de Git, hasta el commit `efb6897` (`git show efb6897:<ruta>`). Reutiliza piezas sueltas (JWT, filtro, manejador de errores), no su arquitectura.

## Stack y arquitectura
Java 21, Spring Boot 3.5, Maven multimódulo, PostgreSQL local (base `appointy`, un schema y un usuario por servicio), Flyway con `ddl-auto: validate`, JUnit 5 y Mockito. Sin Docker ni broker. API en `/api/v1`; `/internal/**` con API key.

Paquetes `com.appointy.<servicio>` (`auth`, `catalog`, `appointment`, `clinicalhistory`, `notification`):
- `domain`: model, exception, port/in, port/out. Sin Spring ni JPA.
- `application`: usecase, config.
- `infrastructure`: entry_points, driven_adapters (jpa_repository, rest_client), mapper, security.

Un caso de uso por clase, puertos pequeños, reglas de negocio componibles. Dominio en español; sufijos técnicos en inglés (`ReservarCitaUseCase`, `FranjaRepositoryPort`).

## Invariantes
- Reservar, reprogramar y cancelar: un solo `@Transactional`, franja con `PESSIMISTIC_WRITE`, dos franjas en orden por id, índice único parcial como red de seguridad, conflicto = 409.
- Todo cambio de estado de una cita escribe `historial_cita` (append-only).
- Sin FK ni consultas entre schemas. Borrado lógico.
- Errores uniformes (código, mensaje, detalle, timestamp). Sin datos sensibles ni clínicos en logs.
- Notificación con `AFTER_COMMIT` y `@Async`; su fallo no revierte la cita.

## Cómo trabajar
- Una fase a la vez (ver `docs/progreso.md`). Lee solo lo de la fase; usa `rg` y lecturas delimitadas.
- Edita en lugar de reescribir. No crees `.md` para registrar cambios: el historial es `git log` y la evidencia son las pruebas.
- Documentos permitidos: `CLAUDE.md`, `README.md`, `docs/especificacion.md`, `docs/progreso.md`.
- Filtra la salida de comandos (`-q`, `| tail`); no pegues logs completos.
- Pruebas del módulo afectado: `mvn -pl <servicio> -am test`. El reactor completo solo al cerrar fase.
- Informe de cada tarea, máximo 5 líneas: cambio, prueba y resultado, riesgo.
- Se trabaja directamente en `main`. Un commit al cerrar cada fase (autorizado por el usuario); `push` solo si se pide. Sin secretos en archivos. No leas ni copies `.env` ni `.aws`.

## Skills (`.claude/skills/`, se añaden por fase; léelas solo cuando la tarea las toque)
- Fases 0 a 6: clean-architecture (la estructura de la especificación prevalece), spring-boot-security-jwt, spring-boot-event-driven-patterns, spring-boot-test-patterns, spring-boot-rest-api-standards, spring-data-jpa, unit-test-service-layer, reserva-concurrente, flyway-schemas.
- Fase 6: spring-boot-openapi-documentation. Fase 7 (interfaz): impeccable, emil-design-eng, break-ui, review-animations. Fase 8 (AWS): skills de AWS; no ejecutes acciones sobre la cuenta sin confirmación expresa y con costo estimado.
- Los hooks de terceros permanecen desactivados. Origen y licencias en `.claude/skills/NOTICE.md`.

## Comandos
```bash
mvn -q -DskipTests compile                 # compilar los 5 módulos (JDK 21)
mvn -pl auth-service spring-boot:run       # arrancar un servicio (variables de .env exportadas)
```
