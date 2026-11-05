# Appointy Med: progreso

Rama `main`. El código anterior está en el historial hasta el commit `efb6897`; consúltalo con `git show efb6897:<ruta>`. Última actualización: 2026-11-05.

## Fases

| Fase | Contenido | Estado |
|---|---|---|
| 0 | Base: especificación, POM de 5 módulos, schemas, skills, documentación | Hecha y verificada (ver abajo) |
| 1 | auth-service: CU-01 a CU-07 | Hecha y verificada (ver abajo) |
| 2 | catalog-service: CU-08 a CU-16 | Hecha y verificada con appointment simulado (ver abajo) |
| 3 | appointment-service: CU-17 a CU-27 y prueba de concurrencia | Pendiente |
| 4 | clinical-history-service: CU-28 a CU-32 | Pendiente |
| 5 | notification-service: CU-33 a CU-35 | Pendiente |
| 6 | Cierre: OpenAPI, colección Postman, cobertura 70 %, correlación de logs | Pendiente |
| 7 | Interfaz (proyecto aparte en `frontend/`) | Pendiente, por decidir stack |
| 8 | Despliegue en AWS | Pendiente, por decidir alcance y presupuesto |

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
21. Las horas del horario llegan y salen como `HH:mm`, con máximo 23:59 (no existe 24:00 en una columna TIME).

## Pedir al docente

- La lista de reglas de negocio RN-xx (el enunciado cita RN-06, 09, 11, 13 y 17).
- Si el despliegue en AWS se evalúa y con qué presupuesto.

## Pendientes menores

- Revisar `.github/modernize/` (restos de una herramienta de actualización de Java, sin relación con el proyecto).
- Skills de interfaz y AWS: se copian al empezar sus fases (ver `CLAUDE.md`).
