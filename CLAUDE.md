# Appointy Med (backend, trabajo académico)

Español, profesional y conciso. Sin emojis ni relleno. Responde primero; no repitas código ya escrito.

## Fuente de verdad
- `docs/especificacion.md`: requisitos, casos de uso, datos y reglas del docente. Prevalece sobre cualquier otro texto. Léela por secciones con `rg`, no completa.
- `docs/progreso.md`: al empezar lee solo "Siguiente paso" y la tabla de Fases (las primeras ~25 líneas); las secciones por fase y las decisiones solo si la tarea las toca. Actualízalo al cerrar una fase.
- Si la especificación no cubre algo o se contradice, pregunta; no inventes reglas de negocio.
- El código anterior sigue en el historial de Git, hasta el commit `efb6897` (`git show efb6897:<ruta>`). Reutiliza piezas sueltas (JWT, filtro, manejador de errores), no su arquitectura.

## Stack y arquitectura
Java 21, Spring Boot 3.5, Maven multimódulo, PostgreSQL local (base `appointy`, un schema y un usuario por servicio), Flyway con `ddl-auto: validate`, JUnit 5 y Mockito. Sin Docker ni broker. API en `/api/v1`; `/internal/**` con API key.

Paquetes `com.appointy.<servicio>` (`auth`, `catalog`, `appointment`, `clinicalhistory`, `notification`):
- `domain`: model, exception, port/in, port/out. **Cero imports ajenos al dominio** (regla del docente): ni librerías, ni Spring, JPA o Lombok, ni siquiera del JDK (`java.util`, `java.time`, `java.nio`). Solo `java.lang` y clases del propio `domain`. Lo vigila `DependenciasEntreCapasTest`.
- `application`: usecase, config.
- `infrastructure`: entry_points, driven_adapters (jpa_repository, rest_client), mapper, security.

Un caso de uso por clase, puertos pequeños, reglas de negocio componibles. Dominio en español; sufijos técnicos en inglés (`ReservarCitaUseCase`, `FranjaRepositoryPort`).

## Tipos del dominio (sin imports)
- Ids: `String` (texto de un UUID; lo genera el caso de uso con `UUID.randomUUID().toString()`).
- Instantes: `long`, milisegundos desde 1970 en UTC. Fechas de calendario: `String` ISO `yyyy-MM-dd`. Hora del día: `int`, minutos desde medianoche. Duraciones: `int`, minutos.
- Búsquedas que pueden no encontrar: el puerto devuelve `null` y lo documenta; el caso de uso lo revisa y lanza la excepción. Sin `Optional`.
- Listas: arreglos (`Tipo[]`), sin `List`.
- Enums, `record` y excepciones que extienden `RuntimeException` no necesitan imports y sí se usan. Sin Lombok: los `record` reemplazan getters, constructores, `equals` y `toString`.
- Las conversiones (`UUID`, `Instant`, `LocalDate`, `Optional`, `List`) viven en `application` (casos de uso) e `infrastructure` (mappers, adaptadores, DTOs), que sí pueden usar el JDK y librerías.
- Si una regla del dominio necesita comparar fechas, el caso de uso le pasa los valores ya calculados (por ejemplo `hoy` como texto ISO o `ahora` como `long`).

## Invariantes
- Reservar, reprogramar y cancelar: un solo `@Transactional`, franja con `PESSIMISTIC_WRITE`, dos franjas en orden por id, índice único parcial como red de seguridad, conflicto = 409.
- Todo cambio de estado de una cita escribe `historial_cita` (append-only).
- Sin FK ni consultas entre schemas. Borrado lógico.
- Errores uniformes (código, mensaje, detalle, timestamp). Sin datos sensibles ni clínicos en logs.
- Notificación con `AFTER_COMMIT` y `@Async`; su fallo no revierte la cita.
- Zona horaria: la JVM corre en UTC (`TimeZone.setDefault` en cada `*Application`) y los instantes se guardan en UTC. El "hoy" de la clínica sale de `app.timezone` (America/Bogota), nunca de la zona de la máquina. Sin esto una hora TIME se guarda desplazada.
- Entre servicios: solo REST a `/internal/**` con `X-Api-Key` (`INTERNAL_API_KEY`) y `X-Correlation-Id`, con timeouts; todo fallo de red o 5xx se informa como 503. El contrato de appointment está en `docs/progreso.md`. Un JWT de usuario no abre `/internal` ni la llave abre la API de usuarios.

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
mvn -q -DskipTests package && bash scripts/e2e/catalog.sh   # prueba real contra PostgreSQL (genera sus propias claves; borra lo que crea)
```
