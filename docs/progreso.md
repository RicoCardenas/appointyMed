# Appointy Med: progreso

Rama `main`. El código anterior está en el historial hasta el commit `efb6897`; consúltalo con `git show efb6897:<ruta>`. Última actualización: 2026-11-14.

## Siguiente paso: Fase 6b, requisitos adicionales (RA-03 a RA-06)

Ver "Requisitos adicionales" más abajo. Orden sugerido: (1) JaCoCo al 90 % por módulo y medir lo que hay hoy; (2) completar 5 APIs no CRUD y el GET all paginado en auth, catalog, appointment y clinical (notification ya los cumple); (3) las 2 APIs externas (feriados en catalog, CIE-10 en clinical); (4) luego la Fase 6 (OpenAPI, Postman) y la 7 (front).

- Pendiente del equipo: `JWT_SECRET`, `INTERNAL_API_KEY` y `NOTIFICATION_DB_PASSWORD` en `.env`; opcional `MAIL_ENABLED=true` con `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` para correo real.

## Fases

| Fase | Contenido | Estado |
|---|---|---|
| 0 | Base: especificación, POM de 5 módulos, schemas, skills, documentación | Hecha y verificada (ver abajo) |
| 1 | auth-service: CU-01 a CU-07 | Hecha y verificada (ver abajo) |
| 2 | catalog-service: CU-08 a CU-16 | Hecha y verificada con appointment simulado (ver abajo) |
| 3 | appointment-service: CU-17 a CU-27 y prueba de concurrencia | Hecha (ver abajo) |
| 4 | clinical-history-service: CU-28 a CU-32 | Hecha (ver abajo) |
| 5 | notification-service: CU-33 a CU-35 | Hecha y verificada (ver abajo) |
| 6 | Cierre: OpenAPI, colección Postman, cobertura 90 % (JaCoCo), correlación de logs | Pendiente |
| 6b | Requisitos RA-03, RA-04 y RA-06: 5 APIs no CRUD y GET all paginado por servicio, 2 APIs externas | Pendiente (ver "Requisitos adicionales") |
| 7 | Interfaz (proyecto aparte en `frontend/`), con las skills `impeccable` y `apple-design` | Pendiente, por decidir stack |
| 8 | Despliegue en AWS | Pendiente, por decidir alcance y presupuesto |

## Requisitos adicionales (RA-01 a RA-07, ver especificación sección 11)

Estado frente a cada requisito:
- RA-01, RA-02: cumplidos (5 servicios, una base `appointy`).
- RA-03: inventario por servicio y brecha hasta 5 APIs no CRUD. Hacer al abrir la Fase 6b: contar lo que ya existe (casi todo son casos de uso de negocio) y completar con consultas/reportes. Propuesta de relleno: auth (estadísticas de usuarios por rol, buscar pacientes), catalog (disponibilidad por especialidad, profesionales por centro, festivos del mes), appointment (agenda por rango, próximas citas, resumen de estados), clinical (resumen del paciente, búsqueda de diagnósticos con CIE-10), notification (reenviar, estadísticas de envío, historial por cita).
- RA-04: un GET all paginado (`page`, `size`, `sort`, respuesta con `contenido`, `pagina`, `tamano`, `totalElementos`, `totalPaginas`) por servicio: auth `/admin/usuarios`, catalog `/profesionales`, appointment `/citas`, clinical `/atenciones`, notification `/notificaciones`. Hay que revisar cuáles de los listados actuales ya paginan.
- RA-05: añadir JaCoCo al POM padre con `check` en 0,90 de líneas por módulo (excluir `*Application`, config y DTOs sin lógica). Hoy no se mide cobertura.
- RA-06 (propuesta, decisión 23): (1) feriados de Colombia con Nager.Date (`https://date.nager.at/api/v3/PublicHolidays/{año}/CO`, sin llave) consumida por catalog al generar franjas, para no ofrecer citas en festivos; (2) catálogo CIE-10 de NLM Clinical Tables (`https://clinicaltables.nlm.nih.gov/api/icd10cm/v3/search?terms=...`, sin llave) consumido por clinical-history para buscar y validar diagnósticos. Ambas con timeout, caché Caffeine y degradación: si fallan, catalog genera sin filtrar festivos y clinical acepta el diagnóstico en texto libre; nunca tumban la operación.
- RA-07: Fase 7, carpeta `frontend/`, stack por decidir (propuesta: React + Vite + TypeScript consumiendo la API por un único origen). Skills de diseño ya instaladas: `impeccable` (al empezar el front correr `/impeccable init` para crear `PRODUCT.md` y `DESIGN.md`) y `apple-design`.

## Validado en la Fase 0

- `mvn clean package -DskipTests` de los 5 módulos con JDK 21.0.12 y Maven 3.9.12: exitoso.
- `db/init.sql` aplicado en PostgreSQL 18.6: los 5 usuarios conectan, cada uno con su schema por defecto y viendo solo ese schema (RNF-15).
- Los 5 servicios arrancan y `/actuator/health` responde UP en 8081 a 8085; Flyway crea `flyway_schema_history` en el schema de cada servicio. Todavía no hay migraciones (0 validadas).
- Flyway 11.7.2 (BOM de Spring Boot) avisa que no soporta PostgreSQL 18; el POM padre fija `flyway.version` en 11.20.3 y el aviso desaparece (comprobado en auth y clinical-history).
- No probado aún: tablas reales ni validación de Hibernate contra entidades (no existen entidades).

## Fase 1: auth-service

Endpoints (base `/api/v1`, puerto 8081):

| Método y ruta | Rol | CU |
|---|---|---|
| POST `/auth/register` | público | CU-01 |
| POST `/auth/login` | público | CU-02 |
| POST `/auth/logout` | autenticado | CU-03 |
| GET `/pacientes/me` | PACIENTE | CU-04 |
| PATCH `/pacientes/me` (correo, teléfono, dirección, ciudad) | PACIENTE | CU-05 |
| POST `/admin/usuarios` (PROFESIONAL o ADMINISTRATIVO) | ADMINISTRATIVO | CU-06 |
| PATCH `/admin/pacientes/{usuarioId}/identidad` | ADMINISTRATIVO | CU-07 |

Comportamiento a tener en cuenta:
- JWT firmado con `JWT_SECRET` (mínimo 32 caracteres, el mismo en todos los servicios), emisor `appointy-auth`, 15 minutos, claims `sub` (usuarioId), `rol` y `jti`. Los demás servicios lo validan localmente.
- El cierre de sesión guarda el `jti` en `token_revocado`; un job horario borra los ya vencidos (RF-03). Un token revocado solo se rechaza en auth: los otros servicios lo aceptarán hasta que expire (decisión 9).
- El primer ADMINISTRATIVO se crea al arrancar desde `ADMIN_EMAIL` y `ADMIN_PASSWORD`, solo si no existe ninguno. Sin esas variables no hay administrador y nadie puede crear cuentas de personal.
- El paciente no puede cambiar cédula, nombres ni nacimiento: un campo fuera de los editables devuelve 400 `CAMPO_NO_PERMITIDO`. Solo CU-07 los corrige y deja en el log quién y qué campos, nunca los valores.
- Login: mismo 401 para correo inexistente, clave errónea o cuenta inactiva, y se compara contra un hash señuelo para no revelar si la cuenta existe.
- Los correos se guardan en minúsculas (CHECK en BD). Las claves: 8 a 72 bytes con al menos una letra y un dígito (la especificación no fija política; es decisión propia).
- Las violaciones de unicidad se traducen a 409 por nombre de restricción (`uq_usuario_correo`, `uq_paciente_cedula`); cualquier otra violación de integridad devuelve 500, no 409.
- `X-Correlation-Id` (RNF-17): se acepta el del cliente si es alfanumérico de 8 a 64 caracteres, si no se genera uno; va en la respuesta y en cada línea de log.

## Validado en la Fase 1

- `mvn -pl auth-service test`: 64 pruebas (JUnit 5 y Mockito) sin fallos. Cubren los 9 casos de uso, la política de claves (incluido el conteo de bytes UTF-8 a mano), el normalizador de correo (solo ASCII), el emisor JWT (vigencia, firma, emisor, secreto corto) y la arquitectura: el dominio solo importa clases de su propio paquete, no usa tipos externos con nombre completo y la aplicación no importa infraestructura. Se comprobó que esa prueba falla al añadir un `import java.util.UUID` al dominio.
- Dominio sin imports (regla del docente): ids `String`, instantes `long`, fechas `String` ISO, búsquedas con `null`; las conversiones están en los casos de uso, mappers, adaptadores y DTOs. Tras la conversión la API no cambió: se repitió la prueba de punta a punta con el mismo resultado.
- Prueba de punta a punta con `curl` contra PostgreSQL 18.6 real: 51 comprobaciones correctas, 0 fallidas. Flyway aplicó `V1`, Hibernate validó contra las tablas, los 7 CU respondieron con los códigos esperados (incluidos 401, 403, 404 y 409), las claves quedan en BCrypt, ni la clave ni el token ni datos personales aparecen en el log. Los datos de prueba se borraron al terminar.
- No probado: carga o concurrencia sobre el registro (la unicidad se apoya en las restricciones de BD), expiración real del job de limpieza (solo prueba unitaria), arranque con claves reales en `.env`.

## Fase 2: catalog-service

Puerto 8082, base `/api/v1`, JWT de auth validado localmente. Salvo que se indique, "cualquiera" es cualquier usuario autenticado.

| Método y ruta | Rol | CU |
|---|---|---|
| GET `/especialidades` (`?incluirInactivas=true` solo ADMINISTRATIVO) | cualquiera | CU-08 |
| GET `/especialidades/{id}/profesionales` | cualquiera | CU-09 |
| GET `/profesionales/{id}/centros` | cualquiera | CU-10 |
| POST, PUT `/especialidades`, `/centros`, `/profesionales`; DELETE = desactivar | ADMINISTRATIVO | CU-11 |
| GET `/profesionales/{id}` (con especialidades y centros) | cualquiera | |
| PUT `/profesionales/{id}/especialidades`, `/profesionales/{id}/centros` (lista completa de ids) | ADMINISTRATIVO | CU-12 |
| POST `/profesionales/{id}/horarios`; GET `/profesionales/{id}/horarios` (ADMINISTRATIVO o PROFESIONAL) | ADMINISTRATIVO | CU-13 |
| PUT `/horarios/{id}` (incluye `activo`) | ADMINISTRATIVO | CU-14 |
| POST `/horarios/{id}/franjas` (reintento de generación, 503 si appointment no responde) | ADMINISTRATIVO | CU-13, CU-25 |
| GET, PUT `/parametros` | ADMINISTRATIVO | CU-15 |
| POST `/bloqueos` | ADMINISTRATIVO | CU-16, CU-26 |
| GET `/internal/profesionales/{id}/asociacion?especialidadId=&centroId=` y GET `/internal/parametros` (cabecera `X-Api-Key`) | servicio | para CU-18 |

### Contrato que appointment-service debe cumplir (Fase 3)

Catalog llama a `/internal` con `X-Api-Key` (`INTERNAL_API_KEY`), `X-Correlation-Id`, timeout de conexión 2 s y de lectura 5 s. Cualquier fallo de red, timeout, 5xx o 401 se devuelve al cliente como 503 `APPOINTMENT_NO_DISPONIBLE`.

- `POST /internal/franjas/generar` con `{profesionalId, centroId, franjas:[{fecha:"yyyy-MM-dd", horaInicio:"HH:mm", horaFin:"HH:mm"}]}` responde 200 `{creadas, existentes}`. Idempotente: repetir no duplica (RNF-16; `UNIQUE(profesional_id, centro_id, fecha, hora_inicio)`).
- `POST /internal/franjas/bloquear` con `{profesionalId, centroId, fechaInicio, fechaFin}` responde 200 `{bloqueadas}`, o 409 `{citasAfectadas:["id", ...]}` si hay citas CONFIRMADAS en el intervalo (no bloquea nada). Idempotente.
- Catalog genera las franjas desde el "hoy" de la clínica hasta hoy + `horizonte_max_dias`; appointment solo las persiste.

Comportamiento a tener en cuenta:
- El horario siempre se guarda; si appointment no responde, la respuesta trae `franjasGeneradas:false` y se reintenta con POST `/horarios/{id}/franjas`. Modificar un horario no retira las franjas ya generadas de su versión anterior: para dejar de ofrecerlas se usa un bloqueo.
- El bloqueo se guarda y se llama a appointment dentro de la misma transacción: si appointment rechaza (409 con la lista de citas) o no responde (503), el bloqueo se revierte y no queda nada en catalog.
- Un profesional no puede tener horarios activos que se crucen el mismo día, ni siquiera en centros distintos. La duración de la franja debe dividir el rango exacto. Las horas van de 00:00 a 23:59 (se envían como `HH:mm`).
- No se puede quitar de un profesional un centro que tenga horarios o bloqueos suyos (409 `CENTRO_CON_HORARIOS`).
- Las rutas `/internal/**` solo aceptan la llave de servicio: un JWT de usuario no las abre y la llave no abre la API de usuarios.

## Validado en la Fase 2

- 85 pruebas unitarias de catalog (149 con las de auth), sin fallos. Cubren las reglas de horario, parámetros y bloqueo, el generador de franjas (incluida la zona horaria: a las 22:00 en Bogotá "hoy" sigue siendo el día local aunque en UTC ya sea el siguiente), los casos de uso, el cliente REST contra un servidor HTTP simulado (éxito, 409 con citas, 500, llave rechazada, servidor caído, timeout), el validador JWT, la llave de API y la arquitectura. La prueba de arquitectura se comprobó con dos infracciones deliberadas (un import y un tipo con nombre completo): falla con ambas.
- Prueba real de punta a punta con auth real, catalog real y un appointment simulado: 121 comprobaciones correctas, 0 fallidas, y repetida la de auth (51 de 51). Incluye el JWT de auth validado en catalog, roles, los 9 CU, la caída de appointment, el reintento idempotente, el timeout (503 en menos de 6 s sin esperar los 8 s del simulado), el rollback del bloqueo, la API interna con y sin llave, que la llave y el id de correlación llegan a appointment y que no hay secretos ni datos personales en los logs. Los datos de prueba se borraron y los parámetros se restauraron.
- Defecto real encontrado por la prueba y corregido: `hora_inicio` de 08:00 quedaba guardada como 13:00 porque la JVM usaba la zona de Bogotá y JDBC trabaja en UTC; leída de vuelta parecía correcta, pero en AWS (JVM en UTC) se habría leído mal. Cada servicio fija ahora UTC como zona por defecto de la JVM al arrancar. La prueba real comprueba que se guarda `08:00:00`, que las fechas del bloqueo no se desplazan y que `creado_en` está en UTC.
- No probado: el appointment real (solo simulado hasta la Fase 3), concurrencia de dos administradores editando el mismo horario (la comprobación de cruce y el guardado no son atómicos; la especificación pide validarlo en el dominio), y comportamiento tras mucho tiempo de uso del cliente REST.

## Fase 3: appointment-service

Puerto 8083, base `/api/v1`. Franjas con fecha y hora locales de la clínica; instantes en UTC.

| Método y ruta | Rol | CU |
|---|---|---|
| GET `/disponibilidad?profesionalId=&centroId=&fecha=` | cualquiera | CU-17 |
| POST `/citas` (especialidadId, profesionalId, centroId, franjaId) | PACIENTE | CU-18 |
| POST `/citas/{id}/reprogramar` (franjaId) | PACIENTE dueño | CU-19 |
| POST `/citas/{id}/cancelar` (motivo opcional) | PACIENTE dueño o ADMINISTRATIVO | CU-20 |
| GET `/citas/mias?periodo=PROXIMAS\|PASADAS` | PACIENTE | CU-21 |
| GET `/citas/agenda?fecha=` (sin fecha: de hoy en adelante) | PROFESIONAL | CU-22 |
| PATCH `/citas/{id}/estado` (COMPLETADA o NO_ASISTIDA) | PROFESIONAL de la cita | CU-23 |
| GET `/citas/{id}/historial`, GET `/citas/{id}` | paciente, profesional de la cita o ADMINISTRATIVO | CU-24 |
| POST `/internal/franjas/generar`, `/internal/franjas/bloquear` | servicio (catalog) | CU-25, CU-26 |
| GET `/internal/relaciones?profesionalId=&pacienteId=` -> `{existe}` | servicio (clinical) | CU-27 |

Contrato hacia notification (Fase 5): `POST /internal/notificaciones` con `{tipo: CONFIRMADA|REPROGRAMADA|CANCELADA, citaId, pacienteId, profesionalId, centroId, fecha, horaInicio:"HH:mm", actorId}`, llave de servicio y correlation id. Solo ids, sin datos personales. Se envía tras el commit y de forma asíncrona; si falla, solo se registra. Catalog tiene además `GET /internal/profesionales/por-usuario/{usuarioId}` (nuevo).

Decisiones de esta fase:
- `paciente_id` de la cita es el `usuarioId` del JWT. La agenda y CU-23 resuelven el profesional de la cuenta en catalog.
- Anticipación mínima aplica a reservar y reprogramar; el límite de cancelación, a cancelar (también al administrativo). Reprogramar puede cambiar de profesional y centro si sigue asociado a la especialidad de la cita.
- Orden de bloqueos: candado por paciente, franjas por id ascendente, luego la cita (que se relee bajo bloqueo; si se movió, 409 `CITA_CAMBIO`). Espera máxima 5 s (`lock_timeout`), vencida es 409.
- Cada caso de uso con llamadas a catalog se parte en un caso de uso (consulta fuera de la transacción) y una clase `*Transaccion` con el único `@Transactional`.
- CU-27: hay relación si existe una cita CONFIRMADA, COMPLETADA o NO_ASISTIDA entre ambos.
- Marcar COMPLETADA o NO_ASISTIDA no exige que la hora ya haya pasado, y la franja sigue OCUPADA.
- Cancelación por administrativo: el evento solo lleva `pacienteId`; resolver el correo en auth (decisión 7) queda para notification.
- `catalog.sh` conserva el appointment simulado porque inyecta fallos (timeout de 8 s) que el real no puede; la integración real está en `appointment.sh` (auth, catalog y appointment reales, notification simulado).

## Validado en la Fase 3

- 69 pruebas unitarias en appointment (reglas, casos de uso, clientes REST con servidor simulado, oyente posterior al commit, arquitectura).
- Prueba de concurrencia contra PostgreSQL real (`ReservaConcurrenteTest`, se activa si `APPOINTMENT_DB_PASSWORD` está definida, p. ej. cargando `.env`): 12 hilos sobre una franja, 1 éxito y 11 conflictos; mismo paciente con dos franjas que se cruzan; reprogramaciones cruzadas sin deadlock; dos reprogramaciones a la misma franja libre; el índice parcial rechaza el duplicado.
- `scripts/e2e/appointment.sh` (auth, catalog y appointment reales, notification simulado): 131 comprobaciones correctas, 0 fallidas. Cubre los 11 CU, el catalog→appointment real (franjas, idempotencia, bloqueo con 409 y rollback), 8 solicitudes HTTP simultáneas sobre una franja (1 éxito, 7 conflictos), notification caído (la cita se guarda), catalog caído (503), la API interna con y sin llave, y que no hay secretos ni datos personales en el log. Defecto de la propia prueba corregido: un `wait` sin argumentos la dejaba colgada.

## Fase 4: clinical-history-service

Puerto 8084, base `/api/v1`. El rol lo exige cada caso de uso (no `@PreAuthorize`) para que un rechazo por rol también quede en el log de acceso; sin token responde 401 (no hay usuario que registrar).

| Método y ruta | Rol | CU |
|---|---|---|
| POST `/entradas` (citaId, motivo, diagnostico, notas?, indicaciones?) | PROFESIONAL de la cita | CU-28 |
| GET `/pacientes/{pacienteId}/historia` | PROFESIONAL con relación (CU-27) | CU-29 |
| GET `/historia/mia` | PACIENTE | CU-30 |
| POST `/entradas/{id}/correcciones` (mismos textos) | PROFESIONAL autor | CU-31 |

CU-32 es transversal: `log_acceso_clinico` (usuario, rol, paciente, acción, entrada, fecha, resultado) se escribe en cada lectura y escritura, también las rechazadas. La escritura clínica y su log van en la misma transacción; el rechazo se registra aparte. Si no se puede registrar una lectura permitida, no se entrega la historia.

Dependencias: catalog `GET /internal/profesionales/por-usuario/{id}` (id de catalog del profesional) y appointment `GET /internal/relaciones` (CU-27) y `GET /internal/citas/{id}` (nuevo; solo `citaId, pacienteId, profesionalId, estado`). Timeouts 2 s y 5 s; todo fallo, timeout o 5xx es 503 (`CATALOG_NO_DISPONIBLE`, `APPOINTMENT_NO_DISPONIBLE`). Cache Caffeine de las relaciones confirmadas (60 s, `RELACIONES_TTL_SEGUNDOS`); las negativas y el estado de la cita no se cachean.

Respuestas de error: 400 validación, 401, 403 (`SOLO_PROFESIONAL`, `SOLO_PACIENTE`, `NO_ES_TU_CITA`, `NO_ES_TU_ENTRADA`, `SIN_RELACION_CON_PACIENTE`), 404, 409 `ATENCION_YA_REGISTRADA`, 422 `CITA_NO_ATENDIBLE`, 503.

Decisiones de esta fase (las tres primeras las confirmó el equipo al arrancar):
- Appointment expone `GET /internal/citas/{id}` porque CU-28 exige comprobar el profesional y el estado de la cita concreta y CU-27 solo da la relación.
- Solo el autor corrige una entrada (CU-31).
- El profesional con relación ve todas las entradas del paciente, también las de otros profesionales (lectura literal de RF-27).
- Registrar exige cita CONFIRMADA o COMPLETADA (RN-17) y que el profesional de catalog de la cuenta sea el de la cita; una sola entrada original por cita (índice único parcial de la especificación); las correcciones heredan paciente, cita y autor, y se puede corregir una corrección.
- Motivo y diagnóstico obligatorios; notas e indicaciones opcionales; máximo 5000 caracteres por texto (la especificación no los distingue).
- Append-only también en la base: un trigger rechaza UPDATE y DELETE en `entrada_clinica` y `log_acceso_clinico`. Para limpiar datos de prueba el propietario desactiva el trigger un instante (lo hace `clinical.sh`).
- Sin datos clínicos en logs: ni el contenido ni la traza de las excepciones de base de datos (PostgreSQL puede incluir la fila rechazada); solo el nombre de la clase de la causa.
- `paciente_id` y `entrada_id` del log son nulos cuando se rechaza antes de conocerlos (p. ej. cita inexistente). ADMINISTRATIVO no accede a historias.
- No hay endpoint para consultar el log de acceso: la especificación solo pide registrarlo.

## Validado en la Fase 4

- 42 pruebas unitarias en clinical (reglas, casos de uso con el log de acceso en cada rechazo, clientes REST con servidor simulado incluida la cache, validador JWT, llave de API, arquitectura) y 2 nuevas en appointment (71). Reactor completo: auth 64, catalog 85, appointment 71, clinical 42, sin fallos.
- `scripts/e2e/clinical.sh` (auth, catalog, appointment y clinical reales, notification simulado): 105 comprobaciones correctas, 0 fallidas. Cubre los 5 CU, cada rol y cada rechazo, el log de acceso (permitidos y denegados, atómico con la escritura), que la base rechaza UPDATE y DELETE, 6 registros simultáneos sobre una cita (1 éxito, 5 conflictos 409, sin 5xx), appointment caído (la relación ya confirmada sale de la cache; lo demás da 503), catalog caído, la API interna con y sin llave, y que no hay texto clínico, secretos ni datos personales en el log ni en los errores. Los datos de prueba se borraron.
- Defecto real encontrado por la prueba y corregido: Hibernate registraba el mensaje SQL del driver al violar el índice único, y PostgreSQL puede adjuntar la fila rechazada (texto clínico); se silencia ese registrador y el manejador de errores solo anota la clase de la causa.
- No probado: carga sostenida, expiración de la cache en tiempo real más allá de la prueba unitaria, y que el log de acceso resista millones de filas (hay índices por paciente y por usuario).

## Fase 5: notification-service

Puerto 8085, base `/api/v1`. Auth gana `GET /internal/usuarios/{id}/contacto` -> `{correo}` (llave de servicio; 404 si no existe o está inactivo) con su propia cadena de seguridad para `/internal/**`.

| Método y ruta | Rol | CU / RA |
|---|---|---|
| POST `/internal/notificaciones` (contrato de la Fase 3) -> 201 `{id, estado}` | servicio (appointment) | CU-33 |
| GET `/api/v1/notificaciones?page=&size=&estado=&tipo=&citaId=` | ADMINISTRATIVO | RA-04 (paginado) |
| GET `/notificaciones/estadisticas`, GET `/notificaciones/cita/{citaId}` | ADMINISTRATIVO | RA-03 |
| POST `/notificaciones/{id}/reenviar` | ADMINISTRATIVO | RA-03 |
| POST `/notificaciones/recordatorios/ejecutar`, POST `/notificaciones/reintentos/ejecutar` | ADMINISTRATIVO | CU-34, CU-35, RA-03 |

Comportamiento:
- CU-33 resuelve el correo en auth, registra la fila y envía en la misma llamada; si el correo falla, la fila queda FALLIDA y la respuesta sigue siendo 201 (la cita nunca se pierde). Si auth no responde, 503 y no se guarda nada; si el paciente no existe, 404.
- Idempotencia (RNF-16): UNIQUE(cita_id, tipo, programada_para). Las notificaciones inmediatas usan el inicio de la cita como `programada_para`, así que repetir la misma solicitud devuelve la misma notificación. Límite conocido: reprogramar a un horario al que la cita ya se movió antes (A a B a A a B) no genera un segundo correo.
- Recordatorio (CU-34): al confirmar o reprogramar con más de 24 h de margen se crea una fila RECORDATORIO PENDIENTE con `programada_para` = inicio menos 24 h. Reprogramar o cancelar pasa los pendientes a CANCELADA (estado añadido, decisión 8). Job cada 60 s.
- Reintentos (CU-35): job cada 5 min sobre FALLIDA con menos de 3 intentos (`NOTIFICATION_MAX_INTENTOS`); tras el tercero queda FALLIDA definitiva y solo se reenvía a mano. `ultimo_error` guarda un código, nunca el mensaje de la causa.
- Correo: por defecto `MAIL_ENABLED=false` (adaptador simulado: marca ENVIADA y registra solo el asunto). Con `true` usa SMTP (`spring-boot-starter-mail`) con timeouts de 5 s. El indicador de salud de correo está desactivado para que una caída del SMTP no marque el servicio como DOWN.
- La API enmascara el correo (`p***@dominio`) y no expone el cuerpo (RNF-05). El cuerpo solo lleva fecha, hora y código de la cita; no consulta a catalog.
- Los jobs asumen una sola instancia (sin bloqueo de lote).

## Validado en la Fase 5

- `mvn -pl notification-service test`: 39 pruebas; auth pasa de 64 a 68 (caso de uso de contacto y filtro de llave). Incluyen el cliente HTTP a auth (llave, correlación, 404, 5xx, timeout y servicio caído) y la regla de capas.
- `bash scripts/e2e/notification.sh` (auth real + notification real, PostgreSQL local, correo simulado): 46 comprobaciones correctas, 0 fallidas. Cubre la seguridad de `/internal`, los tres tipos de CU-33, idempotencia, creación y cancelación de recordatorios, paginación y filtros, roles, CU-34, CU-35 y que el log no contiene correos ni cuerpos.
- Defectos reales encontrados y corregidos: `/actuator/health` quedaba DOWN por el indicador de correo; una fecha con formato válido pero inexistente (`2026-13-45`) daba 500 en vez de 400; el enmascarado de un correo sin `@` lo devolvía completo.
- No probado: envío por SMTP real, dos instancias a la vez, appointment real llamando a notification (la integración se prueba con las Fases 3 y 5 juntas en la Fase 6).

## Decisiones propias (tomadas por el equipo; no se consultan al docente)

1. Idioma: dominio en español; sufijos técnicos en inglés (interfaz `ReservarCitaUseCase`, implementación `ReservarCitaUseCaseImpl`, `FranjaRepositoryGateway`).
2. Spring Boot 3.5.16 (la especificación dice 3.x).
3. `MODIFICADA` fuera del CHECK de `cita`; el cambio de franja queda en `historial_cita` (recomendación del docente).
4. Códigos 422 y 503 además de los de la convención, porque los casos de uso los piden.
5. appointment consulta a catalog antes de abrir la transacción y reverifica la franja bajo bloqueo.
6. Candado `pg_advisory_xact_lock` por paciente contra citas cruzadas en franjas distintas.
7. El correo del paciente cuando cancela un administrativo se obtiene de auth por `/internal`.
8. Recordatorio: crear la fila al confirmar y añadir el estado CANCELADA a `notificacion`.
9. Los tokens de acceso revocados siguen válidos en otros servicios hasta expirar; mantener expiración corta.
10. `HorarioAtencion` vive en catalog (la especificación lo lista también en appointment).
11. auth: actualizar datos usa PATCH (valor nulo = sin cambio), y el administrador inicial sale de variables de entorno.
12. auth: política de claves propia (8 a 72 bytes, letra y dígito) y expiración de 15 minutos; sin refresh token (no está en la especificación).
13. Dominio con cero imports, también del JDK. Se infirió del ejemplo del docente (ids `String`, rol `String`, `Integer`, `long`); falta ver cómo resuelve él las búsquedas vacías, las listas y las fechas. Si lo hace distinto (por ejemplo con `Optional` o `List`), se ajusta el estilo.
14. Correos solo ASCII (se guardan en minúsculas ASCII; no se admiten correos con tildes o ñ).
15. Zona horaria: la JVM corre en UTC y "hoy" de la clínica sale de `app.timezone` (America/Bogota por defecto). Antes no había zona clínica explícita.
16. Desactivar una especialidad, centro o profesional es solo borrado lógico y no consulta si tiene citas futuras (la lista de reglas RN no se entregó). Si el docente quiere impedirlo con citas futuras, catalog necesitaría preguntar a appointment.
17. Catalog calcula las franjas y las envía a appointment (appointment solo las guarda); el horizonte sale del parámetro `horizonte_max_dias`. Si appointment no responde, el horario queda guardado y se reintenta.
18. Modificar un horario no retira las franjas ya generadas; se usa un bloqueo para dejar de ofrecerlas.
19. Catalog no comprueba en auth que el `usuarioId` de un profesional exista ni tenga rol PROFESIONAL (no hay llamada auth a catalog en la especificación); confía en el administrativo.
20. API interna adicional de catalog para CU-18 (`/internal/profesionales/{id}/asociacion`, `/internal/parametros`) y de appointment para CU-25/CU-26: la lista de comunicaciones de la especificación no incluye appointment a catalog.
21. Estructura del docente: `domain/usecase` (interfaces de casos de uso), `domain/gateway` (puertos de salida) y `application/usecase` (`*UseCaseImpl`). Antes eran `port/in` y `port/out` con sufijo `Port`.
22. Las horas del horario llegan y salen como `HH:mm`, con máximo 23:59 (no existe 24:00 en una columna TIME).
23. APIs externas elegidas por afinidad con el dominio: feriados (Nager.Date) y CIE-10 (NLM). Si el docente prefiere otras, se cambia el adaptador `rest_client`; el puerto en `domain/gateway` no cambia.
24. Cobertura 90 % medida con JaCoCo por módulo; reemplaza el 70 % del RNF-09.
25. El correo del paciente lo resuelve notification en auth (`/internal/usuarios/{id}/contacto`); la especificación decía que llegaba en la llamada, pero appointment solo envía ids (decisión 7).
26. Correo simulado por defecto (`MAIL_ENABLED=false`) para que el proyecto corra localmente sin credenciales SMTP.

## Supuestos abiertos

- La lista de reglas de negocio RN-xx (el enunciado cita RN-06, 09, 11, 13 y 17) no se entregó: se aplican las reglas descritas en los CU.
- Alcance y presupuesto del despliegue en AWS: se decide al llegar a la Fase 8.

## Pendientes menores

- Revisar `.github/modernize/` (restos de una herramienta de actualización de Java, sin relación con el proyecto).
- Skills de AWS: se copian al empezar la Fase 8. Las de interfaz `impeccable` y `apple-design` ya están en `.claude/skills/`.
