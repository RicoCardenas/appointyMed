#!/usr/bin/env bash
# Prueba de punta a punta: auth real + catalog real + appointment simulado, contra PostgreSQL local.
# Requisitos: mvn -DskipTests package, .env con las claves de BD, puertos 8081 a 8083 libres. Borra los datos que crea.
# Secretos y claves de prueba se generan aqui, no se imprimen y no se guardan.
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR/../.."
S=/tmp/appointy-e2e; mkdir -p $S
set -a; source .env; set +a
export JWT_SECRET="$(openssl rand -hex 32)"
export INTERNAL_API_KEY="$(openssl rand -hex 16)"
export ADMIN_EMAIL="e2e-admin@test.local"
export ADMIN_PASSWORD="Aa1$(openssl rand -hex 8)"
export APPOINTMENT_READ_TIMEOUT_MS=1500
CLAVE="Aa1$(openssl rand -hex 6)"
AUTH=http://localhost:8081/api/v1; CAT=http://localhost:8082/api/v1; INT=http://localhost:8082/internal
LOGA=$S/e2e-auth.log; LOGC=$S/e2e-catalog.log; REG=$S/fake-registro.jsonl; : > "$REG"
SQLA() { PGPASSWORD="$AUTH_DB_PASSWORD" psql -h localhost -U auth_svc -d appointy -Atqc "$1"; }
SQLC() { PGPASSWORD="$CATALOG_DB_PASSWORD" psql -h localhost -U catalog_svc -d appointy -Atqc "$1"; }
PASS=0; FAIL=0

for p in 8081 8082 8083; do ss -ltn "sport = :$p" | grep -q LISTEN && { echo "ABORTO: el puerto $p esta ocupado"; exit 1; }; done

FAKE_PID=""
arrancar_fake() { python3 -I "$DIR/fake-appointment.py" "$REG" 8083 & FAKE_PID=$!; for i in $(seq 1 20); do ss -ltn "sport = :8083" | grep -q LISTEN && break; sleep 0.2; done; }
detener_fake() { [ -n "$FAKE_PID" ] && kill $FAKE_PID 2>/dev/null && wait $FAKE_PID 2>/dev/null; FAKE_PID=""; for i in $(seq 1 20); do ss -ltn "sport = :8083" | grep -q LISTEN || break; sleep 0.2; done; }

java -jar auth-service/target/auth-service-0.1.0-SNAPSHOT.jar > "$LOGA" 2>&1 & AUTH_PID=$!
java -jar catalog-service/target/catalog-service-0.1.0-SNAPSHOT.jar > "$LOGC" 2>&1 & CAT_PID=$!
cleanup() {
  kill $AUTH_PID $CAT_PID 2>/dev/null; detener_fake; wait 2>/dev/null
  SQLC "delete from bloqueo; delete from horario_atencion; delete from profesional_centro; delete from profesional_especialidad; delete from profesional; delete from centro; delete from especialidad;
        update parametro_servicio set valor='2' where clave='anticipacion_minima_horas'; update parametro_servicio set valor='24' where clave='limite_cancelacion_horas';
        update parametro_servicio set valor='60' where clave='horizonte_max_dias'; update parametro_servicio set valor='30' where clave='duracion_franja_min';" >/dev/null 2>&1
  SQLA "delete from paciente_perfil; delete from token_revocado; delete from usuario;" >/dev/null 2>&1
  echo "limpieza: catalog especialidades=$(SQLC 'select count(*) from especialidad') profesionales=$(SQLC 'select count(*) from profesional') auth usuarios=$(SQLA 'select count(*) from usuario'); parametros=$(SQLC "select string_agg(valor, ',' order by clave) from parametro_servicio")"
}
trap cleanup EXIT

esperar() { for i in $(seq 1 50); do curl -s -m 2 "localhost:$1/actuator/health" | grep -q '"UP"' && return 0; sleep 1; done; return 1; }
esperar 8081 && esperar 8082 || { echo "Un servicio no arranco"; tail -15 "$LOGC"; exit 1; }
arrancar_fake

# Seguridad de la prueba: no se toca nada si ya habia datos
[ "$(SQLA "select count(*) from usuario")" = "1" ] && [ "$(SQLA "select count(*) from usuario where correo='$ADMIN_EMAIL'")" = "1" ] || { echo "ABORTO: auth ya tenia datos"; trap - EXIT; kill $AUTH_PID $CAT_PID; detener_fake; exit 1; }
for t in especialidad centro profesional horario_atencion bloqueo; do [ "$(SQLC "select count(*) from $t")" = "0" ] || { echo "ABORTO: catalog.$t ya tenia datos"; trap - EXIT; kill $AUTH_PID $CAT_PID; detener_fake; exit 1; }; done

json() { python3 -I -c "import sys,json
try:
    d=json.load(sys.stdin); v=d
    for k in '$1'.split('.'):
        v=v[k] if isinstance(v,dict) else v[int(k)]
    print(v)
except Exception:
    print('')"; }
req() {  # req BASE METODO RUTA TOKEN CUERPO [cabecera-extra]
  local base=$1 m=$2 p=$3 t=${4:-} b=${5:-} extra=${6:-}
  local args=(-s -m 20 -o /tmp/e2e-cat-body -w '%{http_code}' -X "$m" "$base$p" -H 'Content-Type: application/json')
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$b" ] && args+=(-d "$b")
  [ -n "$extra" ] && args+=(-H "$extra")
  HTTP_CODE=$(curl "${args[@]}"); BODY=$(cat /tmp/e2e-cat-body)
}
chk() {  # chk NOMBRE ESPERADO [campo=valor ...]
  local ok=1 n=$1 e=$2; shift 2
  [ "$HTTP_CODE" = "$e" ] || ok=0
  for c in "$@"; do [ "$(echo "$BODY" | json "${c%%=*}")" = "${c#*=}" ] || ok=0; done
  if [ $ok = 1 ]; then PASS=$((PASS+1)); printf 'PASS %-62s %s\n' "$n" "$HTTP_CODE"
  else FAIL=$((FAIL+1)); printf 'FAIL %-62s esperado=%s real=%s %s\n' "$n" "$e" "$HTTP_CODE" "$(echo "$BODY" | head -c 260)"; fi
}
igual() { if [ "$2" = "$3" ]; then PASS=$((PASS+1)); printf 'PASS %-62s %s\n' "$1" "$2"; else FAIL=$((FAIL+1)); printf 'FAIL %-62s esperado=%s real=%s\n' "$1" "$3" "$2"; fi; }

echo "== Cuentas reales en auth (el JWT se valida localmente en catalog)"
req $AUTH POST /auth/login "" "{\"correo\":\"$ADMIN_EMAIL\",\"clave\":\"$ADMIN_PASSWORD\"}"; chk "login del administrador sembrado" 200 rol=ADMINISTRATIVO
ADMIN=$(echo "$BODY" | json accessToken)
req $AUTH POST /auth/register "" "{\"correo\":\"e2e-pac@test.local\",\"clave\":\"$CLAVE\",\"cedula\":\"E2E0001\",\"nombres\":\"Ana\",\"apellidos\":\"Perez\",\"fechaNacimiento\":\"1990-05-01\"}"; chk "registro de un paciente" 201
req $AUTH POST /auth/login "" "{\"correo\":\"e2e-pac@test.local\",\"clave\":\"$CLAVE\"}"; PAC=$(echo "$BODY" | json accessToken)
req $AUTH POST /admin/usuarios "$ADMIN" "{\"correo\":\"e2e-doc@test.local\",\"clave\":\"$CLAVE\",\"rol\":\"PROFESIONAL\"}"; chk "cuenta de profesional en auth" 201
DOC_USER=$(echo "$BODY" | json usuarioId)
req $AUTH POST /auth/login "" "{\"correo\":\"e2e-doc@test.local\",\"clave\":\"$CLAVE\"}"; DOC=$(echo "$BODY" | json accessToken)
req $AUTH POST /admin/usuarios "$ADMIN" "{\"correo\":\"e2e-doc2@test.local\",\"clave\":\"$CLAVE\",\"rol\":\"PROFESIONAL\"}"; DOC2_USER=$(echo "$BODY" | json usuarioId)

echo "== Acceso y roles (RNF-03)"
req $CAT GET /especialidades; chk "sin token" 401 codigo=NO_AUTENTICADO
req $CAT GET /especialidades "token.basura.invalido"; chk "token invalido" 401 codigo=NO_AUTENTICADO
req $CAT GET /especialidades "$PAC"; chk "token de auth aceptado (lista vacia)" 200
req $CAT POST /especialidades "$PAC" '{"nombre":"Hack"}'; chk "paciente no crea especialidades" 403 codigo=ACCESO_DENEGADO
req $CAT POST /especialidades "$DOC" '{"nombre":"Hack"}'; chk "profesional no crea especialidades" 403
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' localhost:8082/actuator/health); igual "health publico" "$CODE" 200

echo "== CU-11 especialidades y CU-08 listar"
req $CAT POST /especialidades "$ADMIN" '{"nombre":"  Cardiologia ","descripcion":"Corazon"}'; chk "crea especialidad (nombre recortado)" 201 nombre=Cardiologia activo=True
CARDIO=$(echo "$BODY" | json id)
req $CAT POST /especialidades "$ADMIN" '{"nombre":"Cardiologia"}'; chk "nombre duplicado" 409 codigo=ESPECIALIDAD_YA_EXISTE
req $CAT POST /especialidades "$ADMIN" '{"nombre":"   "}'; chk "nombre vacio" 400 codigo=VALIDACION_ENTRADA
req $CAT POST /especialidades "$ADMIN" '{"nombre":"X","id":"1"}'; chk "campo no permitido" 400 codigo=CAMPO_NO_PERMITIDO
req $CAT POST /especialidades "$ADMIN" '{"nombre":"Pediatria"}'; PEDI=$(echo "$BODY" | json id)
req $CAT PUT "/especialidades/$PEDI" "$ADMIN" '{"nombre":"Pediatria general","descripcion":"Ninos"}'; chk "actualiza especialidad" 200 nombre="Pediatria general"
req $CAT PUT "/especialidades/$PEDI" "$ADMIN" '{"nombre":"Cardiologia"}'; chk "renombrar a uno existente" 409
req $CAT PUT "/especialidades/$(cat /proc/sys/kernel/random/uuid)" "$ADMIN" '{"nombre":"Z"}'; chk "especialidad inexistente" 404 codigo=ESPECIALIDAD_NO_ENCONTRADA
req $CAT PUT "/especialidades/no-es-uuid" "$ADMIN" '{"nombre":"Z"}'; chk "id con formato invalido" 400
req $CAT GET /especialidades "$PAC"; igual "paciente ve 2 especialidades" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(len(json.load(sys.stdin)))')" 2
req $CAT DELETE "/especialidades/$PEDI" "$ADMIN"; chk "desactivar (borrado logico)" 204
req $CAT GET /especialidades "$PAC"; igual "el paciente ya solo ve 1" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(len(json.load(sys.stdin)))')" 1
req $CAT GET "/especialidades?incluirInactivas=true" "$PAC"; chk "paciente no ve inactivas" 403
req $CAT GET "/especialidades?incluirInactivas=true" "$ADMIN"; igual "el administrativo ve las 2" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(len(json.load(sys.stdin)))')" 2
igual "la fila sigue en BD (no hay borrado fisico)" "$(SQLC "select count(*) from especialidad where id='$PEDI' and activo=false")" 1

echo "== CU-11 centros"
req $CAT POST /centros "$ADMIN" '{"nombre":"Sede Norte","direccion":"Calle 1 # 2-3","ciudad":"Cali","telefono":"3001234567"}'; chk "crea centro" 201 nombre="Sede Norte"
NORTE=$(echo "$BODY" | json id)
req $CAT POST /centros "$ADMIN" '{"nombre":"Sede Sur","direccion":"Carrera 5","ciudad":"Cali"}'; SUR=$(echo "$BODY" | json id)
req $CAT POST /centros "$ADMIN" '{"nombre":"Sede Vieja","direccion":"Av 3","ciudad":"Cali"}'; VIEJA=$(echo "$BODY" | json id)
req $CAT POST /centros "$ADMIN" '{"nombre":"Sede Libre","direccion":"Av 9","ciudad":"Cali"}'; LIBRE=$(echo "$BODY" | json id)
req $CAT POST /centros "$ADMIN" '{"nombre":"Sin direccion","ciudad":"Cali"}'; chk "centro sin direccion" 400
req $CAT POST /centros "$ADMIN" '{"nombre":"Tel malo","direccion":"x","ciudad":"y","telefono":"abc"}'; chk "telefono invalido" 400
req $CAT PUT "/centros/$NORTE" "$ADMIN" '{"nombre":"Sede Norte","direccion":"Calle 10 # 2-3","ciudad":"Cali","telefono":"3001234567"}'; chk "actualiza centro" 200 direccion="Calle 10 # 2-3"
req $CAT DELETE "/centros/$VIEJA" "$ADMIN"; chk "desactiva centro" 204
req $CAT POST /centros "$PAC" '{"nombre":"X","direccion":"x","ciudad":"y"}'; chk "paciente no crea centros" 403

echo "== CU-11 profesionales"
BODYP="{\"usuarioId\":\"$DOC_USER\",\"nombres\":\"Luis\",\"apellidos\":\"Gomez\",\"registroMedico\":\"RM-1001\"}"
req $CAT POST /profesionales "$ADMIN" "$BODYP"; chk "crea profesional" 201 registroMedico=RM-1001 activo=True
PROF=$(echo "$BODY" | json id)
req $CAT POST /profesionales "$ADMIN" "$BODYP"; chk "la cuenta ya tiene perfil" 409 codigo=PROFESIONAL_YA_EXISTE
req $CAT POST /profesionales "$ADMIN" "{\"usuarioId\":\"$DOC2_USER\",\"nombres\":\"Eva\",\"apellidos\":\"Ruiz\",\"registroMedico\":\"RM-1001\"}"; chk "registro medico duplicado" 409 codigo=REGISTRO_MEDICO_YA_REGISTRADO
req $CAT POST /profesionales "$ADMIN" '{"usuarioId":"no-uuid","nombres":"E","apellidos":"R","registroMedico":"RM-2"}'; chk "usuarioId invalido" 400
req $CAT POST /profesionales "$ADMIN" "{\"usuarioId\":\"$DOC2_USER\",\"nombres\":\"Eva\",\"apellidos\":\"Ruiz\",\"registroMedico\":\"RM-1002\"}"; chk "segundo profesional" 201
PROF2=$(echo "$BODY" | json id)
req $CAT PUT "/profesionales/$PROF" "$ADMIN" '{"nombres":"Luis Alberto","apellidos":"Gomez","registroMedico":"RM-1001"}'; chk "actualiza profesional (usuarioId no cambia)" 200 nombres="Luis Alberto" usuarioId=$DOC_USER
req $CAT PUT "/profesionales/$PROF" "$ADMIN" '{"nombres":"Luis","apellidos":"Gomez","registroMedico":"RM-1002"}'; chk "registro de otro profesional" 409
req $CAT GET "/profesionales/$PROF" "$PAC"; chk "el paciente consulta un profesional" 200 profesional.registroMedico=RM-1001

echo "== CU-12 asociar especialidades y centros"
req $CAT PUT "/profesionales/$PROF/especialidades" "$ADMIN" "{\"ids\":[\"$CARDIO\"]}"; chk "asocia especialidad" 200 especialidadIds.0=$CARDIO
req $CAT PUT "/profesionales/$PROF/especialidades" "$ADMIN" "{\"ids\":[\"$PEDI\"]}"; chk "especialidad inactiva" 400 codigo=ESPECIALIDAD_INACTIVA
req $CAT PUT "/profesionales/$PROF/especialidades" "$ADMIN" "{\"ids\":[\"$(cat /proc/sys/kernel/random/uuid)\"]}"; chk "especialidad inexistente" 404
req $CAT PUT "/profesionales/$PROF/centros" "$ADMIN" "{\"ids\":[\"$NORTE\",\"$SUR\"]}"; chk "asocia dos centros" 200
req $CAT PUT "/profesionales/$PROF/centros" "$ADMIN" "{\"ids\":[\"$VIEJA\"]}"; chk "centro inactivo" 400 codigo=CENTRO_INACTIVO
req $CAT PUT "/profesionales/$PROF/centros" "$PAC" "{\"ids\":[]}"; chk "paciente no asocia" 403
req $CAT PUT "/profesionales/$PROF/centros" "$ADMIN" '{"ids":null}'; chk "ids nulo" 400
req $CAT GET "/profesionales/$PROF" "$PAC"; igual "las asociaciones sobreviven a los intentos fallidos" "$(echo "$BODY" | json especialidadIds.0)|$(echo "$BODY" | python3 -I -c 'import sys,json; print(len(json.load(sys.stdin)["centroIds"]))')" "$CARDIO|2"

echo "== CU-09 profesionales por especialidad y CU-10 centros del profesional"
req $CAT GET "/especialidades/$CARDIO/profesionales" "$PAC"; igual "especialidad con 1 profesional" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(len(json.load(sys.stdin)))')" 1
req $CAT GET "/especialidades/$PEDI/profesionales" "$PAC"; chk "especialidad inactiva no se ofrece" 404
req $CAT GET "/profesionales/$PROF/centros" "$PAC"; igual "CU-10: dos centros para elegir" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(sorted(c["nombre"] for c in json.load(sys.stdin)))')" "['Sede Norte', 'Sede Sur']"
req $CAT GET "/profesionales/$(cat /proc/sys/kernel/random/uuid)/centros" "$PAC"; chk "profesional inexistente" 404

echo "== CU-13 definir horario y generar franjas (appointment simulado)"
: > "$REG"
req $CAT POST "/profesionales/$PROF/horarios" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":1,\"horaInicio\":\"08:00\",\"horaFin\":\"10:00\"}" "X-Correlation-Id: e2e-corr-0001"
chk "lunes 08:00-10:00, duracion por defecto" 201 franjasGeneradas=True horario.duracionFranjaMin=30 horario.horaInicio=08:00
H1=$(echo "$BODY" | json horario.id)
ESPERADAS=$(python3 -I -c "
from datetime import date, timedelta; from zoneinfo import ZoneInfo; from datetime import datetime
hoy=datetime.now(ZoneInfo('America/Bogota')).date()
print(sum(1 for i in range(61) if (hoy+timedelta(days=i)).isoweekday()==1)*4)")
igual "franjas enviadas = lunes de los proximos 60 dias x 4" "$(python3 -I -c "import json; print(json.loads(open('$REG').readlines()[0])['n_franjas'])")" "$ESPERADAS"
HOY_BOG=$(python3 -I -c "from datetime import datetime; from zoneinfo import ZoneInfo; print(datetime.now(ZoneInfo('America/Bogota')).date())")
igual "la primera franja no es anterior al hoy de la clinica" "$(python3 -I -c "import json; print(json.loads(open('$REG').readlines()[0])['primera'] >= '$HOY_BOG')")" True
igual "appointment recibio la llave de servicio" "$(python3 -I -c "import json; print(json.loads(open('$REG').readlines()[0])['llave_ok'])")" True
igual "el correlation id viaja hasta appointment" "$(python3 -I -c "import json; print(json.loads(open('$REG').readlines()[0])['correlacion'])")" e2e-corr-0001
req $CAT POST "/profesionales/$PROF/horarios" "$ADMIN" "{\"centroId\":\"$SUR\",\"diaSemana\":1,\"horaInicio\":\"09:00\",\"horaFin\":\"11:00\"}"; chk "traslape el mismo dia aunque sea otro centro" 409 codigo=HORARIO_TRASLAPADO
req $CAT POST "/profesionales/$PROF/horarios" "$ADMIN" "{\"centroId\":\"$SUR\",\"diaSemana\":1,\"horaInicio\":\"10:00\",\"horaFin\":\"12:00\"}"; chk "tocarse en el borde no es traslape" 201
req $CAT POST "/profesionales/$PROF/horarios" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":2,\"horaInicio\":\"08:00\",\"horaFin\":\"10:00\",\"duracionFranjaMin\":45}"; chk "la duracion no divide el rango" 400 codigo=DURACION_NO_DIVIDE_RANGO
req $CAT POST "/profesionales/$PROF/horarios" "$ADMIN" "{\"centroId\":\"$LIBRE\",\"diaSemana\":2,\"horaInicio\":\"08:00\",\"horaFin\":\"10:00\"}"; chk "centro donde no atiende" 400 codigo=PROFESIONAL_NO_ATIENDE_EN_CENTRO
req $CAT POST "/profesionales/$PROF/horarios" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":2,\"horaInicio\":\"8:00\",\"horaFin\":\"10:00\"}"; chk "hora con formato incorrecto" 400 codigo=VALIDACION_ENTRADA
req $CAT POST "/profesionales/$PROF/horarios" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":9,\"horaInicio\":\"08:00\",\"horaFin\":\"10:00\"}"; chk "dia 9" 400
req $CAT POST "/profesionales/$PROF/horarios" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":2,\"horaInicio\":\"10:00\",\"horaFin\":\"08:00\"}"; chk "fin anterior al inicio" 400 codigo=HORAS_INVALIDAS
req $CAT POST "/profesionales/$PROF/horarios" "$PAC" "{\"centroId\":\"$NORTE\",\"diaSemana\":2,\"horaInicio\":\"08:00\",\"horaFin\":\"10:00\"}"; chk "paciente no define horarios" 403
req $CAT GET "/profesionales/$PROF/horarios" "$DOC"; igual "el profesional ve sus horarios" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(len(json.load(sys.stdin)))')" 2
req $CAT GET "/profesionales/$PROF/horarios" "$PAC"; chk "el paciente no ve horarios internos" 403

echo "== Resiliencia (RNF-07, RNF-16): appointment caido, reintento idempotente"
detener_fake
req $CAT POST "/profesionales/$PROF/horarios" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":3,\"horaInicio\":\"08:00\",\"horaFin\":\"09:00\"}"
chk "con appointment caido el horario se guarda igual" 201 franjasGeneradas=False
H3=$(echo "$BODY" | json horario.id)
igual "el horario quedo en BD" "$(SQLC "select count(*) from horario_atencion where id='$H3'")" 1
req $CAT POST "/horarios/$H3/franjas" "$ADMIN"; chk "reintento con appointment aun caido" 503 codigo=APPOINTMENT_NO_DISPONIBLE
arrancar_fake
req $CAT POST "/horarios/$H3/franjas" "$ADMIN"; chk "reintento con appointment de vuelta" 200 franjasGeneradas=True
CREADAS1=$(echo "$BODY" | json franjasCreadas)
req $CAT POST "/horarios/$H3/franjas" "$ADMIN"; chk "repetirlo no duplica nada" 200 franjasCreadas=0
igual "la segunda vez todo ya existia" "$(echo "$BODY" | json franjasExistentes)" "$CREADAS1"
req $CAT POST "/horarios/$(cat /proc/sys/kernel/random/uuid)/franjas" "$ADMIN"; chk "horario inexistente" 404

echo "== CU-14 modificar horario"
req $CAT PUT "/horarios/$H1" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":1,\"horaInicio\":\"08:00\",\"horaFin\":\"10:30\",\"duracionFranjaMin\":30,\"activo\":true}"; chk "extiende el horario (no se traslapa consigo mismo)" 409
req $CAT PUT "/horarios/$H1" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":1,\"horaInicio\":\"07:00\",\"horaFin\":\"10:00\",\"duracionFranjaMin\":30,\"activo\":true}"; chk "adelanta el inicio" 200 horario.horaInicio=07:00 franjasGeneradas=True
req $CAT PUT "/horarios/$H1" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":1,\"horaInicio\":\"07:00\",\"horaFin\":\"10:00\",\"duracionFranjaMin\":30,\"activo\":false}"; chk "desactiva el horario: no genera franjas" 200 franjasGeneradas=False horario.activo=False
req $CAT PUT "/horarios/$H1" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":1,\"horaInicio\":\"07:00\",\"horaFin\":\"10:00\",\"duracionFranjaMin\":30}"; chk "falta el campo activo" 400
req $CAT POST "/horarios/$H1/franjas" "$ADMIN"; chk "no se generan franjas de un horario inactivo" 409 codigo=HORARIO_INACTIVO
req $CAT PUT "/horarios/$H1" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":1,\"horaInicio\":\"08:00\",\"horaFin\":\"10:00\",\"duracionFranjaMin\":30,\"activo\":true}"; chk "lo reactiva" 200 franjasGeneradas=True
igual "en BD: hora_inicio es TIME y la duracion 30" "$(SQLC "select hora_inicio::text||'/'||duracion_franja_min from horario_atencion where id='$H1'")" "08:00:00/30"

echo "== CU-15 parametros"
req $CAT GET /parametros "$ADMIN"; igual "cuatro parametros de serie" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(len(json.load(sys.stdin)))')" 4
req $CAT PUT /parametros "$ADMIN" '{"parametros":{"horizonte_max_dias":45,"limite_cancelacion_horas":"12"}}'; chk "actualiza dos parametros (numero sin comillas incluido)" 200
igual "valores guardados" "$(SQLC "select string_agg(clave||'='||valor, ',' order by clave) from parametro_servicio where clave in ('horizonte_max_dias','limite_cancelacion_horas')")" "horizonte_max_dias=45,limite_cancelacion_horas=12"
req $CAT PUT /parametros "$ADMIN" '{"parametros":{"horizonte_max_dias":"50","duracion_franja_min":"1"}}'; chk "uno invalido: todo o nada" 400 codigo=PARAMETRO_FUERA_DE_RANGO
igual "no cambio ninguno" "$(SQLC "select valor from parametro_servicio where clave='horizonte_max_dias'")" 45
req $CAT PUT /parametros "$ADMIN" '{"parametros":{"inventado":"1"}}'; chk "parametro desconocido" 400 codigo=PARAMETRO_DESCONOCIDO
req $CAT PUT /parametros "$ADMIN" '{"parametros":{}}'; chk "sin parametros" 400
req $CAT GET /parametros "$PAC"; chk "paciente no ve parametros" 403
: > "$REG"; req $CAT POST "/horarios/$H1/franjas" "$ADMIN"
ESP45=$(python3 -I -c "
from datetime import timedelta, datetime; from zoneinfo import ZoneInfo
hoy=datetime.now(ZoneInfo('America/Bogota')).date()
print(sum(1 for i in range(46) if (hoy+timedelta(days=i)).isoweekday()==1)*4)")
igual "el nuevo horizonte (45 dias) se usa al generar" "$(python3 -I -c "import json; print(json.loads(open('$REG').readlines()[0])['n_franjas'])")" "$ESP45"

echo "== CU-16 bloquear franjas (consistencia entre servicios)"
B0=$(SQLC "select count(*) from bloqueo")
req $CAT POST /bloqueos "$ADMIN" "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"2030-03-01\",\"fechaFin\":\"2030-03-15\",\"motivo\":\" Vacaciones \"}"; chk "bloqueo sin citas afectadas" 201 motivo=Vacaciones
igual "se guardo 1 bloqueo" "$(SQLC "select count(*) from bloqueo")" $((B0+1))
igual "fechas del bloqueo guardadas sin desplazamiento" "$(SQLC "select fecha_inicio::text||'/'||fecha_fin::text from bloqueo where motivo='Vacaciones'")" "2030-03-01/2030-03-15"
igual "creado_en se guarda en UTC (no en la zona de la maquina)" "$(SQLC "select creado_en > (now() at time zone 'utc') - interval '5 minutes' and creado_en <= (now() at time zone 'utc') + interval '1 minute' from bloqueo where motivo='Vacaciones'")" t
req $CAT POST /bloqueos "$ADMIN" "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"2030-01-01\",\"fechaFin\":\"2030-01-10\",\"motivo\":\"Congreso\"}"
chk "hay citas confirmadas: se rechaza con la lista" 409 codigo=CITAS_CONFIRMADAS_EN_INTERVALO detalle.0=cita-1 detalle.1=cita-2
igual "y NO queda bloqueo en catalog (rollback)" "$(SQLC "select count(*) from bloqueo")" $((B0+1))
T0=$(date +%s)
req $CAT POST /bloqueos "$ADMIN" "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"2031-01-01\",\"fechaFin\":\"2031-01-10\",\"motivo\":\"Lento\"}"
chk "appointment tarda mas que el timeout: 503" 503 codigo=APPOINTMENT_NO_DISPONIBLE
T1=$(date +%s); [ $((T1-T0)) -lt 6 ] && { PASS=$((PASS+1)); echo "PASS la respuesta no espero los 8 s de appointment (tardo $((T1-T0)) s)"; } || { FAIL=$((FAIL+1)); echo "FAIL tardo $((T1-T0)) s"; }
igual "sin bloqueo guardado tras el timeout" "$(SQLC "select count(*) from bloqueo")" $((B0+1))
detener_fake
req $CAT POST /bloqueos "$ADMIN" "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"2030-05-01\",\"fechaFin\":\"2030-05-02\",\"motivo\":\"Caido\"}"; chk "appointment caido: 503" 503
igual "sin bloqueo guardado con appointment caido" "$(SQLC "select count(*) from bloqueo")" $((B0+1))
arrancar_fake
req $CAT POST /bloqueos "$ADMIN" "{\"profesionalId\":\"$PROF\",\"centroId\":\"$LIBRE\",\"fechaInicio\":\"2030-05-01\",\"fechaFin\":\"2030-05-02\",\"motivo\":\"x\"}"; chk "centro donde no atiende" 400
req $CAT POST /bloqueos "$ADMIN" "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"2030-05-10\",\"fechaFin\":\"2030-05-01\",\"motivo\":\"x\"}"; chk "fin anterior al inicio" 400 codigo=RANGO_INVALIDO
req $CAT POST /bloqueos "$ADMIN" "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"2030-05-01\",\"fechaFin\":\"2030-05-02\",\"motivo\":\"\"}"; chk "motivo vacio" 400
req $CAT POST /bloqueos "$ADMIN" "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"mañana\",\"fechaFin\":\"2030-05-02\",\"motivo\":\"x\"}"; chk "fecha mal formada" 400
req $CAT POST /bloqueos "$PAC" "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"2030-05-01\",\"fechaFin\":\"2030-05-02\",\"motivo\":\"x\"}"; chk "paciente no bloquea" 403
igual "el bloqueo guarda quien lo creo" "$(SQLC "select count(*) from bloqueo where creado_por is not null")" $((B0+1))

echo "== Integridad: no se puede quitar un centro con horarios o bloqueos"
req $CAT PUT "/profesionales/$PROF/centros" "$ADMIN" "{\"ids\":[\"$SUR\"]}"; chk "quitar el centro norte (tiene horarios)" 409 codigo=CENTRO_CON_HORARIOS
req $CAT PUT "/profesionales/$PROF2/centros" "$ADMIN" "{\"ids\":[\"$NORTE\"]}"; chk "otro profesional si puede asociarse" 200
req $CAT PUT "/profesionales/$PROF2/centros" "$ADMIN" "{\"ids\":[]}"; chk "y quitarse (sin horarios)" 200
req $CAT DELETE "/profesionales/$PROF2" "$ADMIN"; chk "desactiva un profesional" 204
req $CAT GET "/especialidades/$CARDIO/profesionales" "$PAC"; igual "los inactivos no se ofrecen" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(len(json.load(sys.stdin)))')" 1

echo "== API interna (RNF-04): solo con llave de servicio"
IA="$INT/profesionales/$PROF/asociacion?especialidadId=$CARDIO&centroId=$NORTE"
CODE=$(curl -s -m 5 -o /tmp/e2e-cat-body -w '%{http_code}' "$IA"); igual "sin llave" "$CODE" 401
CODE=$(curl -s -m 5 -o /tmp/e2e-cat-body -w '%{http_code}' -H "Authorization: Bearer $ADMIN" "$IA"); igual "un JWT de administrador NO abre /internal" "$CODE" 401
CODE=$(curl -s -m 5 -o /tmp/e2e-cat-body -w '%{http_code}' -H "X-Api-Key: llave-incorrecta" "$IA"); igual "llave incorrecta" "$CODE" 401
CODE=$(curl -s -m 5 -o /tmp/e2e-cat-body -w '%{http_code}' -H "X-Api-Key: $INTERNAL_API_KEY" "$IA"); igual "llave correcta" "$CODE" 200
igual "profesional activo y asociado" "$(python3 -I -c "import json; d=json.load(open('/tmp/e2e-cat-body')); print(d['profesionalActivo'], d['asociado'])")" "True True"
curl -s -m 5 -H "X-Api-Key: $INTERNAL_API_KEY" "$INT/profesionales/$PROF/asociacion?especialidadId=$CARDIO&centroId=$LIBRE" > /tmp/e2e-cat-body; igual "centro donde no atiende: no asociado" "$(python3 -I -c "import json; print(json.load(open('/tmp/e2e-cat-body'))['asociado'])")" False
curl -s -m 5 -H "X-Api-Key: $INTERNAL_API_KEY" "$INT/profesionales/$PROF2/asociacion?especialidadId=$CARDIO&centroId=$NORTE" > /tmp/e2e-cat-body; igual "profesional inactivo" "$(python3 -I -c "import json; d=json.load(open('/tmp/e2e-cat-body')); print(d['profesionalActivo'], d['asociado'])")" "False False"
curl -s -m 5 -H "X-Api-Key: $INTERNAL_API_KEY" "$INT/parametros" > /tmp/e2e-cat-body; igual "parametros para appointment" "$(python3 -I -c "import json; print(json.load(open('/tmp/e2e-cat-body'))['horizonte_max_dias'])")" 45
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -H "X-Api-Key: $INTERNAL_API_KEY" "$CAT/especialidades"); igual "la llave de servicio NO abre la API de usuarios" "$CODE" 401
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' "localhost:8082/internal/parametros"); igual "ruta interna sin credenciales" "$CODE" 401

echo "== Errores uniformes, privacidad y logs"
req $CAT GET /ruta-que-no-existe "$ADMIN"; chk "ruta inexistente" 404
req $CAT DELETE /especialidades "$ADMIN"; chk "metodo no permitido" 405
echo "   cuerpo de error: $(echo "$BODY" | python3 -I -c 'import sys,json; print(sorted(json.load(sys.stdin).keys()))')"
echo "   secretos en el log de catalog: llave=$(grep -c "$INTERNAL_API_KEY" "$LOGC") jwt_secret=$(grep -c "$JWT_SECRET" "$LOGC") token_admin=$(grep -c "$ADMIN" "$LOGC") clave=$(grep -c "$CLAVE" "$LOGC")"
echo "   correo/registro en el log de catalog: $(grep -c -E 'e2e-doc|RM-1001' "$LOGC")"
echo "   ERROR en log de catalog (esperados: caidas/timeouts de appointment que provoca la prueba): $(grep -c ' ERROR ' "$LOGC"); de ellos NO esperados: $(grep ' ERROR ' "$LOGC" | grep -vc 'appointment')"
echo "   el id de correlacion aparece en el log: $(grep -c 'e2e-corr-0001' "$LOGC")"

echo; echo "RESULTADO: $PASS correctas, $FAIL fallidas"
