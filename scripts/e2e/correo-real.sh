#!/usr/bin/env bash
# Envia UN correo real (confirmacion de cita) por la API de Brevo a la direccion que se pasa como argumento.
# Uso: bash scripts/e2e/correo-real.sh destino@correo.com
# Requisitos: mvn -DskipTests package, PostgreSQL local, .env con claves de BD, BREVO_API_KEY, MAIL_ENABLED=true,
# MAIL_PROVIDER=brevo y MAIL_FROM (remitente verificado en Brevo). Puertos 8081 y 8085 libres. Solo borra lo que crea (tolera datos locales previos).
# El destino no se imprime ni se guarda; las claves se generan aqui y tampoco se imprimen.
set -u
DESTINO="${1:-}"; [ -n "$DESTINO" ] || { echo "Uso: bash scripts/e2e/correo-real.sh destino@correo.com"; exit 1; }
DIR="$(cd "$(dirname "$0")" && pwd)"; cd "$DIR/../.."
S=/tmp/appointy-e2e; mkdir -p $S
set -a; source .env; set +a
[ "${MAIL_ENABLED:-}" = "true" ] && [ "${MAIL_PROVIDER:-}" = "brevo" ] && [ -n "${BREVO_API_KEY:-}" ] && [ -n "${MAIL_FROM:-}" ] \
  || { echo "ABORTO: faltan MAIL_ENABLED=true, MAIL_PROVIDER=brevo, BREVO_API_KEY o MAIL_FROM en .env"; exit 1; }
export JWT_SECRET="$(openssl rand -hex 32)" INTERNAL_API_KEY="$(openssl rand -hex 16)"
export ADMIN_EMAIL="e2e-admin@test.local" ADMIN_PASSWORD="Aa1$(openssl rand -hex 8)"
CLAVE="Aa1$(openssl rand -hex 6)"
CITA=$(python3 -I -c "import uuid; print(uuid.uuid4())")
AUTH=http://localhost:8081/api/v1; INT=http://localhost:8085/internal
LOGA=$S/real-auth.log; LOGN=$S/real-notification.log
SQLA() { PGPASSWORD="$AUTH_DB_PASSWORD" psql -h localhost -U auth_svc -d appointy -Atqc "$1"; }
SQLN() { PGPASSWORD="$NOTIFICATION_DB_PASSWORD" psql -h localhost -U notification_svc -d appointy -Atqc "$1"; }
for p in 8081 8085; do ss -ltn "sport = :$p" | grep -q LISTEN && { echo "ABORTO: el puerto $p esta ocupado"; exit 1; }; done
CEDULA="R$(date +%s)"
[ "$(SQLA "select count(*) from usuario where lower(correo)=lower('$DESTINO')")" = "0" ] || { echo "ABORTO: ya existe un usuario con ese correo; no se toca"; exit 1; }

java -jar auth-service/target/auth-service-0.1.0-SNAPSHOT.jar > "$LOGA" 2>&1 & AUTH_PID=$!
java -jar notification-service/target/notification-service-0.1.0-SNAPSHOT.jar > "$LOGN" 2>&1 & NOT_PID=$!
cleanup() {
  kill $AUTH_PID $NOT_PID 2>/dev/null; wait 2>/dev/null
  # Solo borra lo que creo esta prueba (el resto de los datos locales no se toca)
  SQLN "delete from notificacion where cita_id='$CITA';" >/dev/null 2>&1
  SQLA "delete from paciente_perfil where cedula='$CEDULA'; delete from usuario where lower(correo)=lower('$DESTINO');" >/dev/null 2>&1
  echo "limpieza: notificaciones de la prueba=$(SQLN "select count(*) from notificacion where cita_id='$CITA'") usuarios de la prueba=$(SQLA "select count(*) from usuario where lower(correo)=lower('$DESTINO')")"
}
trap cleanup EXIT
esperar() { for i in $(seq 1 60); do curl -s -m 2 "localhost:$1/actuator/health" | grep -q '"UP"' && return 0; sleep 1; done; return 1; }
esperar 8081 && esperar 8085 || { echo "Un servicio no arranco"; tail -15 "$LOGN"; exit 1; }

CODE=$(curl -s -m 20 -o /tmp/e2e-real-body -w '%{http_code}' -X POST $AUTH/auth/register -H 'Content-Type: application/json' \
  -d "{\"correo\":\"$DESTINO\",\"clave\":\"$CLAVE\",\"cedula\":\"$CEDULA\",\"nombres\":\"Paciente\",\"apellidos\":\"Demo\",\"fechaNacimiento\":\"1990-05-01\"}")
[ "$CODE" = "201" ] || { echo "FAIL registro del paciente: HTTP $CODE"; exit 1; }
TOKEN=$(curl -s -m 20 -X POST $AUTH/auth/login -H 'Content-Type: application/json' -d "{\"correo\":\"$DESTINO\",\"clave\":\"$CLAVE\"}" \
  | python3 -I -c "import sys,json; print(json.load(sys.stdin)['accessToken'])")
PAC_ID=$(echo "$TOKEN" | cut -d. -f2 | python3 -I -c "import sys,base64,json; s=sys.stdin.read().strip(); s+='='*(-len(s)%4); print(json.loads(base64.urlsafe_b64decode(s))['sub'])")
F1=$(date -d '+10 days' +%F)
CUERPO=$(printf '{"tipo":"CONFIRMADA","citaId":"%s","pacienteId":"%s","profesionalId":"%s","centroId":"%s","fecha":"%s","horaInicio":"08:00","actorId":"%s"}' "$CITA" "$PAC_ID" "$PAC_ID" "$PAC_ID" "$F1" "$PAC_ID")
CODE=$(curl -s -m 30 -o /tmp/e2e-real-body -w '%{http_code}' -X POST $INT/notificaciones -H 'Content-Type: application/json' -H "X-Api-Key: $INTERNAL_API_KEY" -d "$CUERPO")
ESTADO=$(python3 -I -c "import json; print(json.load(open('/tmp/e2e-real-body')).get('estado',''))")
echo "HTTP $CODE, estado de la notificacion: $ESTADO"
if [ "$ESTADO" = "ENVIADA" ]; then echo "PASS Brevo acepto el correo; revisa la bandeja (y spam)"
else echo "FAIL ultimo_error=$(SQLN "select ultimo_error from notificacion where cita_id='$CITA' and tipo='CONFIRMACION'")"; fi
rm -f /tmp/e2e-real-body
