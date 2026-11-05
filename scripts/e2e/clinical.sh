#!/usr/bin/env bash
# Prueba de punta a punta: auth, catalog, appointment y clinical-history reales (notification simulado), contra PostgreSQL local.
# Requisitos: mvn -DskipTests package, .env con las claves de BD, puertos 8081 a 8085 libres. Borra los datos que crea.
# Secretos y claves de prueba se generan aqui, no se imprimen y no se guardan. Los textos clinicos de prueba llevan una marca
# aleatoria para comprobar que no aparecen en logs ni en errores.
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR/../.."
S=/tmp/appointy-e2e; mkdir -p $S
set -a; source .env; set +a
export JWT_SECRET="$(openssl rand -hex 32)"
export INTERNAL_API_KEY="$(openssl rand -hex 16)"
export RELACIONES_TTL_SEGUNDOS=300   # la prueba de la cache no debe depender de la rapidez de la maquina
export ADMIN_EMAIL="e2e-admin@test.local"
export ADMIN_PASSWORD="Aa1$(openssl rand -hex 8)"
CLAVE="Aa1$(openssl rand -hex 6)"
MARCA="MARCA$(openssl rand -hex 6)"
AUTH=http://localhost:8081/api/v1; CAT=http://localhost:8082/api/v1; APP=http://localhost:8083/api/v1; CLI=http://localhost:8084/api/v1
LOGA=$S/e2e-auth.log; LOGC=$S/e2e-catalog.log; LOGP=$S/e2e-appointment.log; LOGK=$S/e2e-clinical.log; REG=$S/notif-registro.jsonl; : > "$REG"
SQLA() { PGPASSWORD="$AUTH_DB_PASSWORD" psql -h localhost -U auth_svc -d appointy -Atqc "$1"; }
SQLC() { PGPASSWORD="$CATALOG_DB_PASSWORD" psql -h localhost -U catalog_svc -d appointy -Atqc "$1"; }
SQLP() { PGPASSWORD="$APPOINTMENT_DB_PASSWORD" psql -h localhost -U appointment_svc -d appointy -Atqc "$1"; }
SQLK() { PGPASSWORD="$CLINICAL_DB_PASSWORD" psql -h localhost -U clinical_svc -d appointy -Atqc "$1"; }
PASS=0; FAIL=0

for p in 8081 8082 8083 8084 8085; do ss -ltn "sport = :$p" | grep -q LISTEN && { echo "ABORTO: el puerto $p esta ocupado"; exit 1; }; done

NOTIF_PID=""
arrancar_notif() { python3 -I "$DIR/fake-notification.py" "$REG" 8085 & NOTIF_PID=$!; for i in $(seq 1 20); do ss -ltn "sport = :8085" | grep -q LISTEN && break; sleep 0.2; done; }
detener_notif() { [ -n "$NOTIF_PID" ] && kill $NOTIF_PID 2>/dev/null && wait $NOTIF_PID 2>/dev/null; NOTIF_PID=""; }

java -jar auth-service/target/auth-service-0.1.0-SNAPSHOT.jar > "$LOGA" 2>&1 & AUTH_PID=$!
java -jar catalog-service/target/catalog-service-0.1.0-SNAPSHOT.jar > "$LOGC" 2>&1 & CAT_PID=$!
java -jar appointment-service/target/appointment-service-0.1.0-SNAPSHOT.jar > "$LOGP" 2>&1 & APP_PID=$!
java -jar clinical-history-service/target/clinical-history-service-0.1.0-SNAPSHOT.jar > "$LOGK" 2>&1 & CLI_PID=$!
cleanup() {
  kill $AUTH_PID $CAT_PID $APP_PID $CLI_PID 2>/dev/null; detener_notif; wait $AUTH_PID $CAT_PID $APP_PID $CLI_PID 2>/dev/null
  # Las tablas clinicas son de solo insercion (trigger): para limpiar los datos de prueba el propietario desactiva el trigger un momento
  SQLK "alter table entrada_clinica disable trigger tr_entrada_inmutable; alter table log_acceso_clinico disable trigger tr_log_inmutable;
        delete from entrada_clinica; delete from log_acceso_clinico;
        alter table entrada_clinica enable trigger tr_entrada_inmutable; alter table log_acceso_clinico enable trigger tr_log_inmutable;" >/dev/null 2>&1
  SQLP "delete from historial_cita; delete from cita; delete from franja;" >/dev/null 2>&1
  SQLC "delete from bloqueo; delete from horario_atencion; delete from profesional_centro; delete from profesional_especialidad; delete from profesional; delete from centro; delete from especialidad;
        update parametro_servicio set valor='2' where clave='anticipacion_minima_horas'; update parametro_servicio set valor='24' where clave='limite_cancelacion_horas';
        update parametro_servicio set valor='60' where clave='horizonte_max_dias'; update parametro_servicio set valor='30' where clave='duracion_franja_min';" >/dev/null 2>&1
  SQLA "delete from paciente_perfil; delete from token_revocado; delete from usuario;" >/dev/null 2>&1
  echo "limpieza: clinical entradas=$(SQLK 'select count(*) from entrada_clinica') logs=$(SQLK 'select count(*) from log_acceso_clinico') triggers=$(SQLK "select count(*) from pg_trigger where tgname in ('tr_entrada_inmutable','tr_log_inmutable') and tgenabled='O'"); appointment citas=$(SQLP 'select count(*) from cita') franjas=$(SQLP 'select count(*) from franja'); catalog profesionales=$(SQLC 'select count(*) from profesional') auth usuarios=$(SQLA 'select count(*) from usuario')"
}
trap cleanup EXIT

esperar() { for i in $(seq 1 60); do curl -s -m 2 "localhost:$1/actuator/health" | grep -q '"UP"' && return 0; sleep 1; done; return 1; }
esperar 8081 && esperar 8082 && esperar 8083 && esperar 8084 || { echo "Un servicio no arranco"; tail -15 "$LOGK"; exit 1; }
arrancar_notif

abortar() { echo "ABORTO: $1 ya tenia datos"; trap - EXIT; kill $AUTH_PID $CAT_PID $APP_PID $CLI_PID; detener_notif; exit 1; }
[ "$(SQLA "select count(*) from usuario")" = "1" ] || abortar auth
for t in franja cita historial_cita; do [ "$(SQLP "select count(*) from $t")" = "0" ] || abortar appointment.$t; done
for t in especialidad centro profesional horario_atencion bloqueo; do [ "$(SQLC "select count(*) from $t")" = "0" ] || abortar catalog.$t; done
for t in entrada_clinica log_acceso_clinico; do [ "$(SQLK "select count(*) from $t")" = "0" ] || abortar clinical.$t; done

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
  local args=(-s -m 20 -o /tmp/e2e-cli-body -w '%{http_code}' -X "$m" "$base$p" -H 'Content-Type: application/json')
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$b" ] && args+=(-d "$b")
  [ -n "$extra" ] && args+=(-H "$extra")
  HTTP_CODE=$(curl "${args[@]}"); BODY=$(cat /tmp/e2e-cli-body)
}
chk() {  # chk NOMBRE ESPERADO [campo=valor ...]
  local ok=1 nm=$1 e=$2; shift 2
  [ "$HTTP_CODE" = "$e" ] || ok=0
  for c in "$@"; do [ "$(echo "$BODY" | json "${c%%=*}")" = "${c#*=}" ] || ok=0; done
  if [ $ok = 1 ]; then PASS=$((PASS+1)); printf 'PASS %-70s %s\n' "$nm" "$HTTP_CODE"
  else FAIL=$((FAIL+1)); printf 'FAIL %-70s esperado=%s real=%s %s\n' "$nm" "$e" "$HTTP_CODE" "$(echo "$BODY" | head -c 300)"; fi
}
igual() { if [ "$2" = "$3" ]; then PASS=$((PASS+1)); printf 'PASS %-70s %s\n' "$1" "$2"; else FAIL=$((FAIL+1)); printf 'FAIL %-70s esperado=%s real=%s\n' "$1" "$3" "$2"; fi; }
UUID() { cat /proc/sys/kernel/random/uuid; }
ATENCION() { echo "{\"citaId\":\"$1\",\"motivo\":\"Dolor $MARCA\",\"diagnostico\":\"Migrana $MARCA\",\"notas\":\"Nota $MARCA\",\"indicaciones\":\"Reposo $MARCA\"}"; }
CORRECCION() { echo "{\"motivo\":\"Dolor corregido $MARCA\",\"diagnostico\":\"Cefalea tensional $MARCA\",\"notas\":\"Nota 2 $MARCA\"}"; }
PACS() { python3 -I -c 'import sys,json; print(",".join(sorted({e["pacienteId"] for e in json.load(sys.stdin)})))'; }

echo "== Cuentas reales en auth"
req $AUTH POST /auth/login "" "{\"correo\":\"$ADMIN_EMAIL\",\"clave\":\"$ADMIN_PASSWORD\"}"; ADMIN=$(echo "$BODY" | json accessToken)
for i in 1 2 3; do
  req $AUTH POST /auth/register "" "{\"correo\":\"e2e-pac$i@test.local\",\"clave\":\"$CLAVE\",\"cedula\":\"E2E000$i\",\"nombres\":\"Ana$i\",\"apellidos\":\"Perez\",\"fechaNacimiento\":\"1990-05-01\"}"
  req $AUTH POST /auth/login "" "{\"correo\":\"e2e-pac$i@test.local\",\"clave\":\"$CLAVE\"}"; eval "PAC$i=$(echo "$BODY" | json accessToken)"
  eval "req \$AUTH GET /pacientes/me \"\$PAC$i\""; eval "PAC${i}_ID=$(echo "$BODY" | json usuarioId)"
  eval "[ -z \"\$PAC${i}_ID\" ] && PAC${i}_ID=$(echo "$BODY" | json id)"
done
for i in 1 2; do
  req $AUTH POST /admin/usuarios "$ADMIN" "{\"correo\":\"e2e-doc$i@test.local\",\"clave\":\"$CLAVE\",\"rol\":\"PROFESIONAL\"}"; eval "DOC${i}_USER=$(echo "$BODY" | json usuarioId)"
  req $AUTH POST /auth/login "" "{\"correo\":\"e2e-doc$i@test.local\",\"clave\":\"$CLAVE\"}"; eval "DOC$i=$(echo "$BODY" | json accessToken)"
done

echo "== Catalogo y citas reales (catalog -> appointment real)"
req $CAT POST /especialidades "$ADMIN" '{"nombre":"Cardiologia"}'; CARDIO=$(echo "$BODY" | json id)
req $CAT POST /centros "$ADMIN" '{"nombre":"Sede Norte","direccion":"Calle 1","ciudad":"Cali"}'; NORTE=$(echo "$BODY" | json id)
req $CAT POST /profesionales "$ADMIN" "{\"usuarioId\":\"$DOC1_USER\",\"nombres\":\"Luis\",\"apellidos\":\"Gomez\",\"registroMedico\":\"RM-2001\"}"; PROF=$(echo "$BODY" | json id)
req $CAT POST /profesionales "$ADMIN" "{\"usuarioId\":\"$DOC2_USER\",\"nombres\":\"Eva\",\"apellidos\":\"Ruiz\",\"registroMedico\":\"RM-2002\"}"; PROF2=$(echo "$BODY" | json id)
for P in $PROF $PROF2; do
  req $CAT PUT "/profesionales/$P/especialidades" "$ADMIN" "{\"ids\":[\"$CARDIO\"]}"
  req $CAT PUT "/profesionales/$P/centros" "$ADMIN" "{\"ids\":[\"$NORTE\"]}"
done
read D3 D3W < <(python3 -I -c "
from datetime import datetime, timedelta; from zoneinfo import ZoneInfo
a=datetime.now(ZoneInfo('America/Bogota')).date()+timedelta(days=3)
print(a, a.isoweekday())")
for P in $PROF $PROF2; do
  req $CAT POST "/profesionales/$P/horarios" "$ADMIN" "{\"centroId\":\"$NORTE\",\"diaSemana\":$D3W,\"horaInicio\":\"08:00\",\"horaFin\":\"12:00\"}"; chk "horario con franjas generadas" 201 franjasGeneradas=True
done
req $APP GET "/disponibilidad?profesionalId=$PROF&centroId=$NORTE&fecha=$D3" "$PAC1"
F0=$(echo "$BODY" | json 0.id); F1=$(echo "$BODY" | json 1.id); F2=$(echo "$BODY" | json 2.id); F3=$(echo "$BODY" | json 3.id)
req $APP GET "/disponibilidad?profesionalId=$PROF2&centroId=$NORTE&fecha=$D3" "$PAC2"; X0=$(echo "$BODY" | json 0.id)
RES() { echo "{\"especialidadId\":\"$CARDIO\",\"profesionalId\":\"$1\",\"centroId\":\"$NORTE\",\"franjaId\":\"$2\"}"; }
req $APP POST /citas "$PAC1" "$(RES $PROF $F0)"; chk "CITA1 de PAC1 con el profesional 1" 201; CITA1=$(echo "$BODY" | json id)
req $APP POST /citas "$PAC1" "$(RES $PROF $F1)"; chk "CITA1B de PAC1 con el profesional 1" 201; CITA1B=$(echo "$BODY" | json id)
req $APP POST /citas "$PAC1" "$(RES $PROF $F2)"; CITA3=$(echo "$BODY" | json id)
req $APP POST /citas/$CITA3/cancelar "$PAC1" '{"motivo":"viaje"}'; chk "CITA3 (cancelada)" 200 estado=CANCELADA
req $APP POST /citas "$PAC2" "$(RES $PROF2 $X0)"; chk "CITA2 de PAC2 con el profesional 2" 201; CITA2=$(echo "$BODY" | json id)
req $APP POST /citas "$PAC3" "$(RES $PROF $F3)"; chk "CITA4 de PAC3 con el profesional 1 (para la concurrencia)" 201; CITA4=$(echo "$BODY" | json id)
req $APP PATCH /citas/$CITA1B/estado "$DOC1" '{"estado":"COMPLETADA"}'; chk "CITA1B marcada COMPLETADA" 200 estado=COMPLETADA

echo "== CU-28 registrar atencion"
req $CLI POST /entradas "" "$(ATENCION $CITA1)"; chk "sin token" 401 codigo=NO_AUTENTICADO
req $CLI POST /entradas "$PAC1" "$(ATENCION $CITA1)"; chk "un paciente no registra atenciones" 403 codigo=SOLO_PROFESIONAL
igual "el rechazo no devuelve texto clinico" "$(echo "$BODY" | grep -c "$MARCA")" 0
req $CLI POST /entradas "$ADMIN" "$(ATENCION $CITA1)"; chk "un administrativo tampoco" 403 codigo=SOLO_PROFESIONAL
req $CLI POST /entradas "$DOC1" '{}'; chk "faltan campos" 400 codigo=VALIDACION_ENTRADA
req $CLI POST /entradas "$DOC1" "{\"citaId\":\"$CITA1\",\"motivo\":\"m\",\"diagnostico\":\"d\",\"pacienteId\":\"$PAC2_ID\"}"; chk "no se acepta elegir el paciente" 400 codigo=CAMPO_NO_PERMITIDO
LARGO=$(python3 -I -c 'print("x"*5001)')
req $CLI POST /entradas "$DOC1" "{\"citaId\":\"$CITA1\",\"motivo\":\"$LARGO\",\"diagnostico\":\"d\"}"; chk "texto de mas de 5000 caracteres" 400 codigo=VALIDACION_ENTRADA
igual "el error de validacion no repite el texto" "$(echo "$BODY" | grep -c xxxxxxxx)" 0
req $CLI POST /entradas "$DOC1" "$(ATENCION $(UUID))"; chk "cita inexistente" 404 codigo=CITA_NO_ENCONTRADA
req $CLI POST /entradas "$DOC2" "$(ATENCION $CITA1)"; chk "otro profesional no es el de la cita" 403 codigo=NO_ES_TU_CITA
req $CLI POST /entradas "$DOC1" "$(ATENCION $CITA3)"; chk "cita CANCELADA no es atendible (RN-17)" 422 codigo=CITA_NO_ATENDIBLE
igual "nada se guardo en las pruebas anteriores" "$(SQLK "select count(*) from entrada_clinica")" 0
req $CLI POST /entradas "$DOC1" "$(ATENCION $CITA1)" "X-Correlation-Id: e2e-corr-0003"; chk "registrar sobre cita CONFIRMADA" 201 pacienteId=$PAC1_ID corrigeAId=None; E1=$(echo "$BODY" | json id)
igual "el profesional guardado es el de catalog, no el id de la cuenta" "$(SQLK "select profesional_id::text from entrada_clinica where id='$E1'")" "$PROF"
req $CLI POST /entradas "$DOC1" "$(ATENCION $CITA1)"; chk "segunda entrada original de la misma cita" 409 codigo=ATENCION_YA_REGISTRADA
req $CLI POST /entradas "$DOC1" "$(ATENCION $CITA1B)"; chk "registrar sobre cita COMPLETADA" 201; E1B=$(echo "$BODY" | json id)
igual "dos entradas guardadas" "$(SQLK "select count(*) from entrada_clinica")" 2
igual "log: rechazos de DOC1 sobre PAC1 (cancelada y duplicada)" "$(SQLK "select count(*) from log_acceso_clinico where usuario_id='$DOC1_USER' and paciente_id='$PAC1_ID' and accion='ESCRITURA' and resultado='DENEGADO'")" 2

echo "== CU-29 historia de un paciente"
req $CLI GET "/pacientes/$PAC1_ID/historia" "$DOC1"; chk "profesional con relacion lee la historia" 200; igual "dos entradas" "$(echo "$BODY" | n)" 2
igual "todas son de ese paciente" "$(echo "$BODY" | PACS)" "$PAC1_ID"
igual "orden cronologico" "$(echo "$BODY" | json 0.id)" "$E1"
req $CLI GET "/pacientes/$PAC2_ID/historia" "$DOC1"; chk "sin relacion con el paciente" 403 codigo=SIN_RELACION_CON_PACIENTE
req $CLI GET "/pacientes/$PAC1_ID/historia" "$DOC2"; chk "otro profesional sin relacion con PAC1" 403 codigo=SIN_RELACION_CON_PACIENTE
req $CLI GET "/pacientes/$PAC2_ID/historia" "$DOC2"; chk "DOC2 con relacion con PAC2: historia vacia" 200; igual "sin entradas todavia" "$(echo "$BODY" | n)" 0
req $CLI GET "/pacientes/$PAC1_ID/historia" "$PAC1"; chk "un paciente no usa la ruta de profesionales" 403 codigo=SOLO_PROFESIONAL
req $CLI GET "/pacientes/$PAC1_ID/historia" "$ADMIN"; chk "un administrativo no lee historias" 403 codigo=SOLO_PROFESIONAL
req $CLI GET "/pacientes/$PAC1_ID/historia" ""; chk "sin token" 401
req $CLI GET "/pacientes/no-es-uuid/historia" "$DOC1"; chk "id con formato invalido" 400
req $CLI POST /entradas "$DOC2" "$(ATENCION $CITA2)"; chk "DOC2 registra sobre CITA2" 201; E2=$(echo "$BODY" | json id)
req $CLI POST /entradas "$DOC1" "$(ATENCION $CITA2)"; chk "DOC1 no puede registrar sobre la cita de otro" 403 codigo=NO_ES_TU_CITA
req $CLI GET "/pacientes/$PAC2_ID/historia" "$DOC1"; chk "DOC1 sigue sin ver a PAC2 aunque ahora tenga historia" 403
req $CLI GET "/pacientes/$PAC2_ID/historia" "$DOC2"; igual "DOC2 ve la entrada de PAC2" "$(echo "$BODY" | n)" 1

echo "== CU-30 mi historia"
req $CLI GET /historia/mia "$PAC1"; chk "PAC1 ve su historia" 200; igual "dos entradas" "$(echo "$BODY" | n)" 2; igual "solo suyas" "$(echo "$BODY" | PACS)" "$PAC1_ID"
req $CLI GET /historia/mia "$PAC2"; igual "PAC2 ve solo la suya" "$(echo "$BODY" | n):$(echo "$BODY" | PACS)" "1:$PAC2_ID"
req $CLI GET /historia/mia "$PAC3"; chk "PAC3 sin entradas" 200; igual "lista vacia" "$(echo "$BODY" | n)" 0
req $CLI GET /historia/mia "$DOC1"; chk "un profesional no usa mi historia" 403 codigo=SOLO_PACIENTE
req $CLI GET /historia/mia ""; chk "sin token" 401

echo "== CU-31 corregir una entrada"
req $CLI POST /entradas/$E1/correcciones "$PAC1" "$(CORRECCION)"; chk "un paciente no corrige" 403 codigo=SOLO_PROFESIONAL
req $CLI POST /entradas/$E1/correcciones "$DOC2" "$(CORRECCION)"; chk "otro profesional no corrige" 403 codigo=NO_ES_TU_ENTRADA
req $CLI POST /entradas/$(UUID)/correcciones "$DOC1" "$(CORRECCION)"; chk "entrada inexistente" 404 codigo=ENTRADA_NO_ENCONTRADA
req $CLI POST /entradas/$E1/correcciones "$DOC1" '{"motivo":"x"}'; chk "correccion incompleta" 400
req $CLI POST /entradas/$E1/correcciones "$DOC1" "$(CORRECCION)"; chk "el autor corrige" 201 corrigeAId=$E1 pacienteId=$PAC1_ID citaId=$CITA1; C1=$(echo "$BODY" | json id)
igual "la correccion es otra entrada" "$([ "$C1" != "$E1" ] && echo si)" si
igual "la entrada original no cambio" "$(SQLK "select motivo from entrada_clinica where id='$E1'")" "Dolor $MARCA"
igual "tres entradas; la original sigue siendo la unica original de su cita" "$(SQLK "select count(*)||':'||count(*) filter (where cita_id='$CITA1' and corrige_a_id is null) from entrada_clinica where paciente_id='$PAC1_ID'")" "3:1"
req $CLI POST /entradas/$C1/correcciones "$DOC1" "$(CORRECCION)"; chk "se puede corregir la correccion" 201 corrigeAId=$C1
req $CLI GET /historia/mia "$PAC1"; igual "el paciente ve original y correcciones" "$(echo "$BODY" | n)" 4
req $CLI GET "/pacientes/$PAC1_ID/historia" "$DOC1"; igual "el profesional tambien" "$(echo "$BODY" | n)" 4

echo "== CU-32 log de acceso y append-only en la base"
igual "cada entrada tiene su log de escritura PERMITIDO (atomico)" "$(SQLK "select count(*) from entrada_clinica e join log_acceso_clinico l on l.entrada_id=e.id and l.accion='ESCRITURA' and l.resultado='PERMITIDO'")" "$(SQLK "select count(*) from entrada_clinica")"
igual "el log de escritura lleva al paciente y a quien la hizo" "$(SQLK "select usuario_id::text||'|'||rol||'|'||paciente_id::text from log_acceso_clinico where entrada_id='$E1' and resultado='PERMITIDO'")" "$DOC1_USER|PROFESIONAL|$PAC1_ID"
igual "PAC1: 3 rechazos (escribir, leer por profesional, corregir) y 2 lecturas" "$(SQLK "select count(*) filter (where resultado='DENEGADO')||':'||count(*) filter (where resultado='PERMITIDO') from log_acceso_clinico where usuario_id='$PAC1_ID'")" "3:2"
igual "rechazo por rol queda con el rol real" "$(SQLK "select count(distinct rol) from log_acceso_clinico where usuario_id='$PAC1_ID'")" 1
igual "el rechazo sin relacion registra al paciente consultado" "$(SQLK "select count(*) from log_acceso_clinico where usuario_id='$DOC1_USER' and paciente_id='$PAC2_ID' and accion='LECTURA' and resultado='DENEGADO'")" 2
igual "lecturas permitidas de DOC1 sobre PAC1" "$(SQLK "select count(*) from log_acceso_clinico where usuario_id='$DOC1_USER' and paciente_id='$PAC1_ID' and accion='LECTURA' and resultado='PERMITIDO'")" 2
igual "el log tiene fecha en UTC reciente" "$(SQLK "select bool_and(fecha > (now() at time zone 'utc') - interval '30 minutes' and fecha <= (now() at time zone 'utc') + interval '1 minute') from log_acceso_clinico")" t
igual "UPDATE sobre una entrada es rechazado por la base" "$(SQLK "update entrada_clinica set motivo='x' where id='$E1'" 2>&1 | grep -c 'solo insercion')" 1
igual "DELETE de una entrada es rechazado" "$(SQLK "delete from entrada_clinica where id='$E1'" 2>&1 | grep -c 'solo insercion')" 1
igual "UPDATE del log es rechazado" "$(SQLK "update log_acceso_clinico set resultado='PERMITIDO'" 2>&1 | grep -c 'solo insercion')" 1
igual "DELETE del log es rechazado" "$(SQLK "delete from log_acceso_clinico" 2>&1 | grep -c 'solo insercion')" 1
igual "la entrada original sigue intacta" "$(SQLK "select motivo from entrada_clinica where id='$E1'")" "Dolor $MARCA"
igual "sin FK hacia otros schemas" "$(SQLK "select count(*) from information_schema.table_constraints c join information_schema.constraint_column_usage u using (constraint_name) where c.constraint_schema='clinical' and c.constraint_type='FOREIGN KEY' and u.table_schema<>'clinical'")" 0

echo "== Concurrencia: 6 registros simultaneos sobre la misma cita"
rm -f $S/conc-cli.txt; PIDS=()
for i in 1 2 3 4 5 6; do curl -s -m 20 -o /dev/null -w '%{http_code}\n' -X POST $CLI/entradas -H 'Content-Type: application/json' -H "Authorization: Bearer $DOC1" -d "$(ATENCION $CITA4)" >> $S/conc-cli.txt & PIDS+=($!); done
wait "${PIDS[@]}"
igual "exactamente un registro con exito (201)" "$(grep -c '^201$' $S/conc-cli.txt)" 1
igual "los otros 5 reciben 409 controlado" "$(grep -c '^409$' $S/conc-cli.txt)" 5
igual "ningun 5xx" "$(grep -c '^5' $S/conc-cli.txt)" 0
igual "una sola entrada original para esa cita" "$(SQLK "select count(*) from entrada_clinica where cita_id='$CITA4' and corrige_a_id is null")" 1
igual "los 5 perdedores quedaron en el log como DENEGADO" "$(SQLK "select count(*) from log_acceso_clinico where usuario_id='$DOC1_USER' and paciente_id='$PAC3_ID' and accion='ESCRITURA' and resultado='DENEGADO'")" 5

echo "== API interna: /internal solo con llave"
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' http://localhost:8084/internal/lo-que-sea); igual "sin llave" "$CODE" 401
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -H "Authorization: Bearer $ADMIN" http://localhost:8084/internal/lo-que-sea); igual "un JWT no abre /internal" "$CODE" 401
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -H "X-Api-Key: $INTERNAL_API_KEY" "$CLI/historia/mia"); igual "la llave no abre la API de usuarios" "$CODE" 401
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -H "X-Api-Key: $INTERNAL_API_KEY" http://localhost:8083/internal/citas/$CITA1); igual "appointment: GET /internal/citas/{id} con llave" "$CODE" 200
curl -s -m 5 -H "X-Api-Key: $INTERNAL_API_KEY" http://localhost:8083/internal/citas/$CITA1 > /tmp/e2e-cli-body; igual "devuelve solo ids y estado" "$(python3 -I -c 'import sys,json; print(",".join(sorted(json.load(sys.stdin))))' < /tmp/e2e-cli-body)" "citaId,estado,pacienteId,profesionalId"
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' http://localhost:8083/internal/citas/$CITA1); igual "appointment: sin llave" "$CODE" 401
CODE=$(curl -s -m 5 -o /dev/null -w '%{http_code}' -H "X-Api-Key: $INTERNAL_API_KEY" http://localhost:8083/internal/citas/$(UUID)); igual "appointment: cita inexistente" "$CODE" 404

echo "== Appointment caido: cache de relaciones y 503 controlado"
kill $APP_PID; wait $APP_PID 2>/dev/null
req $CLI GET "/pacientes/$PAC1_ID/historia" "$DOC1"; chk "relacion ya confirmada: sale de la cache de Caffeine" 200; igual "con sus entradas" "$(echo "$BODY" | n)" 4
req $CLI GET "/pacientes/$PAC3_ID/historia" "$DOC1"; chk "relacion no cacheada: 503 controlado" 503 codigo=APPOINTMENT_NO_DISPONIBLE
req $CLI POST /entradas "$DOC2" "$(ATENCION $CITA2)"; chk "registrar necesita appointment: 503" 503 codigo=APPOINTMENT_NO_DISPONIBLE
req $CLI GET /historia/mia "$PAC1"; chk "mi historia no depende de appointment" 200
igual "log: la lectura que dio 503 quedo como DENEGADO" "$(SQLK "select count(*) from log_acceso_clinico where usuario_id='$DOC1_USER' and paciente_id='$PAC3_ID' and accion='LECTURA' and resultado='DENEGADO'")" 1
igual "log: el registro que dio 503 quedo como DENEGADO (cita desconocida)" "$(SQLK "select count(*) from log_acceso_clinico where usuario_id='$DOC2_USER' and paciente_id is null and accion='ESCRITURA' and resultado='DENEGADO'")" 1
igual "las entradas no cambiaron" "$(SQLK "select count(*) from entrada_clinica")" 6
echo "== Catalog caido"
kill $CAT_PID; wait $CAT_PID 2>/dev/null
req $CLI GET "/pacientes/$PAC1_ID/historia" "$DOC1"; chk "sin catalog no se resuelve al profesional: 503" 503 codigo=CATALOG_NO_DISPONIBLE
req $CLI GET /historia/mia "$PAC2"; chk "el paciente sigue leyendo su historia" 200

echo "== Errores uniformes, privacidad y logs"
req $CLI GET /ruta-que-no-existe "$ADMIN"; chk "ruta inexistente" 404
req $CLI POST /entradas "$DOC1" '{"x":'; chk "JSON ilegible" 400 codigo=SOLICITUD_ILEGIBLE
echo "   cuerpo de error: $(echo "$BODY" | python3 -I -c 'import sys,json; print(sorted(json.load(sys.stdin).keys()))')"
igual "ninguna respuesta de error repite texto clinico (ultima)" "$(echo "$BODY" | grep -c "$MARCA")" 0
igual "texto clinico en el log de clinical" "$(grep -c "$MARCA" "$LOGK")" 0
igual "secretos en el log de clinical (llave, jwt, claves, tokens)" "$(grep -c -e "$INTERNAL_API_KEY" -e "$JWT_SECRET" -e "$CLAVE" -e "$PAC1" -e "$DOC1" "$LOGK")" 0
igual "correos o nombres en el log de clinical" "$(grep -c -E 'e2e-pac|e2e-doc|Perez|RM-200' "$LOGK")" 0
igual "errores inesperados en el log de clinical (se esperan solo appointment y catalog caidos)" "$(grep ' ERROR ' "$LOGK" | grep -vc -E 'appointment no respondio|catalog no respondio')" 0
igual "el log de clinical no incluye mensajes SQL del driver (traen la fila rechazada)" "$(grep -c -E 'SqlExceptionHelper|duplicate key|Failing row' "$LOGK")" 0
igual "clinical devuelve el id de correlacion recibido" "$(curl -s -D - -o /dev/null -H 'X-Correlation-Id: e2e-corr-0003' $CLI/historia/mia | grep -ci 'x-correlation-id: e2e-corr-0003')" 1

echo; echo "RESULTADO: $PASS correctas, $FAIL fallidas"
