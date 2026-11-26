# Appointy Med: progreso

Rama `main`. El código anterior está en el historial hasta el commit `efb6897`; consúltalo con `git show efb6897:<ruta>`. Última actualización: 2026-11-14.

## Siguiente paso: Fase 8, subir a Render con `render.yaml` (pasos en el README)

Antes de ejecutar nada sobre la cuenta: estimar costo y pedir confirmación (CLAUDE.md). Pendientes de la Fase 7 que conviene decidir primero: un proxy inverso en producción que reproduzca los prefijos `/svc/<servicio>` del front (hoy los hace Vite en desarrollo), y la política de la sesión de 15 minutos sin refresh token.

- Pendiente del equipo: `JWT_SECRET`, `INTERNAL_API_KEY` y `NOTIFICATION_DB_PASSWORD` en `.env`; opcional `MAIL_ENABLED=true` para correo real: con `MAIL_PROVIDER=brevo` (API HTTP por 443, recomendado en Render) `BREVO_API_KEY` y `MAIL_FROM` (remitente verificado en Brevo); con `MAIL_PROVIDER=smtp` (por defecto) `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM`.
- Si Maven falla con "Unresolved compilation problems" o "cannot access ...": la extensión Java de VS Code compila en `target/` a la vez. Correr Maven sobre una copia (ver `CLAUDE.md`, "Comandos").

## Fases

| Fase | Contenido | Estado |
|---|---|---|
| 0 | Base: especificación, POM de 5 módulos, schemas, skills, documentación | Hecha y verificada (ver abajo) |
| 1 | auth-service: CU-01 a CU-07 | Hecha y verificada (ver abajo) |
| 2 | catalog-service: CU-08 a CU-16 | Hecha y verificada con appointment simulado (ver abajo) |
| 3 | appointment-service: CU-17 a CU-27 y prueba de concurrencia | Hecha (ver abajo) |
| 4 | clinical-history-service: CU-28 a CU-32 | Hecha (ver abajo) |
| 5 | notification-service: CU-33 a CU-35 | Hecha y verificada (ver abajo) |
| 6 | Cierre: OpenAPI, colección Postman, correlación de logs | Hecha y verificada (ver abajo) |
| 6b | Requisitos RA-03 a RA-06: 5 APIs no CRUD y GET all paginado por servicio, 2 APIs externas, cobertura 90 % | Hecha y verificada (ver abajo) |
| 7 | Interfaz (proyecto aparte en `frontend/`), con las skills `impeccable` y `apple-design` | Hecha y verificada en navegador (ver abajo) |
| 8 | Despliegue (Render gratuito, para la sustentación) | Archivos listos y probados en local con Docker; falta subirlo a Render |

## Requisitos adicionales (RA-01 a RA-07, ver especificación sección 11)

| Requisito | Estado |
|---|---|
| RA-01, RA-02 (3 o más servicios, una base) | Cumplidos: 5 servicios, base `appointy`. |
| RA-03 (5 APIs que no son CRUD por servicio) | Cumplido, ver tabla. |
| RA-04 (GET all paginado por servicio) | Cumplido: `page` (desde 0), `size` (1 a 100, por defecto 20) y filtros; respuesta `contenido`, `pagina`, `tamano`, `totalElementos`, `totalPaginas`. |
| RA-05 (cobertura 90 %) | Cumplido y exigido por Maven: JaCoCo `check` en `verify`, 90 % de líneas por módulo. |
| RA-06 (2 APIs externas) | Cumplido: festivos (Nager.Date) en catalog y CIE-10 (NLM) en clinical-history. |
| RA-07 (front) | Cumplido: `frontend/`, ver Fase 7. |

APIs por servicio (las que no son CRUD; hay más de 5 en cada uno):

| Servicio | No CRUD | GET all paginado |
|---|---|---|
| auth | POST `/auth/login`, `/auth/logout`, `/auth/cambiar-clave`; POST `/admin/usuarios/{id}/activar` y `/desactivar`; GET `/admin/usuarios/estadisticas`; GET `/internal/usuarios/{id}/contacto` | GET `/admin/usuarios?rol=&activo=` |
| catalog | POST `/horarios/{id}/franjas`, POST `/bloqueos`; GET `/especialidades/{id}/profesionales`, `/profesionales/{id}/centros`; PUT `/profesionales/{id}/especialidades` y `/centros`; GET `/festivos?anio=` (externa) | GET `/profesionales?texto=&activo=` |
| appointment | POST `/citas`, `/citas/{id}/reprogramar`, `/citas/{id}/cancelar`; GET `/disponibilidad`, `/citas/agenda`, `/citas/mias`, `/citas/{id}/historial`, `/citas/estadisticas`; PATCH `/citas/{id}/estado` | GET `/citas?estado=&profesionalId=&pacienteId=` (ADMINISTRATIVO) |
| clinical-history | GET `/pacientes/{id}/resumen`, `/historia/mia/resumen`, `/entradas/{id}/versiones`, `/diagnosticos?q=` (externa); POST `/entradas/{id}/correcciones` | GET `/atenciones` (PROFESIONAL: las que escribió) |
| notification | POST `/notificaciones/{id}/reenviar`, `/recordatorios/ejecutar`, `/reintentos/ejecutar`; GET `/estadisticas`, `/cita/{citaId}` | GET `/notificaciones?estado=&tipo=&citaId=` |

## Fase 6b: requisitos adicionales

- Festivos (catalog): `FestivosClientGateway` con adaptador a `https://date.nager.at/api/v3/PublicHolidays/{año}/CO` (sin llave, caché Caffeine 24 h, timeouts 2 s y 3 s). Al generar franjas se retiran las que caen en festivo; si la API no responde se generan todas y el mensaje lo avisa. Las franjas ya generadas no se retiran (como en la decisión 18). Variables: `FESTIVOS_API_URL`, `FESTIVOS_PAIS`, `FESTIVOS_TTL_HORAS`.
- CIE-10 (clinical): `Cie10ClientGateway` con adaptador a `https://clinicaltables.nlm.nih.gov/api/icd10cm/v3/search` (sin llave, caché 24 h). Solo sale el término (2 a 80 caracteres, letras, dígitos, espacio, punto, guion, coma); nunca ids ni datos del paciente, y no se registra. Es solo ayuda de búsqueda: el diagnóstico del registro sigue siendo texto libre. Variables: `CIE10_API_URL`, `CIE10_TTL_HORAS`.
- Los listados nuevos usan `Specification` de Spring Data con orden fijo y desempate por id. En clinical, cada paciente cuya historia aparece en una página de `/atenciones` queda en el log de acceso.
- Auth: un administrativo no puede desactivar su propia cuenta (409). Desactivar no revoca los tokens ya emitidos (decisión 9). Cambiar la clave con la actual incorrecta es 400 `CLAVE_ACTUAL_INCORRECTA`, no 401, para que el front no cierre la sesión.
- Notification: el historial por cita se ordena por fecha de creación y, en empate, por tipo.

## Validado en la Fase 6b

- `mvn verify` de los 5 módulos: BUILD SUCCESS, 0 fallos, "All coverage checks have been met". Cobertura de líneas: auth 98,0 %, catalog 97,3 %, appointment 96,0 %, clinical-history 97,5 %, notification 97,8 %. JaCoCo excluye solo `*Application`, `application/config`, `infrastructure/config`, `SecurityConfig`, entidades y los `*MapperImpl` generados por MapStruct; la seguridad y la configuración se verifican en las pruebas reales.
- Pruebas reales contra PostgreSQL: auth 73, catalog 131, appointment 140, clinical 120, notification 46 y `externas.sh` 18 (red real), todas correctas. La prueba de concurrencia de reserva (5 casos, base real) también pasa.
- Defectos reales encontrados y corregidos: el CIE-10 devolvía 0 resultados sin `sf=code,name` (lo vio la prueba con la API real); el historial por cita salía en orden inestable; un test de ruta esperaba 405 donde ahora el administrativo tiene `GET /admin/usuarios`.
- Las pruebas de catalog y appointment fijan `FESTIVOS_API_URL` a un puerto cerrado para que los conteos de franjas no dependan del calendario real; con la API real los festivos recortan franjas (se vio: 36 esperadas, 28 reales).
- No probado: carga, dos instancias a la vez, SMTP real.

## Fase 6: cierre

- OpenAPI (RNF-11): `springdoc` en los 5 servicios. `/v3/api-docs` y `/swagger-ui/index.html` públicos (la especificación no abre la API: las rutas siguen pidiendo JWT); solo se documenta `/api/v1`, no `/internal`. Esquema `bearer` JWT global; `register` y `login` sin candado. Cada controlador tiene `@Tag` y cada ruta `@Operation(summary)` con su rol. `OPENAPI_ENABLED=false` lo apaga (por ejemplo en producción).
- Especificaciones en `docs/openapi/<servicio>.json` y colección en `docs/postman/` (62 peticiones, una carpeta por servicio y por etiqueta, entorno con los 5 puertos, `{{token}}` que guarda la petición de login y un `X-Correlation-Id` nuevo en cada llamada). Se regeneran con `bash scripts/postman/generar.sh` (levanta los servicios, descarga las especificaciones y corre `generar_coleccion.py`); no siembra el administrador de `.env`.
- Correlación (RNF-17): el id del cliente (o uno generado) va en la respuesta y en cada línea de log; los clientes REST entre servicios lo reenvían en `X-Correlation-Id` (probado con servidores simulados en cada adaptador). Real: `notification.sh` comprueba el id en el log y en la respuesta.

## Validado en la Fase 6

- `mvn verify`: BUILD SUCCESS, 0 fallos y cobertura sobre 90 % en los 5 módulos (la configuración de OpenAPI está excluida como el resto de `infrastructure/config`).
- `auth.sh` 84 comprobaciones correctas (incluye OpenAPI público, sin `/internal`, esquema bearer, Swagger UI, y que publicar la especificación no abre la API) y `notification.sh` 49, ambas contra PostgreSQL.
- Incidente corregido: la primera versión de `generar.sh` arrancó auth con `ADMIN_EMAIL` de `.env` y sembró ese administrador en la base real; se borró y el script ya lo anula.
- No probado: importar la colección en Postman (se comprobó su estructura, no la aplicación), la correlación de punta a punta a través de los 3 saltos con servicios reales, y CORS.

## Fase 7: interfaz web

- Stack: React 19, Vite 8, TypeScript, React Router, lucide-react, Figtree autoalojada; sin librería de componentes. `PRODUCT.md` y `DESIGN.md` en la raíz recogen el producto y el sistema de diseño (superficie Operate, teal clínico restringido, OKLCH, claro y oscuro, una sola familia tipográfica, movimiento solo para estado, sin coreografía de carga).
- Estructura: `src/api` (cliente con JWT, `X-Correlation-Id` y errores uniformes; un archivo por servicio), `src/sesion` (sesión en `sessionStorage`, cierre automático al vencer el token de 15 minutos con aviso), `src/componentes` (botón, campos, aviso, tabla paginada, diálogo nativo, toasts), `src/paginas/{publicas,paciente,profesional,admin}`.
- Paciente: reservar en un recorrido (especialidad, profesional, centro, fecha, hora; un 409 recarga las franjas libres), mis citas (próximas y pasadas, reprogramar con otra franja, cancelar con confirmación), mi historia con resumen, perfil y cambio de clave, registro.
- Profesional: agenda por día o desde hoy, registrar atención con búsqueda CIE-10 (API externa, se puede escribir a mano si falla), marcar atendida o no asistió, mis atenciones paginadas con corrección, historia de un paciente (403 explicado).
- Administrativo: citas (estadísticas, filtro, paginado, cancelar), cuentas (estadísticas, filtros, crear personal, activar y desactivar), profesionales (búsqueda, crear, especialidades, centros y horarios con generación de franjas), catálogo (especialidades, centros, festivos), notificaciones (estadísticas, filtros, reenviar, ejecutar jobs), parámetros.
- Cambio en el back: `GET /api/v1/centros` (ADMINISTRATIVO) en catalog, porque no había forma de listar centros para asociarlos. OpenAPI y Postman regenerados (63 peticiones).
- Nombres de pacientes (agregado después): `POST /api/v1/pacientes/nombres` en auth (PROFESIONAL y ADMINISTRATIVO, hasta 50 ids) devuelve solo nombres y apellidos; el front lo usa en agenda, atenciones, historia y citas de administración y, si no responde, muestra el id corto. Decisión propia: no verifica relación profesional-paciente porque auth no la conoce; los ids que un profesional conoce salen de sus propias citas y son UUID. Cédula, contacto y nacimiento no se exponen.
- Límites de datos que el front respeta y no inventa: los centros de una cita se resuelven por los centros del profesional, la especialidad por el catálogo activo.
- Demo: `scripts/dev/demo.sh` levanta los servicios con secretos temporales, siembra especialidades, centros, 2 profesionales con horarios y 1 paciente, y al terminar borra todo (incluidas las tablas clínicas de solo inserción, con el mismo método de los scripts e2e).

## Validado en la Fase 7

- `corepack pnpm test`: 25 pruebas (cliente HTTP, formato, sesión con vencimiento, login, rutas por rol, tabla paginada); `tsc` sin errores y `vite build` correcto (337 kB de JS, 101 kB comprimido).
- En el navegador integrado, contra los 5 servicios reales y PostgreSQL: login de paciente, reserva completa (franjas reales, confirmación), mis citas con nombres resueltos, login de profesional, registro de atención con la búsqueda CIE-10 real, y como administrativo citas con estadísticas y notificaciones (confirmación enviada y recordatorio programado a las 24 h). Tema claro y oscuro, y vista móvil.
- Defectos encontrados al probar y corregidos: los botones chicos perdían sus estilos base (`className` pisaba la clase), y la resolución de especialidades pedía las inactivas (403 para pacientes).
- `catalog.sh` 134 correctas con los centros; `generar.sh` regenera la colección.
- No probado: reprogramar y cancelar desde la interfaz, la administración de profesionales y horarios en el navegador (probadas por API y con pruebas de componentes solo en parte), lector de pantalla, Safari y Firefox, y la sesión de 15 minutos de punta a punta.

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
- Correo: por defecto `MAIL_ENABLED=false` (adaptador simulado: marca ENVIADA y registra solo el asunto). Con `true` usa SMTP (`spring-boot-starter-mail`, timeouts de 5 s) o, con `MAIL_PROVIDER=brevo`, la API HTTP de Brevo (`CorreoBrevoApiAdapter`, POST `/v3/smtp/email`, timeouts 3 s y 8 s). El indicador de salud de correo está desactivado para que una caída del SMTP no marque el servicio como DOWN.
- La API enmascara el correo (`p***@dominio`) y no expone el cuerpo (RNF-05). El cuerpo solo lleva fecha, hora y código de la cita; no consulta a catalog.
- Los jobs asumen una sola instancia (sin bloqueo de lote).

## Validado en la Fase 5

- `mvn -pl notification-service test`: 39 pruebas; auth pasa de 64 a 68 (caso de uso de contacto y filtro de llave). Incluyen el cliente HTTP a auth (llave, correlación, 404, 5xx, timeout y servicio caído) y la regla de capas.
- `bash scripts/e2e/notification.sh` (auth real + notification real, PostgreSQL local, correo simulado): 46 comprobaciones correctas, 0 fallidas. Cubre la seguridad de `/internal`, los tres tipos de CU-33, idempotencia, creación y cancelación de recordatorios, paginación y filtros, roles, CU-34, CU-35 y que el log no contiene correos ni cuerpos.
- Defectos reales encontrados y corregidos: `/actuator/health` quedaba DOWN por el indicador de correo; una fecha con formato válido pero inexistente (`2026-13-45`) daba 500 en vez de 400; el enmascarado de un correo sin `@` lo devolvía completo.
- No probado: envío por SMTP real, dos instancias a la vez, appointment real llamando a notification (la integración se prueba con las Fases 3 y 5 juntas en la Fase 6).

## Decisiones propias (tomadas por el equipo; no se consultan al docente)

1. Idioma: dominio en español; sufijos técnicos en inglés (`CitaUseCase`, `FranjaRepositoryGateway`).
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
21. Estructura del docente: `domain/usecase` y `domain/gateway` (puertos de salida, única interfaz). Antes eran `port/in` y `port/out` con sufijo `Port`. Desde 2026-11-21 los casos de uso están en `domain/usecase` (instrucción obligatoria del docente), sin interfaz ni `*Impl` y sin imports ajenos al dominio (transacción, reloj, ids y log por puertos; fechas con `FechaIso`; arreglos). Una clase por tema (auth: `SesionUseCase`, `CuentaUseCase`, `PerfilUseCase`; catalog: Especialidad, Centro, Profesional, Horario, Parametro; appointment: `CitaUseCase`, `ConsultaCitaUseCase`, `FranjaUseCase`; clinical-history: `EntradaClinicaUseCase`, `HistoriaUseCase`; notification: `NotificacionUseCase`, `ConsultaNotificacionUseCase`), con comandos como `record` anidados y auxiliares (`Busquedas`, `Fechas`, etc.). Los controladores inyectan la clase directamente. Las pruebas siguen AAA con mocks y cobertura >= 90 % por módulo.
22. Las horas del horario llegan y salen como `HH:mm`, con máximo 23:59 (no existe 24:00 en una columna TIME).
23. APIs externas elegidas por afinidad con el dominio: feriados (Nager.Date) y CIE-10 (NLM). Si el docente prefiere otras, se cambia el adaptador `rest_client`; el puerto en `domain/gateway` no cambia.
24. Cobertura 90 % medida con JaCoCo por módulo (líneas); reemplaza el 70 % del RNF-09. Exclusiones: configuración, entidades, `*Application` y mappers generados.
27. Las pruebas de controladores usan MockMvc sin contexto de Spring (`standaloneSetup`) y las de adaptadores JPA usan repositorios simulados: así corren sin base de datos. La autorización por rol se prueba en los scripts `scripts/e2e/*.sh`.
25. El correo del paciente lo resuelve notification en auth (`/internal/usuarios/{id}/contacto`); la especificación decía que llegaba en la llamada, pero appointment solo envía ids (decisión 7).
26. Correo simulado por defecto (`MAIL_ENABLED=false`) para que el proyecto corra localmente sin credenciales SMTP.
28. Mejoras del front en la rama `front-mejoras` (2026-11-26): (a) Reservar avisa si el día elegido es festivo, con el nombre que da `GET /festivos` (Nager.Date), y oculta las horas porque el servicio no agenda en festivos; si la API de festivos falla se ignora y queda el aviso general. appointment no conoce los festivos: solo catalog los omite al generar franjas. (b) Las cifras de Citas, Cuentas y Notificaciones son filtros (componente `Cifras`). (c) Botón de tema con tres estados (automático, claro, oscuro) guardado en `localStorage`, y tokens con `light-dark()`. (d) Campos de clave con botón mostrar u ocultar. (e) Tamaños: cuerpo 16 px, controles de 44 px y botones chicos de 44 px en pantallas táctiles. (f) Login y registro con panel de marca; Perfil con cabecera de identidad; Agenda agrupada por día; horario semanal agrupado por día en Profesionales; pestañas de Catálogo con teclado (flechas, Home y End).
29. Correos más claros (2026-11-26, solo notification-service; sin endpoints nuevos: siguen 64 públicos y 9 internos). El cuerpo ahora usa fecha legible ("miércoles 2 de diciembre de 2026"), hora de 12 horas, código corto de 8 caracteres y una indicación por tipo; el asunto incluye el tipo y el momento de la cita. `FormatoFecha` calcula el día de la semana a mano para mantener el dominio sin imports. El adaptador de Brevo envía además `htmlContent` (`PlantillaCorreoHtml`, tablas y estilos en línea, todo escapado) con botón "Ver mis citas" si está `APP_FRONT_URL` (en `render.yaml`); el texto plano queda como respaldo y es lo que se guarda en BD. Pendiente (no se hizo): nombres del paciente y del profesional, centro y especialidad, que exigen endpoints internos nuevos en catalog y auth.

## Supuestos abiertos

- La lista de reglas de negocio RN-xx (el enunciado cita RN-06, 09, 11, 13 y 17) no se entregó: se aplican las reglas descritas en los CU.
- Alcance y presupuesto del despliegue en AWS: se decide al llegar a la Fase 8.

- Correo por API HTTP de Brevo (2026-11-25): Render gratuito bloquea los puertos SMTP 25, 465 y 587, así que se añadió un segundo adaptador de `CorreoGateway` por HTTPS (443) elegido con `MAIL_PROVIDER`. El dominio no cambia. Se descartó probar el SMTP por 2525 para no depender de algo no confirmado.

- Despliegue en Render gratuito (2026-11-25), solo para la sustentación; sustituye al plan de AWS. Un `Dockerfile` por servicio (contexto: raíz; JVM con `-Xmx300m`), `frontend/Dockerfile` con Nginx que hace de proxy `/svc/<servicio>/api/` y no expone `/internal`, y `render.yaml`. Los servicios se hablan por sus URL públicas `onrender.com` con la API key (la red privada no se confirmó en el plan gratuito). En Render todos usan el usuario de la base y `FLYWAY_CREATE_SCHEMAS=true` (RNF-15 se cumple en local con `db/init.sql`, no aquí). La BD se arma con `DB_HOST`, `DB_PORT` y `DB_NAME`. Verificado en local con `docker compose`: login por el proxy, SPA y los 5 servicios con JWT; `render.yaml` sin validar en Render (`fromGroup` y `fromDatabase` por confirmar).
- Pendiente de seguridad: límite de intentos en `/auth/login`.

- Pruebas de calidad para la entrega (2026-11-25): `scripts/postman/generar_pruebas.py` genera en `entregables/postman/` una colección con 67 peticiones y 254 aserciones (código HTTP, formato y tiempo < 500 ms) y los entornos local y Render; `scripts/postman/ejecutar.mjs` la corre sin Newman. `scripts/carga/carga.py` hace las pruebas de carga y estrés (lectura, login, reservas y contención) y `scripts/carga/informe.py` convierte los resultados en fragmentos para el artículo IEEE en `entregables/articulo/`. `docker-compose.pruebas.yml` publica los puertos y `docker-compose.render.yml` limita cada servicio a 0,1 CPU y 512 MB (plan gratuito de Render).
- Defecto encontrado con la prueba en Docker: con un solo usuario de BD el SQL nativo de franjas no encontraba la tabla (dependía del `search_path` por rol de `db/init.sql`). Corregido con `spring.datasource.hikari.connection-init-sql: SET search_path TO <schema>` en los 5 servicios. En Render hay que volver a desplegar los servicios con este cambio.
- Resultados: sin límites, 0 errores hasta 2000 hilos de lectura (p99 354 ms) y la invariante de reserva se cumple en 10 rondas de 20 competidores; el login (BCrypt) toma unos 217 ms y satura cerca de 37/s. Con 0,1 CPU el requisito de 500 ms solo se cumple con un usuario a la vez (el login tarda unos 8,5 s). Los tiempos entre servicios en `render.yaml` se ampliaron a 10 s de conexión y 60 s de lectura.
- Posible mejora, no aplicada: bajar el costo de BCrypt o limitar su concurrencia; con 0,1 CPU el login es el cuello de botella de la demo.

- Servicios dormidos en Render (2026-11-25): el front pide `/svc/<servicio>/actuator/health` de los 5 servicios al abrirse (`calentarServicios`, rutas de salud en Nginx) y un 502/503/504 sin error uniforme se muestra como "El servicio se esta despertando" (`SERVICIO_DESPERTANDO`) en lugar de "error inesperado". 33 pruebas del front pasan.

- Modelo de datos (2026-11-25): MER de los 5 schemas (17 tablas) en el README (Mermaid) y en el artículo IEEE (figura TikZ); el artículo suma 6 figuras de resultados (cobertura, Postman, latencia, rendimiento, login y contención).

## Pendientes menores

- Revisar `.github/modernize/` (restos de una herramienta de actualización de Java, sin relación con el proyecto).
- Skills de AWS: se copian al empezar la Fase 8. Las de interfaz `impeccable` y `apple-design` ya están en `.claude/skills/`.
