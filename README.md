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

2. Copiar `.env.example` a `.env`, completar las claves anteriores y exportarlas: `set -a; source .env; set +a`. Para `auth-service` hacen falta además `JWT_SECRET` (mínimo 32 caracteres, el mismo en todos los servicios) y, para crear el primer administrador, `ADMIN_EMAIL` y `ADMIN_PASSWORD`.
3. Arrancar cada servicio en su terminal:

```bash
mvn -pl auth-service spring-boot:run
```

Las migraciones de Flyway se aplican al arrancar. Swagger y la colección de Postman se agregan en la fase de cierre.
