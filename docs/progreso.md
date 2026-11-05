# Appointy Med: progreso

Rama `main`. El código anterior está en el historial hasta el commit `efb6897`; consúltalo con `git show efb6897:<ruta>`. Última actualización: 2026-11-05.

## Fases

| Fase | Contenido | Estado |
|---|---|---|
| 0 | Base: especificación, POM de 5 módulos, schemas, skills, documentación | Hecha y verificada (ver abajo) |
| 1 | auth-service: CU-01 a CU-07 | Hecha y verificada (ver abajo) |
| 2 | catalog-service: CU-08 a CU-16 | Pendiente |
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

- `mvn -pl auth-service test`: 50 pruebas (JUnit 5 y Mockito) sin fallos. Cubren los 9 casos de uso, la política de claves, el normalizador de correo, el emisor JWT (vigencia, firma, emisor, secreto corto) y que el dominio no importe frameworks y la aplicación no importe infraestructura.
- Prueba de punta a punta con `curl` contra PostgreSQL 18.6 real: 51 comprobaciones correctas, 0 fallidas. Flyway aplicó `V1`, Hibernate validó contra las tablas, los 7 CU respondieron con los códigos esperados (incluidos 401, 403, 404 y 409), las claves quedan en BCrypt, ni la clave ni el token ni datos personales aparecen en el log. Los datos de prueba se borraron al terminar.
- No probado: carga o concurrencia sobre el registro (la unicidad se apoya en las restricciones de BD), expiración real del job de limpieza (solo prueba unitaria), arranque con claves reales en `.env`.

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

## Pedir al docente

- La lista de reglas de negocio RN-xx (el enunciado cita RN-06, 09, 11, 13 y 17).
- Si el despliegue en AWS se evalúa y con qué presupuesto.

## Pendientes menores

- Revisar `.github/modernize/` (restos de una herramienta de actualización de Java, sin relación con el proyecto).
- Skills de interfaz y AWS: se copian al empezar sus fases (ver `CLAUDE.md`).
