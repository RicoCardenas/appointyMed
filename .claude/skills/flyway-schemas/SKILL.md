---
name: flyway-schemas
description: Reglas de base de datos de Appointy Med - un schema y un usuario por servicio, migraciones Flyway, ddl-auto validate y sin FK entre schemas. Usalo al crear o cambiar migraciones, entidades JPA, application.yml o db/init.sql (RNF-12, RNF-14, RNF-15).
---

# Flyway y schemas por servicio

Fuente de verdad: `docs/especificacion.md`, secciones 5 y 10.

## Estructura

- Una base `appointy`, un schema por servicio, un usuario por servicio. Los crea `db/init.sql` (idempotente); Flyway **no** crea schemas (`create-schemas: false`).
- Servicio, schema y usuario: auth/`auth`/`auth_svc`, catalog/`catalog`/`catalog_svc`, appointment/`appointment`/`appointment_svc`, clinical-history/`clinical`/`clinical_svc`, notification/`notification`/`notification_svc`.
- Las claves llegan por variable de entorno (`AUTH_DB_PASSWORD`, etc.), nunca en un `.yml` ni en el repositorio.

## Migraciones

- Ubicacion: `<servicio>/src/main/resources/db/migration`. La primera es `V1__init.sql` con todas las tablas del schema segun la seccion 10 de la especificacion.
- Nombres de tabla sin prefijo de schema: Flyway y Hibernate usan el schema por defecto del servicio.
- `ddl-auto: validate`: la entidad debe coincidir con la tabla. Tipos habituales: `UUID` para ids, `TIMESTAMP` para fechas de auditoria, `BIGINT NOT NULL DEFAULT 0` para `@Version`, `CHECK` para estados y roles.
- Una migracion ya aplicada no se edita: el cambio va en `V2__...`. Mientras solo exista en tu maquina puedes recrear el schema y corregir `V1`.
- Segunda linea de defensa en BD (RNF-14): UNIQUE, CHECK, FK dentro del schema, indice unico parcial de cita.

## Prohibido

- FK o consultas hacia el schema de otro servicio. Las referencias externas (`usuario_id`, `profesional_id`, `centro_id`, `paciente_id`, `cita_id`...) son columnas `UUID` sin FK ("logicas").
- Borrado fisico: usa `activo` o estados; historial y entradas clinicas son append-only.
- Permisos de un usuario sobre schemas ajenos.

## Verificar

1. Con una base limpia (`db/init.sql` aplicado) arrancar el servicio: Flyway aplica las migraciones y Hibernate valida sin errores.
2. Comprobar que existe `flyway_schema_history` en el schema del servicio y no en `public`.
3. Reiniciar: no debe re-aplicar nada.
