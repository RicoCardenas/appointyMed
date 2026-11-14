#!/usr/bin/env bash
# Prueba de punta a punta: auth real + notification real (correo simulado), contra PostgreSQL local.
# Requisitos: mvn -DskipTests package, .env con las claves de BD, puertos 8081 y 8085 libres. Borra los datos que crea.
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
export MAIL_ENABLED=false
CLAVE="Aa1$(openssl rand -hex 6)"
AUTH=http://localhost:8081/api/v1; NOT=http://localhost:8085/api/v1; INT=http://localhost:8085/internal
LOGA=$S/e2e-auth.log; LOGN=$S/e2e-notification.log
SQLA() { PGPASSWORD="$AUTH_DB_PASSWORD" psql -h localhost -U auth_svc -d appointy -Atqc "$1"; }
SQLN() { PGPASSWORD="$NOTIFICATION_DB_PASSWORD" psql -h localhost -U notification_svc -d appointy -Atqc "$1"; }
PASS=0; FAIL=0

for p in 8081 8085; do ss -ltn "sport = :$p" | grep -q LISTEN && { echo "ABORTO: el puerto $p esta ocupado"; exit 1; }; done

java -jar auth-service/target/auth-service-0.1.0-SNAPSHOT.jar > "$LOGA" 2>&1 & AUTH_PID=$!
java -jar notification-service/target/notification-service-0.1.0-SNAPSHOT.jar > "$LOGN" 2>&1 & NOT_PID=$!
cleanup() {
  kill $AUTH_PID $NOT_PID 2>/dev/null; wait 2>/dev/null
  SQLN "delete from notificacion;" >/dev/null 2>&1
  SQLA "delete from paciente_perfil; delete from token_revocado; delete from usuario;" >/dev/null 2>&1
  echo "limpieza: notificaciones=$(SQLN 'select count(*) from notificacion') auth usuarios=$(SQLA 'select count(*) from usuario')"
}
trap cleanup EXIT

esperar() { for i in $(seq 1 50); do curl -s -m 2 "localhost:$1/actuator/health" | grep -q '"UP"' && return 0; sleep 1; done; return 1; }
esperar 8081 && esperar 8085 || { echo "Un servicio no arranco"; tail -15 "$LOGN"; exit 1; }

for i in $(seq 1 20); do [ "$(SQLA "select count(*) from usuario where correo='$ADMIN_EMAIL'")" = "1" ] && break; sleep 0.5; done   # el administrador se siembra tras el arranque
# Seguridad de la prueba: no se toca nada si ya habia datos
[ "$(SQLA "select count(*) from usuario")" = "1" ] && [ "$(SQLA "select count(*) from usuario where correo='$ADMIN_EMAIL'")" = "1" ] || { echo "ABORTO: auth ya tenia datos"; trap - EXIT; kill $AUTH_PID $NOT_PID; exit 1; }
[ "$(SQLN "select count(*) from notificacion")" = "0" ] || { echo "ABORTO: notification ya tenia datos"; trap - EXIT; kill $AUTH_PID $NOT_PID; exit 1; }

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
  local args=(-s -m 20 -o /tmp/e2e-not-body -w '%{http_code}' -X "$m" "$base$p" -H 'Content-Type: application/json')
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$b" ] && args+=(-d "$b")
  [ -n "$extra" ] && args+=(-H "$extra")
  HTTP_CODE=$(curl "${args[@]}"); BODY=$(cat /tmp/e2e-not-body)
}
chk() {  # chk NOMBRE ESPERADO [campo=valor ...]
  local ok=1 n=$1 e=$2; shift 2
  [ "$HTTP_CODE" = "$e" ] || ok=0
  for c in "$@"; do [ "$(echo "$BODY" | json "${c%%=*}")" = "${c#*=}" ] || ok=0; done
  if [ $ok = 1 ]; then PASS=$((PASS+1)); printf 'PASS %-62s %s\n' "$n" "$HTTP_CODE"
  else FAIL=$((FAIL+1)); printf 'FAIL %-62s esperado=%s real=%s %s\n' "$n" "$e" "$HTTP_CODE" "$(echo "$BODY" | head -c 260)"; fi
}
igual() { if [ "$2" = "$3" ]; then PASS=$((PASS+1)); printf 'PASS %-62s %s\n' "$1" "$2"; else FAIL=$((FAIL+1)); printf 'FAIL %-62s esperado=%s real=%s\n' "$1" "$3" "$2"; fi; }

echo "== Cuentas reales en auth"
req $AUTH POST /auth/login "" "{\"correo\":\"$ADMIN_EMAIL\",\"clave\":\"$ADMIN_PASSWORD\"}"; chk "login del administrador sembrado" 200 rol=ADMINISTRATIVO
ADMIN=$(echo "$BODY" | json accessToken)
req $AUTH POST /auth/register "" "{\"correo\":\"e2e-pac@test.local\",\"clave\":\"$CLAVE\",\"cedula\":\"E2E0001\",\"nombres\":\"Ana\",\"apellidos\":\"Perez\",\"fechaNacimiento\":\"1990-05-01\"}"; chk "registro de un paciente" 201
req $AUTH POST /auth/login "" "{\"correo\":\"e2e-pac@test.local\",\"clave\":\"$CLAVE\"}"; PAC=$(echo "$BODY" | json accessToken)
PAC_ID=$(echo "$PAC" | cut -d. -f2 | python3 -I -c "import sys,base64,json; s=sys.stdin.read().strip(); s+='='*(-len(s)%4); print(json.loads(base64.urlsafe_b64decode(s))['sub'])")
CITA=$(python3 -I -c "import uuid; print(uuid.uuid4())"); CITA2=$(python3 -I -c "import uuid; print(uuid.uuid4())")
F1=$(date -d '+10 days' +%F); F2=$(date -d '+12 days' +%F)
SOL() { printf '{"tipo":"%s","citaId":"%s","pacienteId":"%s","profesionalId":"%s","centroId":"%s","fecha":"%s","horaInicio":"%s","actorId":"%s"}' "$1" "$2" "$PAC_ID" "$PAC_ID" "$PAC_ID" "$3" "$4" "$PAC_ID"; }

echo "== Seguridad de /internal (RNF-04)"
req $INT POST /notificaciones "" "$(SOL CONFIRMADA $CITA $F1 08:00)"; chk "sin llave" 401 codigo=NO_AUTENTICADO
req $INT POST /notificaciones "$ADMIN" "$(SOL CONFIRMADA $CITA $F1 08:00)"; chk "un JWT de usuario no abre /internal" 401
req $INT POST /notificaciones "" "$(SOL CONFIRMADA $CITA $F1 08:00)" "X-Api-Key: llave-equivocada-de-24-caracteres-o-mas"; chk "llave equivocada" 401
req $NOT GET /notificaciones "" "" "X-Api-Key: $INTERNAL_API_KEY"; chk "la llave no abre la API de usuarios" 401

echo "== CU-33 notificar cita"
K="X-Api-Key: $INTERNAL_API_KEY"
req $INT POST /notificaciones "" "$(SOL CONFIRMADA $CITA $F1 08:00)" "$K"; chk "confirmada: correo enviado" 201 estado=ENVIADA
ID1=$(echo "$BODY" | json id)
req $INT POST /notificaciones "" "$(SOL CONFIRMADA $CITA $F1 08:00)" "$K"; chk "repetida: misma notificacion (idempotente)" 201 id=$ID1
igual "filas de la cita (confirmacion + recordatorio)" "$(SQLN "select count(*) from notificacion where cita_id='$CITA'")" 2
igual "recordatorio pendiente 24 h antes" "$(SQLN "select count(*) from notificacion where cita_id='$CITA' and tipo='RECORDATORIO' and estado='PENDIENTE'")" 1
req $INT POST /notificaciones "" "$(SOL REPROGRAMADA $CITA $F2 09:30)" "$K"; chk "reprogramada: correo enviado" 201 estado=ENVIADA
igual "recordatorio viejo cancelado" "$(SQLN "select count(*) from notificacion where cita_id='$CITA' and tipo='RECORDATORIO' and estado='CANCELADA'")" 1
igual "recordatorio nuevo pendiente" "$(SQLN "select count(*) from notificacion where cita_id='$CITA' and tipo='RECORDATORIO' and estado='PENDIENTE'")" 1
req $INT POST /notificaciones "" "$(SOL CANCELADA $CITA $F2 09:30)" "$K"; chk "cancelada: correo enviado" 201 estado=ENVIADA
igual "ya no quedan recordatorios pendientes" "$(SQLN "select count(*) from notificacion where cita_id='$CITA' and estado='PENDIENTE'")" 0
req $INT POST /notificaciones "" "$(SOL OTRA $CITA2 $F1 08:00)" "$K"; chk "tipo invalido" 400 codigo=TIPO_INVALIDO
req $INT POST /notificaciones "" "$(SOL CONFIRMADA no-uuid $F1 08:00)" "$K"; chk "citaId invalido" 400 codigo=ID_INVALIDO
req $INT POST /notificaciones "" "$(SOL CONFIRMADA $CITA2 2026-13-45 08:00)" "$K"; chk "fecha imposible" 400 codigo=FECHA_INVALIDA
req $INT POST /notificaciones "" '{"tipo":"CONFIRMADA"}' "$K"; chk "faltan campos" 400 codigo=VALIDACION_ENTRADA
req $INT POST /notificaciones "" "$(printf '{"tipo":"CONFIRMADA","citaId":"%s","pacienteId":"%s","fecha":"%s","horaInicio":"08:00"}' $CITA2 00000000-0000-0000-0000-000000000000 $F1)" "$K"; chk "paciente inexistente en auth" 404 codigo=DESTINATARIO_NO_ENCONTRADO
igual "no se registro nada para el paciente inexistente" "$(SQLN "select count(*) from notificacion where cita_id='$CITA2'")" 0

echo "== API de administracion (solo ADMINISTRATIVO)"
req $NOT GET /notificaciones; chk "sin token" 401
req $NOT GET /notificaciones "$PAC"; chk "paciente no lista" 403 codigo=ACCESO_DENEGADO
req $NOT GET "/notificaciones?size=2" "$ADMIN"; chk "GET all paginado: pagina 0 de tamano 2" 200 pagina=0 tamano=2 totalElementos=5 totalPaginas=3 contenido.0.tipo=CANCELACION
req $NOT GET "/notificaciones?size=2&page=2" "$ADMIN"; chk "ultima pagina" 200 pagina=2
req $NOT GET "/notificaciones?estado=CANCELADA" "$ADMIN"; chk "filtro por estado" 200 totalElementos=2
req $NOT GET "/notificaciones?tipo=RECORDATORIO&citaId=$CITA" "$ADMIN"; chk "filtro por tipo y cita" 200 totalElementos=2
req $NOT GET "/notificaciones?size=0" "$ADMIN"; chk "tamano invalido" 400 codigo=TAMANO_INVALIDO
req $NOT GET "/notificaciones?page=-1" "$ADMIN"; chk "pagina negativa" 400 codigo=PAGINA_INVALIDA
req $NOT GET "/notificaciones?estado=RARA" "$ADMIN"; chk "estado desconocido" 400
req $NOT GET "/notificaciones?citaId=xx" "$ADMIN"; chk "citaId con formato invalido" 400 codigo=PARAMETRO_INVALIDO
igual "el correo sale enmascarado" "$(echo "$BODY" >/dev/null; curl -s -m 5 "$NOT/notificaciones?size=1" -H "Authorization: Bearer $ADMIN" | json contenido.0.destinatario)" "e***@test.local"
req $NOT GET /notificaciones/estadisticas "$ADMIN"; chk "estadisticas" 200 enviadas=3 canceladas=2 pendientes=0 recordatorios=2
req $NOT GET "/notificaciones/cita/$CITA" "$ADMIN"; chk "historial por cita" 200 0.tipo=CONFIRMACION 1.tipo=RECORDATORIO
req $NOT POST "/notificaciones/$ID1/reenviar" "$ADMIN"; chk "reenviar una ya enviada" 409 codigo=YA_ENVIADA
req $NOT POST "/notificaciones/00000000-0000-0000-0000-000000000000/reenviar" "$ADMIN"; chk "reenviar inexistente" 404

echo "== CU-34 recordatorios y CU-35 reintentos"
req $NOT POST /notificaciones/recordatorios/ejecutar "$ADMIN"; chk "aun no vence ninguno" 200 procesadas=0
SQLN "update notificacion set programada_para = now() at time zone 'utc' - interval '1 minute' where cita_id='$CITA' and tipo='RECORDATORIO' and estado='PENDIENTE'" >/dev/null
# El recordatorio pendiente ya no existe (la cita se cancelo): se crea uno de prueba para ejercitar el job
req $INT POST /notificaciones "" "$(SOL CONFIRMADA $CITA2 $F1 10:00)" "$K"; chk "otra cita confirmada" 201
SQLN "update notificacion set programada_para = now() at time zone 'utc' - interval '1 minute' where cita_id='$CITA2' and tipo='RECORDATORIO'" >/dev/null
req $NOT POST /notificaciones/recordatorios/ejecutar "$ADMIN"; chk "el job envia el recordatorio vencido" 200 procesadas=1 enviadas=1 fallidas=0
igual "recordatorio enviado" "$(SQLN "select estado from notificacion where cita_id='$CITA2' and tipo='RECORDATORIO'")" ENVIADA
SQLN "update notificacion set estado='FALLIDA', intentos=1, ultimo_error='CORREO_NO_DISPONIBLE', enviada_en=null where cita_id='$CITA2' and tipo='CONFIRMACION'" >/dev/null
req $NOT POST /notificaciones/reintentos/ejecutar "$ADMIN"; chk "el job reintenta la fallida" 200 procesadas=1 enviadas=1
igual "tras el reintento: ENVIADA con 2 intentos" "$(SQLN "select estado||intentos from notificacion where cita_id='$CITA2' and tipo='CONFIRMACION'")" ENVIADA2
SQLN "update notificacion set estado='FALLIDA', intentos=3 where cita_id='$CITA2' and tipo='CONFIRMACION'" >/dev/null
req $NOT POST /notificaciones/reintentos/ejecutar "$ADMIN"; chk "con 3 intentos ya no se reintenta" 200 procesadas=0
ID2=$(SQLN "select id from notificacion where cita_id='$CITA2' and tipo='CONFIRMACION'")
req $NOT POST "/notificaciones/$ID2/reenviar" "$ADMIN"; chk "el administrativo la reenvia a mano" 200 estado=ENVIADA

echo "== Correlacion (RNF-17)"
CORR=e2e-corr-0005
CODE=$(curl -s -m 20 -o /dev/null -w '%{http_code}' -X POST $INT/notificaciones -H 'Content-Type: application/json' -H "$K" -H "X-Correlation-Id: $CORR" -d "$(SOL CANCELADA $CITA2 $F1 10:00)"); igual "notificar con un id de correlacion" "$CODE" 201
igual "el id aparece en el log de notification" "$([ "$(grep -c "\[$CORR\]" "$LOGN")" -ge 1 ] && echo si || echo no)" si
igual "la respuesta devuelve el mismo id" "$(curl -s -m 5 -D - -o /dev/null -H "X-Correlation-Id: $CORR" $NOT/notificaciones | tr -d '\r' | grep -ci "x-correlation-id: $CORR")" 1

echo "== Privacidad (RNF-05)"
igual "el log no contiene correos" "$(grep -c 'e2e-pac@test.local' "$LOGN")" 0
igual "el log no contiene el cuerpo" "$(grep -c 'Codigo de la cita' "$LOGN")" 0

echo; echo "RESULTADO: $PASS correctas, $FAIL fallidas"; [ $FAIL = 0 ]
