# Appointy Med

Backend de gestión de agenda médica: cinco microservicios Spring Boot con Clean Architecture. Especificación completa en [docs/especificacion.md](docs/especificacion.md); estado en [docs/progreso.md](docs/progreso.md).

| Servicio | Puerto | Schema |
|---|---|---|
| auth-service | 8081 | auth |
| catalog-service | 8082 | catalog |
| appointment-service | 8083 | appointment |
| clinical-history-service | 8084 | clinical |
| notification-service | 8085 | notification |

## Requisitos

JDK 21, Maven 3.9+ y PostgreSQL local (sin Docker).

## Puesta en marcha

1. Crear la base, los schemas y los usuarios (una sola vez, con un superusuario de PostgreSQL):

```bash
sudo -u postgres psql -v pwd_auth=... -v pwd_catalog=... -v pwd_appointment=... -v pwd_clinical=... -v pwd_notification=... -f db/init.sql
```

2. Copiar `.env.example` a `.env`, completar las claves anteriores y exportarlas: `set -a; source .env; set +a`. Para `auth-service` hacen falta además `JWT_SECRET` (mínimo 32 caracteres, el mismo en todos los servicios) y, para crear el primer administrador, `ADMIN_EMAIL` y `ADMIN_PASSWORD`. Para `catalog-service`, además `INTERNAL_API_KEY` (mínimo 24 caracteres, la misma en todos los servicios) y, si appointment no corre en `http://localhost:8083`, `APPOINTMENT_SERVICE_URL`. Las fechas y horas se guardan en UTC; el "hoy" de la clínica sale de `APP_TIMEZONE` (por defecto `America/Bogota`).
3. Arrancar cada servicio en su terminal:

```bash
mvn -pl auth-service spring-boot:run
```

Las migraciones de Flyway se aplican al arrancar. Swagger y la colección de Postman se agregan en la fase de cierre.

## Interfaz web (`frontend/`)

React 19 + Vite + TypeScript. En desarrollo el front llama a los 5 servicios por un proxy (`/svc/<servicio>`), sin CORS. No hay `npm` instalado en esta máquina: se usa pnpm con corepack.

```bash
bash scripts/dev/demo.sh            # servicios + datos de ejemplo (Ctrl+C los detiene y borra todo)
cd frontend && corepack pnpm install && corepack pnpm dev   # http://localhost:5173
corepack pnpm test                  # pruebas del front (vitest)
corepack pnpm run build             # tsc + vite build
```

Cuentas de la demo (clave `Demo12345`): `paciente@demo.local`, `dra.rojas@demo.local`, `dr.mejia@demo.local`, `admin@demo.local`.
La documentación de la API está en `/swagger-ui/index.html` de cada servicio, en `docs/openapi/` y en la colección `docs/postman/`.
