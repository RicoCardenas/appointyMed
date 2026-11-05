#!/usr/bin/env bash
# Prueba de punta a punta: auth, catalog y appointment reales (notification simulado), contra PostgreSQL local.
# Requisitos: mvn -DskipTests package, .env con las claves de BD, puertos 8081, 8082, 8083 y 8085 libres. Borra los datos que crea.
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
CLAVE="Aa1$(openssl rand -hex 6)"
AUTH=http://localhost:8081/api/v1; CAT=http://localhost:8082/api/v1; APP=http://localhost:8083/api/v1; INT=http://localhost:8083/internal
LOGA=$S/e2e-auth.log; LOGC=$S/e2e-catalog.log; LOGP=$S/e2e-appointment.log; REG=$S/notif-registro.jsonl; : > "$REG"
SQLA() { PGPASSWORD="$AUTH_DB_PASSWORD" psql -h localhost -U auth_svc -d appointy -Atqc "$1"; }
SQLC() { PGPASSWORD="$CATALOG_DB_PASSWORD" psql -h localhost -U catalog_svc -d appointy -Atqc "$1"; }
SQLP() { PGPASSWORD="$APPOINTMENT_DB_PASSWORD" psql -h localhost -U appointment_svc -d appointy -Atqc "$1"; }
PASS=0; FAIL=0

for p in 8081 8082 8083 8085; do ss -ltn "sport = :$p" | grep -q LISTEN && { echo "ABORTO: el puerto $p esta ocupado"; exit 1; }; done

NOTIF_PID=""
arrancar_notif() { python3 -I "$DIR/fake-notification.py" "$REG" 8085 & NOTIF_PID=$!; for i in $(seq 1 20); do ss -ltn "sport = :8085" | grep -q LISTEN && break; sleep 0.2; done; }
detener_notif() { [ -n "$NOTIF_PID" ] && kill $NOTIF_PID 2>/dev/null && wait $NOTIF_PID 2>/dev/null; NOTIF_PID=""; }

java -jar auth-service/target/auth-service-0.1.0-SNAPSHOT.jar > "$LOGA" 2>&1 & AUTH_PID=$!
java -jar catalog-service/target/catalog-service-0.1.0-SNAPSHOT.jar > "$LOGC" 2>&1 & CAT_PID=$!
java -jar appointment-service/target/appointment-service-0.1.0-SNAPSHOT.jar > "$LOGP" 2>&1 & APP_PID=$!
cleanup() {
  kill $AUTH_PID $CAT_PID $APP_PID 2>/dev/null; detener_notif; wait 2>/dev/null
  SQLP "delete from historial_cita; delete from cita; delete from franja;" >/dev/null 2>&1
  SQLC "delete from bloqueo; delete from horario_atencion; delete from profesional_centro; delete from profesional_especialidad; delete from profesional; delete from centro; delete from especialidad;
        update parametro_servicio set valor='2' where clave='anticipacion_minima_horas'; update parametro_servicio set valor='24' where clave='limite_cancelacion_horas';
        update parametro_servicio set valor='60' where clave='horizonte_max_dias'; update parametro_servicio set valor='30' where clave='duracion_franja_min';" >/dev/null 2>&1
  SQLA "delete from paciente_perfil; delete from token_revocado; delete from usuario;" >/dev/null 2>&1
  echo "limpieza: appointment franjas=$(SQLP 'select count(*) from franja') citas=$(SQLP 'select count(*) from cita') historial=$(SQLP 'select count(*) from historial_cita'); catalog profesionales=$(SQLC 'select count(*) from profesional') auth usuarios=$(SQLA 'select count(*) from usuario'); parametros=$(SQLC "select string_agg(valor, ',' order by clave) from parametro_servicio")"
}
trap cleanup EXIT

esperar() { for i in $(seq 1 60); do curl -s -m 2 "localhost:$1/actuator/health" | grep -q '"UP"' && return 0; sleep 1; done; return 1; }
esperar 8081 && esperar 8082 && esperar 8083 || { echo "Un servicio no arranco"; tail -15 "$LOGP"; exit 1; }
arrancar_notif

[ "$(SQLA "select count(*) from usuario")" = "1" ] || { echo "ABORTO: auth ya tenia datos"; trap - EXIT; kill $AUTH_PID $CAT_PID $APP_PID; detener_notif; exit 1; }
for t in franja cita historial_cita; do [ "$(SQLP "select count(*) from $t")" = "0" ] || { echo "ABORTO: appointment.$t ya tenia datos"; trap - EXIT; kill $AUTH_PID $CAT_PID $APP_PID; detener_notif; exit 1; }; done
for t in especialidad centro profesional horario_atencion bloqueo; do [ "$(SQLC "select count(*) from $t")" = "0" ] || { echo "ABORTO: catalog.$t ya tenia datos"; trap - EXIT; kill $AUTH_PID $CAT_PID $APP_PID; detener_notif; exit 1; }; done

json() { python3 -I -c "import sys,json
try:
    d=json.load(sys.stdin); v=d
    for k in '$1'.split('.'):
        v=v[k] if isinstance(v,dict) else v[int(k)]
    print(v)
except Exception:
    print('')"; }
n() { python3 -I -c 'import sys,json; print(len(json.load(sys.stdin)))'; }
req() {  # req BASE METODO RUTA TOKEN CUERPO [cabecera-extra]
  local base=$1 m=$2 p=$3 t=${4:-} b=${5:-} extra=${6:-}
  local args=(-s -m 20 -o /tmp/e2e-app-body -w '%{http_code}' -X "$m" "$base$p" -H 'Content-Type: application/json')
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$b" ] && args+=(-d "$b")
  [ -n "$extra" ] && args+=(-H "$extra")
  HTTP_CODE=$(curl "${args[@]}"); BODY=$(cat /tmp/e2e-app-body)
}
chk() {  # chk NOMBRE ESPERADO [campo=valor ...]
  local ok=1 nm=$1 e=$2; shift 2
  [ "$HTTP_CODE" = "$e" ] || ok=0
  for c in "$@"; do [ "$(echo "$BODY" | json "${c%%=*}")" = "${c#*=}" ] || ok=0; done
  if [ $ok = 1 ]; then PASS=$((PASS+1)); printf 'PASS %-66s %s\n' "$nm" "$HTTP_CODE"
  else FAIL=$((FAIL+1)); printf 'FAIL %-66s esperado=%s real=%s %s\n' "$nm" "$e" "$HTTP_CODE" "$(echo "$BODY" | head -c 300)"; fi
}
igual() { if [ "$2" = "$3" ]; then PASS=$((PASS+1)); printf 'PASS %-66s %s\n' "$1" "$2"; else FAIL=$((FAIL+1)); printf 'FAIL %-66s esperado=%s real=%s\n' "$1" "$3" "$2"; fi; }
UUID() { cat /proc/sys/kernel/random/uuid; }

echo "== Cuentas reales en auth"
req $AUTH POST /auth/login "" "{\"correo\":\"$ADMIN_EMAIL\",\"clave\":\"$ADMIN_PASSWORD\"}"; ADMIN=$(echo "$BODY" | json accessToken)
for i in 1 2; do
  req $AUTH POST /auth/register "" "{\"correo\":\"e2e-pac$i@test.local\",\"clave\":\"$CLAVE\",\"cedula\":\"E2E000$i\",\"nombres\":\"Ana$i\",\"apellidos\":\"Perez\",\"fechaNacimiento\":\"1990-05-01\"}"
  req $AUTH POST /auth/login "" "{\"correo\":\"e2e-pac$i@test.local\",\"clave\":\"$CLAVE\"}"; eval "PAC$i=$(echo "$BODY" | json accessToken)"
  eval "PAC${i}_ID=\$(echo \"\$BODY\" | json usuarioId)"
done
req $AUTH GET /pacientes/me "$PAC1"; PAC1_ID=$(echo "$BODY" | json usuarioId); [ -z "$PAC1_ID" ] && PAC1_ID=$(echo "$BODY" | json id)
req $AUTH GET /pacientes/me "$PAC2"; PAC2_ID=$(echo "$BODY" | json usuarioId); [ -z "$PAC2_ID" ] && PAC2_ID=$(echo "$BODY" | json id)
for i in 1 2; do
  req $AUTH POST /admin/usuarios "$ADMIN" "{\"correo\":\"e2e-doc$i@test.local\",\"clave\":\"$CLAVE\",\"rol\":\"PROFESIONAL\"}"; eval "DOC${i}_USER=$(echo "$BODY" | json usuarioId)"
  req $AUTH POST /auth/login "" "{\"correo\":\"e2e-doc$i@test.local\",\"clave\":\"$CLAVE\"}"; eval "DOC$i=$(echo "$BODY" | json accessToken)"
done

echo "== Catalogo real: especialidades, centro, profesionales y horarios (generan franjas en appointment REAL)"
req $CAT POST /especialidades "$ADMIN" '{"nombre":"Cardiologia"}'; CARDIO=$(echo "$BODY" | json id)
req $CAT POST /especialidades "$ADMIN" '{"nombre":"Pediatria"}'; PEDI=$(echo "$BODY" | json id)
req $CAT POST /centros "$ADMIN" '{"nombre":"Sede Norte","direccion":"Calle 1","ciudad":"Cali"}'; NORTE=$(echo "$BODY" | json id)
req $CAT POST /profesionales "$ADMIN" "{\"usuarioId\":\"$DOC1_USER\",\"nombres\":\"Luis\",\"apellidos\":\"Gomez\",\"registroMedico\":\"RM-2001\"}"; PROF=$(echo "$BODY" | json id)
req $CAT POST /profesionales "$ADMIN" "{\"usuarioId\":\"$DOC2_USER\",\"nombres\":\"Eva\",\"apellidos\":\"Ruiz\",\"registroMedico\":\"RM-2002\"}"; PROF2=$(echo "$BODY" | json id)
for P in $PROF $PROF2; do
  req $CAT PUT "/profesionales/$P/especialidades" "$ADMIN" "{\"ids\":[\"$CARDIO\"]}"
  req $CAT PUT "/profesionales/$P/centros" "$ADMIN" "{\"ids\":[\"$NORTE\"]}"
done
read D3 D3W D4 D4W < <(python3 -I -c "
from datetime import datetime, timedelta; from zoneinfo import ZoneInfo
h=datetime.now(ZoneInfo('America/Bogota')).date(); a=h+timedelta(days=3); b=h+timedelta(days=4)
print(a, a.isoweekday(), b, b.isoweekday())")
req $CAT POST "/profesionales/$PROF/horarios" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":$D3W,\"horaInicio\":\"08:00\",\"horaFin\":\"12:00\"}" "X-Correlation-Id: e2e-corr-0001"
chk "horario del dia de D3 (8 franjas) con appointment real" 201 franjasGeneradas=True; H1=$(echo "$BODY" | json horario.id)
req $CAT POST "/profesionales/$PROF/horarios" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":$D4W,\"horaInicio\":\"08:00\",\"horaFin\":\"12:00\"}"; chk "horario del dia de D4" 201 franjasGeneradas=True
req $CAT POST "/profesionales/$PROF2/horarios" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":$D3W,\"horaInicio\":\"08:00\",\"horaFin\":\"10:00\"}"; chk "horario del segundo profesional el mismo dia (para el cruce)" 201 franjasGeneradas=True
ESPERADAS=$(python3 -I -c "
from datetime import datetime, timedelta; from zoneinfo import ZoneInfo
h=datetime.now(ZoneInfo('America/Bogota')).date(); d=[(h+timedelta(days=i)).isoweekday() for i in range(61)]
print(d.count($D3W)*8 + d.count($D4W)*8 + d.count($D3W)*4)")
igual "franjas guardadas por appointment = las que calculo catalog" "$(SQLP "select count(*) from franja")" "$ESPERADAS"
igual "08:00 se guarda como 08:00:00 (JVM en UTC)" "$(SQLP "select min(hora_inicio)::text from franja")" "08:00:00"
req $CAT POST "/horarios/$H1/franjas" "$ADMIN"; chk "reintento de generacion" 200 franjasGeneradas=True
igual "idempotente: el reintento no duplica franjas (RNF-16)" "$(SQLP "select count(*) from franja")" "$ESPERADAS"

echo "== CU-17 disponibilidad"
DISP="/disponibilidad?profesionalId=$PROF&centroId=$NORTE&fecha=$D3"
req $APP GET "$DISP"; chk "sin token" 401 codigo=NO_AUTENTICADO
req $APP GET "$DISP" "$PAC1"; chk "paciente consulta D3" 200; igual "ocho franjas DISPONIBLES" "$(echo "$BODY" | n)" 8
F0=$(echo "$BODY" | json 0.id); F1=$(echo "$BODY" | json 1.id); F2=$(echo "$BODY" | json 2.id); F3=$(echo "$BODY" | json 3.id); F4=$(echo "$BODY" | json 4.id); F5=$(echo "$BODY" | json 5.id)
igual "la primera es 08:00-08:30" "$(echo "$BODY" | json 0.horaInicio)-$(echo "$BODY" | json 0.horaFin)" "08:00-08:30"
req $APP GET "/disponibilidad?profesionalId=$PROF&centroId=$NORTE&fecha=2020-01-01" "$PAC1"; igual "una fecha pasada no tiene franjas" "$(echo "$BODY" | n)" 0
req $APP GET "/disponibilidad?profesionalId=$PROF&centroId=$NORTE&fecha=manana" "$PAC1"; chk "fecha mal formada" 400
req $APP GET "/disponibilidad?profesionalId=$PROF&centroId=$NORTE" "$PAC1"; chk "falta la fecha" 400
req $APP GET "/disponibilidad?profesionalId=$PROF&centroId=$NORTE&fecha=$D4" "$DOC1"; igual "el profesional tambien puede consultar (8 franjas en D4)" "$(echo "$BODY" | n)" 8
G0=$(echo "$BODY" | json 0.id); G1=$(echo "$BODY" | json 1.id)
req $APP GET "/disponibilidad?profesionalId=$PROF2&centroId=$NORTE&fecha=$D3" "$PAC1"; X0=$(echo "$BODY" | json 0.id)

echo "== CU-18 reservar"
RES() { echo "{\"especialidadId\":\"$CARDIO\",\"profesionalId\":\"$1\",\"centroId\":\"$NORTE\",\"franjaId\":\"$2\"}"; }
req $APP POST /citas "" "$(RES $PROF $F0)"; chk "sin token" 401
req $APP POST /citas "$DOC1" "$(RES $PROF $F0)"; chk "un profesional no reserva" 403 codigo=ACCESO_DENEGADO
req $APP POST /citas "$PAC1" '{}'; chk "faltan campos" 400 codigo=VALIDACION_ENTRADA
req $APP POST /citas "$PAC1" "$(RES $PROF $(UUID))"; chk "franja inexistente" 404 codigo=FRANJA_NO_ENCONTRADA
req $APP POST /citas "$PAC1" "{\"especialidadId\":\"$PEDI\",\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"franjaId\":\"$F0\"}"; chk "profesional no atiende esa especialidad" 422 codigo=PROFESIONAL_NO_ASOCIADO
req $APP POST /citas "$PAC1" "$(RES $PROF2 $F0)"; chk "franja de otro profesional que el indicado" 422 codigo=FRANJA_NO_COINCIDE
igual "ninguna de las pruebas anteriores dejo cita" "$(SQLP "select count(*) from cita")" 0
req $APP POST /citas "$PAC1" "$(RES $PROF $F0)" "X-Correlation-Id: e2e-corr-0002"
chk "reserva 08:00 de D3" 201 estado=CONFIRMADA fecha=$D3 horaInicio=08:00 pacienteId=$PAC1_ID; CITA1=$(echo "$BODY" | json id)
igual "la franja quedo OCUPADA" "$(SQLP "select estado from franja where id='$F0'")" OCUPADA
igual "un registro de historial (sin estado anterior)" "$(SQLP "select count(*)||':'||coalesce(max(estado_anterior),'null') from historial_cita where cita_id='$CITA1'")" "1:null"
req $APP POST /citas "$PAC2" "$(RES $PROF $F0)"; chk "otro paciente, misma franja: conflicto" 409 codigo=FRANJA_NO_DISPONIBLE
req $APP POST /citas "$PAC1" "$(RES $PROF2 $X0)"; chk "mismo paciente, otro profesional a la misma hora: cruce" 409 codigo=CRUCE_DE_CITAS
sleep 1
igual "notification recibio CONFIRMADA con la llave y el correlation id" "$(python3 -I -c "
import json; l=[json.loads(x) for x in open('$REG')]; print([(e['cuerpo']['tipo'], e['llave_ok'], e['correlacion']) for e in l if e['cuerpo']['citaId']=='$CITA1'])")" "[('CONFIRMADA', True, 'e2e-corr-0002')]"
igual "el aviso lleva ids y hora, sin nombres ni correos" "$(python3 -I -c "
import json; e=json.loads(open('$REG').readlines()[0])['cuerpo']; print(sorted(e.keys()))")" "['actorId', 'centroId', 'citaId', 'fecha', 'horaInicio', 'pacienteId', 'profesionalId', 'tipo']"

echo "== RNF-07 notification caido no revierte la cita"
detener_notif
req $APP POST /citas "$PAC2" "$(RES $PROF $F2)"; chk "reserva con notification caido" 201 estado=CONFIRMADA; CITA2=$(echo "$BODY" | json id)
sleep 1
igual "la cita quedo guardada" "$(SQLP "select estado from cita where id='$CITA2'")" CONFIRMADA
igual "el fallo se registro en el log de appointment" "$(grep -c "No se pudo notificar el evento CONFIRMADA de la cita $CITA2" "$LOGP")" 1
arrancar_notif

echo "== CU-21 mis citas y CU-22 agenda"
req $APP GET /citas/mias "$PAC1"; chk "paciente ve sus citas" 200; igual "una cita" "$(echo "$BODY" | n)" 1
req $APP GET "/citas/mias?periodo=PROXIMAS" "$PAC1"; igual "proximas: 1" "$(echo "$BODY" | n)" 1
req $APP GET "/citas/mias?periodo=PASADAS" "$PAC1"; igual "pasadas: 0" "$(echo "$BODY" | n)" 0
req $APP GET "/citas/mias?periodo=otra" "$PAC1"; chk "periodo invalido" 400 codigo=PERIODO_INVALIDO
req $APP GET /citas/mias "$DOC1"; chk "un profesional no usa mis citas" 403
req $APP GET /citas/agenda "$DOC1"; chk "agenda del profesional" 200; igual "tiene 2 citas (PAC1 y PAC2)" "$(echo "$BODY" | n)" 2
igual "ordenada por hora: 08:00 y luego 09:00" "$(echo "$BODY" | json 0.horaInicio)/$(echo "$BODY" | json 1.horaInicio)" "08:00/09:00"
req $APP GET "/citas/agenda?fecha=$D3" "$DOC1"; igual "agenda de D3" "$(echo "$BODY" | n)" 2
req $APP GET "/citas/agenda?fecha=$D4" "$DOC1"; igual "agenda de D4" "$(echo "$BODY" | n)" 0
req $APP GET /citas/agenda "$DOC2"; igual "la agenda de otro profesional no mezcla citas" "$(echo "$BODY" | n)" 0
req $APP GET /citas/agenda "$PAC1"; chk "paciente no ve agenda" 403
req $APP GET "/citas/$CITA1" "$PAC1"; chk "paciente ve su cita" 200 estado=CONFIRMADA
req $APP GET "/citas/$CITA1" "$PAC2"; chk "otro paciente NO ve la cita" 403 codigo=NO_ES_TU_CITA
req $APP GET "/citas/$CITA1" "$DOC2"; chk "otro profesional NO ve la cita" 403
req $APP GET "/citas/$(UUID)" "$ADMIN"; chk "cita inexistente" 404 codigo=CITA_NO_ENCONTRADA

echo "== CU-19 reprogramar"
req $APP POST "/citas/$CITA1/reprogramar" "$PAC2" "{\"franjaId\":\"$G0\"}"; chk "otro paciente no reprograma" 403 codigo=NO_ES_TU_CITA
req $APP POST "/citas/$CITA1/reprogramar" "$DOC1" "{\"franjaId\":\"$G0\"}"; chk "un profesional no reprograma" 403
req $APP POST "/citas/$CITA1/reprogramar" "$PAC1" "{\"franjaId\":\"$F2\"}"; chk "a una franja ocupada: conflicto" 409 codigo=FRANJA_NO_DISPONIBLE
igual "la cita original quedo intacta" "$(SQLP "select franja_id::text||':'||estado from cita where id='$CITA1'")" "$F0:CONFIRMADA"
igual "y su franja sigue ocupada" "$(SQLP "select estado from franja where id='$F0'")" OCUPADA
req $APP POST "/citas/$CITA1/reprogramar" "$PAC1" "{\"franjaId\":\"$F0\"}"; chk "a la misma franja" 422 codigo=MISMA_FRANJA
req $APP POST "/citas/$CITA1/reprogramar" "$PAC1" "{\"franjaId\":\"$G0\"}"; chk "a D4 08:00" 200 estado=CONFIRMADA fecha=$D4 horaInicio=08:00 franjaId=$G0
igual "franja vieja DISPONIBLE y nueva OCUPADA" "$(SQLP "select (select estado from franja where id='$F0')||'/'||(select estado from franja where id='$G0')")" "DISPONIBLE/OCUPADA"
igual "historial: creacion y reprogramacion" "$(SQLP "select count(*) from historial_cita where cita_id='$CITA1'")" 2
igual "la reprogramacion anota franja anterior y nueva" "$(SQLP "select franja_anterior_id::text||'>'||franja_nueva_id::text from historial_cita where cita_id='$CITA1' and franja_anterior_id is not null")" "$F0>$G0"
sleep 1
igual "notification recibio REPROGRAMADA" "$(grep -c '"tipo": "REPROGRAMADA"' "$REG")" 1
req $APP POST /citas "$PAC1" "$(RES $PROF $F0)"; chk "la franja liberada vuelve a poder reservarse" 201; CITA1B=$(echo "$BODY" | json id)
req $APP POST "/citas/$CITA1B/cancelar" "$PAC1"; chk "(se cancela para dejar el escenario limpio)" 200

echo "== CU-24 historial"
req $APP GET "/citas/$CITA1/historial" "$PAC1"; chk "el paciente ve su historial" 200; igual "2 filas, de la mas antigua a la mas reciente" "$(echo "$BODY" | json 0.estadoAnterior)|$(echo "$BODY" | json 0.estadoNuevo)|$(echo "$BODY" | json 1.estadoNuevo)" "None|CONFIRMADA|CONFIRMADA"
req $APP GET "/citas/$CITA1/historial" "$DOC1"; chk "el profesional de la cita lo ve" 200
req $APP GET "/citas/$CITA1/historial" "$ADMIN"; chk "el administrativo lo ve" 200
req $APP GET "/citas/$CITA1/historial" "$PAC2"; chk "otro paciente no" 403
req $APP GET "/citas/$CITA1/historial" "$DOC2"; chk "otro profesional no" 403

echo "== CU-23 completada o no asistida"
req $APP PATCH "/citas/$CITA1/estado" "$DOC2" '{"estado":"COMPLETADA"}'; chk "otro profesional no la marca" 403
req $APP PATCH "/citas/$CITA1/estado" "$PAC1" '{"estado":"COMPLETADA"}'; chk "el paciente no la marca" 403
req $APP PATCH "/citas/$CITA1/estado" "$DOC1" '{"estado":"CANCELADA"}'; chk "estado no permitido" 400 codigo=ESTADO_NO_PERMITIDO
req $APP PATCH "/citas/$CITA1/estado" "$DOC1" '{"estado":"INVENTADO"}'; chk "estado desconocido" 400
req $APP PATCH "/citas/$CITA1/estado" "$DOC1" '{"estado":"COMPLETADA"}'; chk "el profesional la marca COMPLETADA" 200 estado=COMPLETADA
igual "la franja sigue ocupada" "$(SQLP "select estado from franja where id='$G0'")" OCUPADA
req $APP PATCH "/citas/$CITA1/estado" "$DOC1" '{"estado":"NO_ASISTIDA"}'; chk "ya no se puede marcar otra vez" 409 codigo=CITA_NO_MODIFICABLE
req $APP POST "/citas/$CITA1/reprogramar" "$PAC1" "{\"franjaId\":\"$G1\"}"; chk "RN-13: una COMPLETADA no se reprograma" 409 codigo=CITA_NO_MODIFICABLE
req $APP POST "/citas/$CITA1/cancelar" "$PAC1"; chk "RN-13: una COMPLETADA no se cancela" 409 codigo=CITA_NO_MODIFICABLE

echo "== CU-20 cancelar y parametros de catalog"
req $APP POST "/citas/$CITA2/cancelar" "$(echo $PAC1)"; chk "otro paciente no cancela" 403 codigo=NO_ES_TU_CITA
req $APP POST "/citas/$CITA2/cancelar" "$DOC1"; chk "un profesional no cancela" 403
req $CAT PUT /parametros "$ADMIN" '{"parametros":{"limite_cancelacion_horas":"100"}}'; chk "limite de cancelacion a 100 h" 200
req $APP POST "/citas/$CITA2/cancelar" "$PAC2"; chk "fuera del plazo (la cita es en menos de 100 h)" 422 codigo=FUERA_DEL_PLAZO_DE_CANCELACION
igual "la cita sigue CONFIRMADA" "$(SQLP "select estado from cita where id='$CITA2'")" CONFIRMADA
req $CAT PUT /parametros "$ADMIN" '{"parametros":{"limite_cancelacion_horas":"24"}}'
req $CAT PUT /parametros "$ADMIN" '{"parametros":{"anticipacion_minima_horas":"100"}}'; chk "anticipacion minima a 100 h" 200
req $APP POST /citas "$PAC1" "$(RES $PROF $F3)"; chk "reservar con menos anticipacion que la minima" 422 codigo=ANTICIPACION_INSUFICIENTE
req $CAT PUT /parametros "$ADMIN" '{"parametros":{"anticipacion_minima_horas":"2"}}'
req $CAT PUT /parametros "$ADMIN" '{"parametros":{"horizonte_max_dias":"1"}}'; chk "horizonte a 1 dia" 200
req $APP POST /citas "$PAC1" "$(RES $PROF $F3)"; chk "reservar mas alla del horizonte" 422 codigo=FUERA_DEL_HORIZONTE
req $CAT PUT /parametros "$ADMIN" '{"parametros":{"horizonte_max_dias":"60"}}'
req $APP POST "/citas/$CITA2/cancelar" "$PAC2" '{"motivo":"viaje"}'; chk "el dueno cancela" 200 estado=CANCELADA
igual "la franja se libero" "$(SQLP "select estado from franja where id='$F2'")" DISPONIBLE
igual "historial: quien cancelo y por que" "$(SQLP "select usuario_id::text||'|'||rol_usuario||'|'||detalle from historial_cita where cita_id='$CITA2' and estado_nuevo='CANCELADA'")" "$PAC2_ID|PACIENTE|Cita cancelada: viaje"
req $APP POST "/citas/$CITA2/cancelar" "$PAC2"; chk "ya cancelada: conflicto" 409 codigo=CITA_NO_MODIFICABLE
req $APP POST /citas "$PAC1" "$(RES $PROF $F3)"; CITA3=$(echo "$BODY" | json id)
req $APP POST "/citas/$CITA3/cancelar" "$ADMIN"; chk "el administrativo cancela una cita ajena" 200 estado=CANCELADA
igual "historial: quien cancelo (administrativo)" "$(SQLP "select rol_usuario from historial_cita where cita_id='$CITA3' and estado_nuevo='CANCELADA'")" ADMINISTRATIVO
sleep 1
igual "notification recibio CANCELADA x3" "$(grep -c '"tipo": "CANCELADA"' "$REG")" 3

echo "== Concurrencia por HTTP (RNF-02): 8 solicitudes a la vez sobre la misma franja"
rm -f $S/conc.txt; CURLS=""
for i in 1 2 3 4 5 6 7 8; do
  ( curl -s -m 30 -o /dev/null -w '%{http_code}\n' -X POST $APP/citas -H 'Content-Type: application/json' -H "Authorization: Bearer $([ $((i%2)) = 0 ] && echo $PAC1 || echo $PAC2)" -d "$(RES $PROF $F4)" >> $S/conc.txt ) & CURLS="$CURLS $!"
done; wait $CURLS
igual "exactamente una reserva con exito (201)" "$(grep -c '^201$' $S/conc.txt)" 1
igual "las otras 7 reciben 409 controlado" "$(grep -c '^409$' $S/conc.txt)" 7
igual "ningun 5xx" "$(grep -c '^5' $S/conc.txt)" 0
igual "una sola cita CONFIRMADA en esa franja" "$(SQLP "select count(*) from cita where franja_id='$F4' and estado='CONFIRMADA'")" 1
rm -f $S/conc.txt

echo "== CU-26 y CU-16 bloquear franjas (via catalog -> appointment real)"
req $CAT POST /bloqueos "$ADMIN" "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"$D3\",\"fechaFin\":\"$D3\",\"motivo\":\"Congreso\"}"
chk "hay una cita CONFIRMADA ese dia: rechazo con la lista" 409 codigo=CITAS_CONFIRMADAS_EN_INTERVALO
igual "catalog no guardo el bloqueo (rollback)" "$(SQLC "select count(*) from bloqueo")" 0
igual "ninguna franja cambio a BLOQUEADA" "$(SQLP "select count(*) from franja where estado='BLOQUEADA'")" 0
igual "la lista nombra la cita afectada" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(len(json.load(sys.stdin)["detalle"]))')" 1
CITA4=$(SQLP "select id from cita where franja_id='$F4' and estado='CONFIRMADA'")
req $APP POST "/citas/$CITA4/cancelar" "$ADMIN"; chk "se cancela la cita que estorbaba" 200
req $CAT POST /bloqueos "$ADMIN" "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"$D3\",\"fechaFin\":\"$D3\",\"motivo\":\"Congreso\"}"; chk "ahora el bloqueo se acepta" 201
igual "las 8 franjas de D3 del profesional quedaron BLOQUEADAS" "$(SQLP "select count(*) from franja where profesional_id='$PROF' and fecha='$D3' and estado='BLOQUEADA'")" 8
igual "no se tocaron las del segundo profesional" "$(SQLP "select count(*) from franja where profesional_id='$PROF2' and fecha='$D3' and estado='BLOQUEADA'")" 0
req $APP GET "$DISP" "$PAC1"; igual "ya no se ofrece disponibilidad ese dia" "$(echo "$BODY" | n)" 0
req $APP POST /citas "$PAC1" "$(RES $PROF $F5)"; chk "reservar una franja bloqueada" 409 codigo=FRANJA_NO_DISPONIBLE

echo "== API interna (RNF-04): CU-25, CU-26, CU-27"
GEN="{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"franjas\":[{\"fecha\":\"2031-02-03\",\"horaInicio\":\"08:00\",\"horaFin\":\"08:30\"},{\"fecha\":\"2031-02-03\",\"horaInicio\":\"08:30\",\"horaFin\":\"09:00\"}]}"
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -X POST $INT/franjas/generar -H 'Content-Type: application/json' -d "$GEN"); igual "generar sin llave" "$CODE" 401
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -X POST $INT/franjas/generar -H 'Content-Type: application/json' -H "Authorization: Bearer $ADMIN" -d "$GEN"); igual "un JWT de administrador NO abre /internal" "$CODE" 401
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -X POST $INT/franjas/generar -H 'Content-Type: application/json' -H "X-Api-Key: llave-incorrecta" -d "$GEN"); igual "llave incorrecta" "$CODE" 401
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -H "X-Api-Key: $INTERNAL_API_KEY" "$APP/citas/mias"); igual "la llave NO abre la API de usuarios" "$CODE" 401
curl -s -m 5 -X POST $INT/franjas/generar -H 'Content-Type: application/json' -H "X-Api-Key: $INTERNAL_API_KEY" -d "$GEN" > /tmp/e2e-app-body; igual "generar: 2 creadas" "$(json creadas < /tmp/e2e-app-body)/$(json existentes < /tmp/e2e-app-body)" "2/0"
curl -s -m 5 -X POST $INT/franjas/generar -H 'Content-Type: application/json' -H "X-Api-Key: $INTERNAL_API_KEY" -d "$GEN" > /tmp/e2e-app-body; igual "repetir: 0 creadas, 2 existentes" "$(json creadas < /tmp/e2e-app-body)/$(json existentes < /tmp/e2e-app-body)" "0/2"
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -X POST $INT/franjas/generar -H 'Content-Type: application/json' -H "X-Api-Key: $INTERNAL_API_KEY" -d "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"franjas\":[{\"fecha\":\"2031-02-04\",\"horaInicio\":\"9:00\",\"horaFin\":\"09:30\"}]}"); igual "hora mal formada" "$CODE" 400
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -X POST $INT/franjas/generar -H 'Content-Type: application/json' -H "X-Api-Key: $INTERNAL_API_KEY" -d "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"franjas\":[{\"fecha\":\"2031-02-04\",\"horaInicio\":\"10:00\",\"horaFin\":\"09:30\"}]}"); igual "fin anterior al inicio" "$CODE" 400
curl -s -m 5 -X POST $INT/franjas/bloquear -H 'Content-Type: application/json' -H "X-Api-Key: $INTERNAL_API_KEY" -d "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"2031-02-03\",\"fechaFin\":\"2031-02-03\"}" > /tmp/e2e-app-body; igual "bloquear: 2 franjas" "$(json bloqueadas < /tmp/e2e-app-body)" 2
curl -s -m 5 -X POST $INT/franjas/bloquear -H 'Content-Type: application/json' -H "X-Api-Key: $INTERNAL_API_KEY" -d "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"2031-02-03\",\"fechaFin\":\"2031-02-03\"}" > /tmp/e2e-app-body; igual "bloquear es idempotente: 0" "$(json bloqueadas < /tmp/e2e-app-body)" 0
CODE=$(curl -s -m 5 -o /tmp/e2e-app-body -w '%{http_code}' -X POST $INT/franjas/bloquear -H 'Content-Type: application/json' -H "X-Api-Key: $INTERNAL_API_KEY" -d "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"$D4\",\"fechaFin\":\"$D4\"}"); igual "bloquear con citas (COMPLETADA no cuenta): 200" "$CODE" 200
CODE=$(curl -s -m 5 -o /tmp/e2e-app-body -w '%{http_code}' -X POST $INT/franjas/bloquear -H 'Content-Type: application/json' -H "X-Api-Key: $INTERNAL_API_KEY" -d "{\"profesionalId\":\"$PROF\",\"centroId\":\"$NORTE\",\"fechaInicio\":\"2031-02-05\",\"fechaFin\":\"2031-02-01\"}"); igual "rango invertido" "$CODE" 400
RELI="$INT/relaciones?profesionalId=$PROF&pacienteId"
curl -s -m 5 -H "X-Api-Key: $INTERNAL_API_KEY" "$RELI=$PAC1_ID" > /tmp/e2e-app-body; igual "CU-27: PAC1 tuvo una cita COMPLETADA con el profesional" "$(json existe < /tmp/e2e-app-body)" True
curl -s -m 5 -H "X-Api-Key: $INTERNAL_API_KEY" "$RELI=$(UUID)" > /tmp/e2e-app-body; igual "CU-27: un paciente sin citas no tiene relacion" "$(json existe < /tmp/e2e-app-body)" False
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' "$RELI=$PAC1_ID"); igual "CU-27 sin llave" "$CODE" 401

echo "== Catalog caido (503 controlado, sin cambios) y recuperacion"
kill $CAT_PID; wait $CAT_PID 2>/dev/null
req $APP POST /citas "$PAC2" "$(RES $PROF2 $X0)"; chk "reservar con catalog caido" 503 codigo=CATALOG_NO_DISPONIBLE
igual "no se guardo nada" "$(SQLP "select count(*) from cita where franja_id='$X0'")" 0
req $APP GET "/citas/agenda" "$DOC1"; chk "la agenda tampoco puede resolver al profesional" 503
req $APP GET "/citas/mias" "$PAC2"; chk "lo que no depende de catalog sigue funcionando" 200
req $APP GET "$DISP" "$PAC1"; chk "la disponibilidad sigue funcionando" 200

echo "== Errores uniformes, privacidad y logs"
req $APP GET /ruta-que-no-existe "$ADMIN"; chk "ruta inexistente" 404
req $APP POST /citas "$PAC1" '{"x":'; chk "JSON ilegible" 400 codigo=SOLICITUD_ILEGIBLE
req $APP GET "/citas/no-es-uuid" "$PAC1"; chk "id con formato invalido" 400
echo "   cuerpo de error: $(echo "$BODY" | python3 -I -c 'import sys,json; print(sorted(json.load(sys.stdin).keys()))')"
echo "   secretos en el log de appointment: llave=$(grep -c "$INTERNAL_API_KEY" "$LOGP") jwt_secret=$(grep -c "$JWT_SECRET" "$LOGP") token=$(grep -c "$PAC1" "$LOGP") clave=$(grep -c "$CLAVE" "$LOGP")"
echo "   correos/nombres en el log de appointment: $(grep -c -E 'e2e-pac|e2e-doc|Perez|RM-200' "$LOGP")"
echo "   ERROR en log de appointment: $(grep -c ' ERROR ' "$LOGP") (esperados: notification caido y catalog caido); NO esperados: $(grep ' ERROR ' "$LOGP" | grep -vc -E 'No se pudo notificar|catalog no respondio')"
echo "   el id de correlacion e2e-corr-0002 aparece en el log: $(grep -c 'e2e-corr-0002' "$LOGP")"
igual "creado_en se guarda en UTC" "$(SQLP "select bool_and(creado_en > (now() at time zone 'utc') - interval '30 minutes' and creado_en <= (now() at time zone 'utc') + interval '1 minute') from cita")" t

echo; echo "RESULTADO: $PASS correctas, $FAIL fallidas"
