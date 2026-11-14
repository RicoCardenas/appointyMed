# Skills de terceros

Copiadas sin modificar (salvo la nota indicada) de https://github.com/giuseppe-trisciuoglio/developer-kit
(licencia MIT, Copyright (c) 2025 Giuseppe Trisciuoglio), carpeta plugins/developer-kit-java/skills, commit fe73fb33a09e.
Se omitieron los scripts y assets ejecutables (.sh).

- clean-architecture: se agregó una nota al inicio (la estructura de paquetes de la especificación prevalece).
- En todas se eliminó la línea `allowed-tools` del encabezado para no pre-aprobar herramientas.
- spring-boot-security-jwt, spring-boot-event-driven-patterns, spring-boot-test-patterns,
  spring-boot-rest-api-standards, spring-data-jpa, unit-test-service-layer.

Propias del proyecto: reserva-concurrente, flyway-schemas.

## Skills de interfaz (Fase 7)

- apple-design: https://github.com/emilkowalski/skills (licencia MIT, Copyright (c) 2026 Emil Kowalski), commit 0f2e23b. Copiada sin cambios.
- impeccable: https://github.com/pbakaus/impeccable (licencia Apache 2.0, ver `impeccable/LICENSE`), commit 1995764, carpeta plugin/skills/impeccable v4.9.1. Se copiaron `SKILL.md` y `reference/`; se omitió `scripts/` (lanzador que descarga un binario y scripts JS) y no se activan hooks. Los comandos de la skill que dependen de esos scripts (`context`, `live`, `pin`, detección automática) no funcionan; el resto de la guía de diseño sí.
