# Appointy Med: especificación del docente

Transcripción del enunciado entregado por el docente. Prevalece sobre cualquier otro documento del repositorio. No incluye la lista de reglas de negocio RN-xx (el enunciado la cita pero no la entregó) ni la imagen del diagrama de schemas; sus tablas están en la sección 7.

## 1. Contexto

Sistema de gestión de agenda médica. El paciente agenda, cancela y reprograma citas; el doctor consulta su agenda y la historia clínica de sus pacientes; el administrativo gestiona catálogo y horarios. Por ahora solo backend, probado con Postman.

## 2. Stack

- Java 21, Spring Boot 3.x, Maven multi-módulo (parent POM)
- Spring Web, Spring Data JPA, Spring Security + JWT, Bean Validation
- Lombok, MapStruct (mappers), Flyway (migraciones SQL)
- PostgreSQL local (sin Docker, sin RabbitMQ, sin broker)
- Pruebas: JUnit 5, Mockito (dominio y casos de uso)
- Documentación: springdoc-openapi, API versionada en /api/v1

## 3. Arquitectura

Clean Architecture por servicio. Regla de dependencia: infrastructure → application → domain.

com.appointy.<servicio>
- domain: model, exception, port/in (casos de uso), port/out (repositorios y clientes externos). Sin Spring ni JPA.
- application: usecase (implementan port/in), config (registro de beans)
- infrastructure: entry_points (controllers REST, DTOs), driven_adapters/jpa_repository (entities, Spring Data, adapters), driven_adapters/rest_client (clientes hacia otros servicios), mapper, security

SOLID obligatorio: un caso de uso por clase, puertos pequeños, validaciones de negocio como reglas componibles, inversión de dependencias mediante puertos.

## 4. Microservicios (monorepo, un módulo por servicio)

| Servicio | Puerto | Schema | Responsabilidad |
|---|---|---|---|
| auth-service | 8081 | auth | Registro, login, JWT, roles, perfil del paciente |
| catalog-service | 8082 | catalog | Profesionales, especialidades, centros, horarios, parámetros, generación de franjas |
| appointment-service | 8083 | appointment | Franjas, citas, reservar/modificar/cancelar, historial_cita, agenda |
| clinical-history-service | 8084 | clinical | Historia clínica (solo profesional escribe, paciente lee la suya) |
| notification-service | 8085 | notification | Correos de confirmación, cambio, cancelación |

## 5. Base de datos y comunicación

Una instancia PostgreSQL, base `appointy`, un schema por servicio. Ningún servicio consulta ni tiene FK hacia el schema de otro. Migraciones con Flyway (V1__init.sql). ddl-auto: validate.

Solo REST síncrono con timeouts y manejo de fallos; endpoints `/internal/**` protegidos con API key.
- catalog → appointment: generar franjas (idempotente)
- appointment → notification: tras el commit (@TransactionalEventListener AFTER_COMMIT + @Async); si falla, la cita no se pierde
- clinical → appointment: validar relación profesional-paciente (cache Caffeine)

## 6. Roles, seguridad, concurrencia, reglas y convenciones

**Roles:** PACIENTE, PROFESIONAL, ADMINISTRATIVO. JWT emitido por auth y validado localmente por los demás. Contraseñas con BCrypt. Autorización por rol en cada endpoint. Datos inmutables del paciente: cédula, fecha de nacimiento, nombres. Editables: dirección, correo, teléfono.

**Modelo núcleo (appointment-service):**
- HorarioAtencion: profesional_id, centro_id, día_semana, hora_inicio, hora_fin, duración_franja
- Franja: profesional_id, centro_id, fecha, hora_inicio, hora_fin, estado (DISPONIBLE, OCUPADA, BLOQUEADA), @Version
- Cita: paciente_id, profesional_id, especialidad_id, centro_id, franja_id, estado (CONFIRMADA, CANCELADA, MODIFICADA, COMPLETADA), fechas de creación y modificación
- HistorialCita: cita_id, estado_anterior, estado_nuevo, usuario, fecha

**Concurrencia (crítico):**
- Reservar, modificar y cancelar en un único @Transactional
- Recargar la franja con @Lock(PESSIMISTIC_WRITE), verificar DISPONIBLE, marcar OCUPADA y crear la cita en la misma transacción
- Índice único parcial en BD: CREATE UNIQUE INDEX ON appointment.cita(franja_id) WHERE estado='CONFIRMADA'
- Conflicto de concurrencia → HTTP 409 controlado
- Modificar: validar la nueva franja, ocuparla, liberar la anterior y registrar historial, todo atómico; si falla, la cita original queda intacta
- Todo cambio de estado genera registro en HistorialCita

**Reglas de negocio clave:** solo franjas DISPONIBLES y futuras son seleccionables; anticipación mínima para modificar/cancelar (parametrizable); horizonte máximo de agendamiento; sin cruces de citas del paciente; no modificar citas COMPLETADAS o CANCELADAS; no eliminar profesionales/especialidades/centros con citas futuras (se desactivan); borrado lógico en todo.

**Convenciones:**
- Errores con estructura uniforme: código, mensaje, detalle, timestamp
- HTTP: 400 validación, 401, 403, 404, 409 conflicto
- Bean Validation en la entrada; reglas de negocio en dominio/aplicación
- No exponer datos sensibles en logs ni respuestas
- Código y paquetes en inglés o español de forma consistente (definir uno)

## 7. Requisitos funcionales

### auth-service
- RF-01 Registrar pacientes y autenticar usuarios (paciente, profesional, administrativo).
- RF-02 Identificar el rol del usuario y restringir las operaciones según ese rol.
- RF-03 Cerrar sesión y expirar los tokens vencidos.
- RF-04 El paciente consulta su perfil.
- RF-05 El paciente actualiza sus datos editables: dirección de residencia, correo y teléfono.
- RF-06 Los datos inmutables (cédula, fecha de nacimiento, nombres) no pueden modificarse por el paciente; solo se corrigen mediante un proceso administrativo.
- RF-07 El administrativo crea cuentas de profesionales y otros administrativos.

### catalog-service
- RF-08 Consultar las especialidades disponibles.
- RF-09 Ver los profesionales asociados a una especialidad.
- RF-10 Seleccionar un centro de atención cuando el profesional atienda en más de uno.
- RF-11 CRUD (con desactivación lógica) de profesionales, especialidades y centros médicos.
- RF-12 Asociar un profesional con una o más especialidades y centros.
- RF-13 Definir y modificar los horarios de atención de cada profesional y generar las franjas.
- RF-14 Configurar parámetros del servicio: duración de la franja, anticipación mínima, límite de cancelación, días máximos de agendamiento.
- RF-15 Bloquear franjas (vacaciones, ausencias) protegiendo las citas ya confirmadas.

### appointment-service
- RF-16 Consultar los horarios disponibles por profesional y fecha, mostrando solo franjas habilitadas y futuras.
- RF-17 Reservar una cita con el flujo: especialidad, profesional, centro, fecha, horario y confirmación.
- RF-18 Reverificar la disponibilidad justo antes de guardar la cita.
- RF-19 Reprogramar una cita validando primero la nueva franja; si falla, la cita original queda intacta.
- RF-20 Cancelar una cita, registrar el nuevo estado y liberar la franja.
- RF-21 El paciente consulta sus citas (próximas y pasadas).
- RF-22 El profesional consulta su agenda de citas programadas, filtrable por fecha.
- RF-23 El profesional marca una cita como COMPLETADA (o NO_ASISTIDA, opcional).
- RF-24 Consultar el historial de cambios de estado de una cita según el rol.
- RF-25 Registrar en historial_cita cada cambio de estado (quién, cuándo, de qué estado a cuál).

### clinical-history-service
- RF-26 El profesional registra una atención clínica (motivo, diagnóstico, notas, indicaciones), siempre asociada a una cita atendida.
- RF-27 El profesional consulta la historia clínica de pacientes con los que tiene o tuvo una cita.
- RF-28 El paciente consulta únicamente su propia historia clínica.
- RF-29 Las entradas no se editan ni se eliminan: se corrigen con una nueva entrada que referencia la anterior.
- RF-30 Registrar un log de acceso a cada lectura y escritura de historia clínica (quién, cuándo, qué paciente).

### notification-service
- RF-31 Enviar correo al paciente al confirmar, reprogramar o cancelar una cita.
- RF-32 Enviar recordatorio de cita (por ejemplo 24 h antes) mediante un job programado.
- RF-33 Registrar cada notificación (destino, tipo, estado, intentos) y reintentar las fallidas.
- RF-34 Informar mensajes claros cuando una operación no pueda completarse.

## 8. Requisitos no funcionales

| Código | Categoría | Requisito |
|---|---|---|
| RNF-01 | Consistencia | Reservar, reprogramar y cancelar son operaciones atómicas (un único @Transactional). Nunca dos citas confirmadas para la misma franja. |
| RNF-02 | Concurrencia | Con solicitudes simultáneas sobre la misma franja, solo una tiene éxito; las demás reciben un error controlado (409). Se usa bloqueo pesimista o @Version. |
| RNF-03 | Seguridad | JWT emitido por auth y validado localmente por cada servicio, contraseñas con BCrypt, autorización por rol en cada endpoint. HTTPS en producción. |
| RNF-04 | Seguridad interna | Los endpoints /internal/** solo aceptan llamadas con API key de servicio y no se exponen a usuarios finales. |
| RNF-05 | Privacidad | Cada rol accede solo a los datos que le corresponden. No se exponen datos sensibles ni clínicos en logs ni en respuestas de error. |
| RNF-06 | Rendimiento | La consulta de disponibilidad responde en menos de 2 s con carga normal (índices sobre profesional, fecha y estado). |
| RNF-07 | Resiliencia | La caída de un servicio no tumba al resto: timeouts y manejo de fallos en las llamadas REST. Si notification falla, la cita no se pierde. |
| RNF-08 | Mantenibilidad | Clean Architecture y SOLID: dominio independiente de frameworks, dependencias solo hacia adentro, puertos pequeños. |
| RNF-09 | Testabilidad | Pruebas unitarias del dominio y casos de uso (JUnit 5 y Mockito), más una prueba de concurrencia sobre la reserva. Cobertura mínima acordada (por ejemplo 70 %). |
| RNF-10 | Usabilidad de API | Respuestas de error con estructura uniforme (código, mensaje, detalle, timestamp) y códigos HTTP correctos (400, 401, 403, 404, 409). |
| RNF-11 | Interoperabilidad | API REST con JSON, documentada con OpenAPI/Swagger y versionada (/api/v1). Colección de Postman incluida. |
| RNF-12 | Portabilidad | Ejecución local reproducible: un PostgreSQL, un schema por servicio y migraciones con Flyway, cada servicio con mvn spring-boot:run. |
| RNF-13 | Auditoría | Toda modificación de citas y todo acceso a historia clínica quedan registrados. Borrado lógico, nunca físico. |
| RNF-14 | Integridad de datos | Restricciones en BD (únicos, FK dentro del schema, checks, índice único parcial) como segunda línea de defensa. Sin FK entre schemas. |
| RNF-15 | Independencia de datos | Cada servicio es dueño de su schema y usa su propio usuario de BD; ninguno consulta tablas de otro. |
| RNF-16 | Idempotencia | La generación de franjas y el envío de notificaciones no producen duplicados si se repiten. |
| RNF-17 | Observabilidad | Logs estructurados con un identificador de correlación que viaja entre servicios. |

## 9. Casos de uso

Actores: Paciente, Profesional (doctor), Administrativo, Sistema (jobs programados y llamadas internas /internal/**).

| Código | Caso de uso | Actor | RF |
|---|---|---|---|
| CU-01 | Registrar paciente | Paciente | RF-01 |
| CU-02 | Iniciar sesión | Todos | RF-01, 02 |
| CU-03 | Cerrar sesión | Todos | RF-03 |
| CU-04 | Consultar perfil | Paciente | RF-04 |
| CU-05 | Actualizar datos editables (dirección, correo, teléfono) | Paciente | RF-05 |
| CU-06 | Crear cuenta de profesional o administrativo | Administrativo | RF-07 |
| CU-07 | Corregir datos inmutables (cédula, nombres, fecha de nacimiento) | Administrativo | RF-06 |
| CU-08 | Listar especialidades | Paciente | RF-08 |
| CU-09 | Listar profesionales por especialidad | Paciente | RF-09 |
| CU-10 | Listar centros de un profesional | Paciente | RF-10 |
| CU-11 | Gestionar profesionales, especialidades y centros (crear, editar, desactivar) | Administrativo | RF-11 |
| CU-12 | Asociar profesional con especialidades y centros | Administrativo | RF-12 |
| CU-13 | Definir horario de atención y generar franjas | Administrativo | RF-13 |
| CU-14 | Modificar horario de atención | Administrativo | RF-13 |
| CU-15 | Configurar parámetros del servicio | Administrativo | RF-14 |
| CU-16 | Bloquear franjas (vacaciones, ausencias) | Administrativo | RF-15 |
| CU-17 | Consultar disponibilidad por profesional y fecha | Paciente | RF-16 |
| CU-18 | Reservar cita | Paciente | RF-17, 18 |
| CU-19 | Reprogramar cita | Paciente | RF-19 |
| CU-20 | Cancelar cita | Paciente (y administrativo) | RF-20 |
| CU-21 | Consultar mis citas | Paciente | RF-21 |
| CU-22 | Consultar mi agenda | Profesional | RF-22 |
| CU-23 | Marcar cita como completada o no asistida | Profesional | RF-23 |
| CU-24 | Consultar historial de estados de una cita | Según rol | RF-24, 25 |
| CU-25 | Generar franjas (idempotente) | Sistema (catalog) | RF-13 |
| CU-26 | Bloquear franjas validando citas confirmadas | Sistema (catalog) | RF-15 |
| CU-27 | Validar relación profesional-paciente | Sistema (clinical) | RF-27 |
| CU-28 | Registrar atención clínica | Profesional | RF-26 |
| CU-29 | Consultar historia clínica de un paciente | Profesional | RF-27 |
| CU-30 | Consultar mi historia clínica | Paciente | RF-28 |
| CU-31 | Corregir una entrada (crea una nueva que referencia la anterior) | Profesional | RF-29 |
| CU-32 | Registrar log de acceso (transversal a CU-28 a CU-31) | Sistema | RF-30 |
| CU-33 | Enviar notificación de cita (confirmada, reprogramada, cancelada) | Sistema (appointment) | RF-31 |
| CU-34 | Enviar recordatorios 24 h antes | Sistema (job) | RF-32 |
| CU-35 | Reintentar notificaciones fallidas | Sistema (job) | RF-33 |

Dependencias: CU-13 → CU-25; CU-16 → CU-26; CU-18/19/20 → CU-33 (tras commit); CU-28/29 → CU-27 → CU-32.

### CU-18 Reservar cita
Actor: paciente autenticado. Precondiciones: sesión activa y al menos una franja DISPONIBLE y futura. Entrada: especialidadId, profesionalId, centroId, franjaId.

Flujo principal:
1. El paciente consulta la disponibilidad (CU-17) y elige una franja.
2. Envía la solicitud de reserva.
3. El sistema valida la entrada (Bean Validation).
4. Inicia la transacción y recarga la franja con PESSIMISTIC_WRITE.
5. Verifica las reglas componibles: franja DISPONIBLE y futura, profesional asociado a la especialidad y al centro (consulta a catalog), anticipación y horizonte máximo, sin cruce de citas del paciente.
6. Marca la franja OCUPADA, crea la cita CONFIRMADA y escribe el registro en historial_cita.
7. Confirma la transacción y responde 201.
8. Después del commit, se notifica al paciente de forma asíncrona (CU-33).

Alternos: 4a franja ya ocupada: 409 con mensaje para elegir otra. 5a profesional no asociado: 422 o 400. 5b cruce de citas del paciente: 409. 5c catalog no responde (timeout): 503 controlado, sin cambios guardados. 6a el índice único rechaza el duplicado (red de seguridad): rollback y 409. 8a notification falla: se registra el error y la cita se mantiene.

Postcondición: una cita CONFIRMADA, una franja OCUPADA y un registro de historial.

### CU-19 Reprogramar cita
Precondiciones: la cita está CONFIRMADA y se cumple la anticipación mínima (RN-09).
1. El paciente elige una nueva franja.
2. El sistema valida primero la nueva franja (mismas reglas que CU-18).
3. En una transacción: ocupa la nueva, libera la anterior, actualiza la cita y registra el historial.
4. Responde 200 y notifica después del commit.

Alternos: nueva franja inválida u ocupada: 409 y la cita original queda intacta; cita COMPLETADA o CANCELADA: 409 (RN-13); falta anticipación: 422. Diseño: bloquear las dos franjas en orden fijo (por id ascendente) para evitar deadlocks entre reprogramaciones cruzadas.

### CU-20 Cancelar cita
Precondiciones: cita CONFIRMADA, solicitante dueño o administrativo, y se cumple el límite de cancelación. Flujo: en una transacción cambia el estado a CANCELADA, libera la franja y registra quién canceló y cuándo; después del commit notifica. Alternos: ya cancelada o completada: 409; fuera del plazo: 422; no es el dueño: 403.

### CU-16 Bloquear franjas
Precondición: el administrativo indica profesional, centro y rango de fechas. Flujo: catalog llama a appointment (CU-26); appointment verifica que no haya citas CONFIRMADAS en el intervalo; si no hay, marca las franjas BLOQUEADAS. Alterno: si hay citas confirmadas, rechaza con la lista de citas afectadas (RN-11); el administrativo debe reprogramarlas o cancelarlas primero.

### CU-28 Registrar atención clínica
Precondiciones: el profesional autenticado es el asignado a la cita, y la cita está CONFIRMADA o COMPLETADA (RN-17). Flujo: clinical valida con appointment (CU-27, con caché); guarda la entrada de forma inmutable; registra el log de acceso (CU-32). Alternos: el profesional no es el de la cita: 403; appointment no responde: 503 controlado.

### Mapeo a la arquitectura
Cada caso de uso es una clase en application/usecase que implementa una interfaz de domain/port/in. Por ejemplo, ReservarCitaUseCase implementa ReservarCitaPort y recibe por constructor FranjaRepositoryPort, CitaRepositoryPort, HistorialCitaRepositoryPort, CatalogClientPort y List<ReglaReserva>. Los controllers solo llaman al puerto de entrada.

## 10. Modelo de datos

IDs UUID. Borrado lógico (`activo`) en catálogo y usuarios; las demás tablas son append-only o cambian de estado.

### Schema auth
**usuario:** id UUID PK; correo VARCHAR(150) UNIQUE NOT NULL; password_hash VARCHAR(100) NOT NULL (BCrypt); rol VARCHAR(20) CHECK IN (PACIENTE, PROFESIONAL, ADMINISTRATIVO); activo BOOLEAN DEFAULT true; creado_en, actualizado_en TIMESTAMP NOT NULL; version BIGINT (@Version).

**paciente_perfil (1:1 con usuario):** usuario_id UUID PK y FK → usuario; cedula VARCHAR(20) UNIQUE inmutable; nombres, apellidos VARCHAR(100) inmutables; fecha_nacimiento DATE inmutable; telefono VARCHAR(20) editable; direccion, ciudad VARCHAR editables.

**token_revocado (cierre de sesión):** jti PK, expira_en. Un job limpia los vencidos.

### Schema catalog
| Tabla | Columnas principales | Restricciones |
|---|---|---|
| especialidad | id, nombre, descripcion, activo | UNIQUE(nombre) |
| centro | id, nombre, direccion, ciudad, telefono, activo | |
| profesional | id, usuario_id (lógica a auth), nombres, apellidos, registro_medico, activo | UNIQUE(usuario_id), UNIQUE(registro_medico) |
| profesional_especialidad | profesional_id, especialidad_id | PK compuesta, 2 FK |
| profesional_centro | profesional_id, centro_id | PK compuesta, 2 FK |
| horario_atencion | id, profesional_id, centro_id, dia_semana (1-7), hora_inicio, hora_fin, duracion_franja_min, activo | CHECK(hora_fin > hora_inicio); la duración divide el rango exacto; sin traslape por profesional (validado en dominio) |
| bloqueo | id, profesional_id, centro_id, fecha_inicio, fecha_fin, motivo, creado_por | CHECK(fecha_fin >= fecha_inicio) |
| parametro_servicio | clave, valor, descripcion | PK(clave). Ej.: anticipacion_minima_horas, limite_cancelacion_horas, horizonte_max_dias, duracion_franja_min |

horario_atencion y bloqueo tienen FK a profesional_centro (par profesional-centro), de modo que solo se define horario donde el profesional realmente atiende (RN-06).

### Schema appointment
**franja:** id UUID PK; profesional_id, centro_id UUID NOT NULL (lógicas a catalog); fecha DATE NOT NULL; hora_inicio, hora_fin TIME CHECK(hora_fin > hora_inicio); estado VARCHAR(15) CHECK IN (DISPONIBLE, OCUPADA, BLOQUEADA); version BIGINT (@Version). UNIQUE(profesional_id, centro_id, fecha, hora_inicio) hace idempotente la generación (RNF-16). Índice (profesional_id, fecha, estado) para disponibilidad (RNF-06).

**cita:** id UUID PK; paciente_id, profesional_id, especialidad_id, centro_id UUID NOT NULL (lógicas); franja_id UUID FK → franja; estado VARCHAR(15) CHECK IN (CONFIRMADA, CANCELADA, MODIFICADA, COMPLETADA, NO_ASISTIDA); creado_en, modificado_en TIMESTAMP NOT NULL; version BIGINT (@Version).

```sql
CREATE UNIQUE INDEX uq_cita_franja_confirmada
  ON appointment.cita (franja_id) WHERE estado = 'CONFIRMADA';
```
Otros índices: (paciente_id, estado) y (profesional_id, estado).

**historial_cita:** id, cita_id (FK → cita), estado_anterior (nullable en la creación), estado_nuevo, usuario_id, rol_usuario, franja_anterior_id y franja_nueva_id (reprogramaciones), detalle, fecha. Append-only.

### Schema clinical
**entrada_clinica:** id UUID PK; paciente_id, profesional_id, cita_id NOT NULL (lógicas); motivo, diagnostico, notas, indicaciones TEXT; corrige_a_id FK → entrada_clinica (nullable; la corrección referencia la anterior, RF-29); creado_en TIMESTAMP. Inmutable (sin UPDATE ni DELETE). Índices: (paciente_id, creado_en) y UNIQUE parcial opcional para evitar dos entradas "originales" de la misma cita.

**log_acceso_clinico:** id, usuario_id, rol, paciente_id, accion (LECTURA o ESCRITURA), entrada_id (nullable), fecha, resultado (PERMITIDO o DENEGADO).

### Schema notification
**notificacion:** id UUID PK; cita_id (lógica); tipo (CONFIRMACION, REPROGRAMACION, CANCELACION, RECORDATORIO); destinatario_correo (llega en la llamada REST, sin consultar a auth); asunto, cuerpo; estado (PENDIENTE, ENVIADA, FALLIDA); intentos, ultimo_error; programada_para, enviada_en, creado_en. UNIQUE(cita_id, tipo, programada_para) para la idempotencia (RNF-16).

### Cardinalidades
usuario 1:1 paciente_perfil; profesional N:M especialidad; profesional N:M centro; profesional 1:N horario_atencion; franja 1:N cita en el tiempo (solo una CONFIRMADA por franja, índice parcial); cita 1:N historial_cita; cita 1:N entrada_clinica y cita 1:N notificacion (lógicas entre schemas).

### Decisiones propuestas por el docente (a validar)
1. UUID en lugar de secuenciales.
2. Borrado lógico: `activo` en catálogo y usuarios.
3. Dato desnormalizado opcional: profesional_nombre en cita.
4. MODIFICADA: tratarla como cambio de franja y no como estado; se elimina del CHECK y queda solo en historial_cita (recomendado por el docente).
