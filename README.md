# AppointyMed - Medical Appointment Management System

A microservices-based medical appointment management system built with **Clean Architecture** (Domain, Application, Infrastructure), **SOLID principles**, and **Domain-Driven Design (DDD)** using Spring Boot 3.2.x, Java 17/21, PostgreSQL, Flyway, AWS SQS, and Maven.

---

## 🏛️ Architecture Overview

```
appointyMed/
├── auth-service/           # Authentication & Authorization microservice (Port: 8081) [✓ Implemented]
├── appointment-service/    # Core appointment, doctor, patient & schedule service (Port: 8082) [✓ Implemented]
│   └── src/main/resources/static/index.html # Interactive Visual Web Testing Dashboard
├── notification-service/   # SQS notification consumer (Port: 8083) [✓ MVP]
├── docker-compose.yml      # Multi-database PostgreSQL + LocalStack AWS SQS + pgAdmin
└── pom.xml                 # Maven Multi-Module Root POM
```

### 🧩 Clean Architecture Layers (Per Microservice)

```
src/main/java/com/appointymed/{service}/
├── domain/                 # 100% Framework-agnostic enterprise business rules
│   ├── model/              # Entities, Value Objects (Email, Password, TimeSlot, etc.)
│   ├── usecase/            # Atomic Use Cases (Single Responsibility Principle)
│   ├── repository/         # Repository Interfaces (Ports / Dependency Inversion)
│   └── exception/          # Pure Domain Exceptions
├── application/            # Application business rules
│   ├── service/            # Application Orchestration Services
│   ├── dto/                # Data Transfer Objects (Java Records)
│   ├── mapper/             # Domain <-> DTO Mappers
│   └── port/               # Ports
└── infrastructure/         # Frameworks & External Drivers
    ├── persistence/        # JPA Entities, Spring Data Repositories, Adapters
    ├── security/           # Spring Security Config, JWT Filter, BCrypt
    ├── config/             # OpenAPI Swagger Config, AWS SQS Client Config
    ├── controller/         # REST Controllers & GlobalExceptionHandler
    ├── messaging/          # AWS SQS Event Publishers
    └── static/             # Web UI / Testing Dashboard
```

---

## 🚀 Implemented Microservices & Modules

### 1. `auth-service` (Port 8081)
- **Cuentas y Autenticación:** Registro de usuarios, login con JWT (Access & Refresh tokens), BCrypt hashing.
- **Seguridad y Sesión:** Rotación de refresh tokens, logout seguro.
- **Recuperación de Acceso:** Solicitud de reset de contraseña y publicación de eventos en AWS SQS.
- **Control de Acceso (RBAC):** Roles preconfigurados (`ADMIN`, `DOCTOR`, `PATIENT`, `RECEPTIONIST`) y permisos granulares.
- **Auditoría:** Registro de trazas y acciones críticas en PostgreSQL.
- **Eventos:** Publicación con envelope común v1 hacia `notification-service`.

### 2. `appointment-service` (Port 8082)
- **Especialidades Médicas:** Creación y catálogo de especialidades con duración configurable.
- **Gestión de Médicos:** Registro de médicos con tarjeta profesional, especialidad y consultorio.
- **Configuración de Agendas:** Configuración de horarios semanales por médico (días, horas, duración de slots).
- **Gestión de Pacientes:** Registro con documento de identidad, datos de contacto y contacto de emergencia.
- **Disponibilidad en Tiempo Real:** Cálculo dinámico de slots libres vs. ocupados.
- **Reserva de Citas Médicas:** Reserva con validación anti-solapamiento y bloqueo de citas duplicadas.
- **Reprogramación y Cancelación:** Cambios de horario con validación de agenda y cancelación con motivo.
- **Historial y Próximas Citas:** Filtros por paciente y médico.
- **Notificaciones Asíncronas:** Publicación de eventos `APPOINTMENT_BOOKED`, `APPOINTMENT_CANCELLED`, `APPOINTMENT_RESCHEDULED` en SQS.
- **Dashboard Visual:** Interfaz web interactiva en `http://localhost:8082/index.html`.

### 3. `notification-service` (Port 8083)
- **Consumo de eventos:** Lee envelope v1 desde SQS `notification-queue`.
- **Eventos soportados:** Recuperación de contraseña, registro de usuario y cambios de citas.
- **Idempotencia:** Evita reprocesar eventos durante la ejecución del servicio.
- **Entrega MVP:** Simula envío mediante logs; proveedor real queda pendiente.
- **Errores:** Reintentos SQS y DLQ después de tres intentos.

---

## 📡 REST API Endpoints

### Auth Service (`http://localhost:8081/api/v1`)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/auth/register` | Registrar nuevo usuario |
| POST | `/api/v1/auth/login` | Iniciar sesión y obtener tokens JWT |
| POST | `/api/v1/auth/refresh` | Renovar Access Token |
| POST | `/api/v1/auth/logout` | Revocar Refresh Token |
| POST | `/api/v1/auth/forgot-password` | Solicitar recuperación de contraseña (SQS) |
| POST | `/api/v1/auth/reset-password` | Reestablecer contraseña con token |
| POST | `/api/v1/auth/change-password` | Cambiar contraseña autenticado |
| GET | `/api/v1/users` | Listar usuarios (Admin) |
| POST | `/api/v1/users/{id}/roles` | Asignar rol a usuario |
| DELETE | `/api/v1/users/{id}/roles/{role}` | Revocar rol a usuario |
| GET | `/api/v1/users/{id}/permissions` | Consultar permisos de usuario |

### Appointment Service (`http://localhost:8082/api/v1`)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/v1/specialties` | Crear especialidad médica |
| GET | `/api/v1/specialties` | Listar todas las especialidades activas |
| POST | `/api/v1/doctors` | Registrar médico |
| GET | `/api/v1/doctors` | Listar médicos (filtro por especialidad) |
| GET | `/api/v1/doctors/{id}` | Obtener perfil de médico |
| PUT | `/api/v1/doctors/{id}/schedules` | Configurar agenda semanal del médico |
| GET | `/api/v1/doctors/{id}/availability?date=YYYY-MM-DD` | Consultar slots libres/ocupados para una fecha |
| POST | `/api/v1/patients` | Registrar nuevo paciente |
| GET | `/api/v1/patients` | Listar pacientes |
| GET | `/api/v1/patients/{id}` | Obtener detalle de paciente |
| POST | `/api/v1/appointments` | Reservar nueva cita médica |
| POST | `/api/v1/appointments/{id}/cancel` | Cancelar cita médica con motivo |
| POST | `/api/v1/appointments/{id}/reschedule` | Reprogramar fecha/hora de cita médica |
| GET | `/api/v1/appointments/patient/{patientId}` | Listar citas de un paciente |
| GET | `/api/v1/appointments/patient/{patientId}/upcoming` | Próximas citas de un paciente |
| GET | `/api/v1/appointments/patient/{patientId}/history` | Historial de citas de un paciente |
| GET | `/api/v1/appointments/doctor/{doctorId}` | Consultar citas de un médico |

---

## 🛠️ Cómo Ejecutar y Probar el Proyecto

Responsabilidades y límites por microservicio: [`docs/architecture/services.md`](docs/architecture/services.md).
Contrato de eventos y política de pagos: [`docs/architecture/event-contracts.md`](docs/architecture/event-contracts.md).

### 1. Iniciar Infraestructura Docker (PostgreSQL & LocalStack AWS SQS)
```bash
docker-compose up -d
```
Esto iniciará:
- **PostgreSQL 16** (puerto `5433` con `auth_db` y `appointment_db`).
- **LocalStack AWS** (puerto `4566` para SQS, SNS, SES, STS).
- **pgAdmin 4** (puerto `5050` - `admin@appointymed.com` / `admin`).

### 2. Compilar y Ejecutar Pruebas
```bash
mvn clean test
```

### 3. Ejecutar los Microservicios
En terminal 1:
```bash
cd auth-service
mvn spring-boot:run
```

En terminal 2:
```bash
cd appointment-service
mvn spring-boot:run
```

En terminal 3:
```bash
cd notification-service
mvn spring-boot:run
```

### 4. Probar Visualmente
- **Portal Web de Gestión y Pruebas Visuales:** `http://localhost:8082/index.html`
- **Swagger UI (Appointment Service):** `http://localhost:8082/api/v1/swagger-ui.html`
- **Swagger UI (Auth Service):** `http://localhost:8081/api/v1/swagger-ui.html`
