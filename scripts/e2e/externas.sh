#!/usr/bin/env bash
# Prueba de las 2 APIs externas (RA-06) con la red real: festivos (Nager.Date, via catalog) y CIE-10 (NLM, via clinical-history).
# Requiere internet, mvn -DskipTests package, .env con las claves de BD y los puertos 8081, 8082 y 8084 libres. Borra los datos que crea.
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
AUTH=http://localhost:8081/api/v1; CAT=http://localhost:8082/api/v1; CLI=http://localhost:8084/api/v1
LOGA=$S/ext-auth.log; LOGC=$S/ext-catalog.log; LOGK=$S/ext-clinical.log
SQLA() { PGPASSWORD="$AUTH_DB_PASSWORD" psql -h localhost -U auth_svc -d appointy -Atqc "$1"; }
PASS=0; FAIL=0
for p in 8081 8082 8084; do ss -ltn "sport = :$p" | grep -q LISTEN && { echo "ABORTO: el puerto $p esta ocupado"; exit 1; }; done
java -jar auth-service/target/auth-service-0.1.0-SNAPSHOT.jar > "$LOGA" 2>&1 & AUTH_PID=$!
java -jar catalog-service/target/catalog-service-0.1.0-SNAPSHOT.jar > "$LOGC" 2>&1 & CAT_PID=$!
java -jar clinical-history-service/target/clinical-history-service-0.1.0-SNAPSHOT.jar > "$LOGK" 2>&1 & CLI_PID=$!
cleanup() {
  kill $AUTH_PID $CAT_PID $CLI_PID 2>/dev/null; wait 2>/dev/null
  SQLA "delete from paciente_perfil; delete from token_revocado; delete from usuario;" >/dev/null 2>&1
  echo "limpieza: auth usuarios=$(SQLA 'select count(*) from usuario')"
}
trap cleanup EXIT
esperar() { for i in $(seq 1 50); do curl -s -m 2 "localhost:$1/actuator/health" | grep -q '"UP"' && return 0; sleep 1; done; return 1; }
esperar 8081 && esperar 8082 && esperar 8084 || { echo "Un servicio no arranco"; exit 1; }
for i in $(seq 1 20); do [ "$(SQLA "select count(*) from usuario where correo='$ADMIN_EMAIL'")" = "1" ] && break; sleep 0.5; done
[ "$(SQLA "select count(*) from usuario")" = "1" ] || { echo "ABORTO: auth ya tenia datos"; trap - EXIT; kill $AUTH_PID $CAT_PID $CLI_PID; exit 1; }

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
  local args=(-s -m 20 -o /tmp/e2e-ext-body -w '%{http_code}' -X "$m" "$base$p" -H 'Content-Type: application/json')
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$b" ] && args+=(-d "$b")
  [ -n "$extra" ] && args+=(-H "$extra")
  HTTP_CODE=$(curl "${args[@]}"); BODY=$(cat /tmp/e2e-ext-body)
}
chk() {  # chk NOMBRE ESPERADO [campo=valor ...]
  local ok=1 n=$1 e=$2; shift 2
  [ "$HTTP_CODE" = "$e" ] || ok=0
  for c in "$@"; do [ "$(echo "$BODY" | json "${c%%=*}")" = "${c#*=}" ] || ok=0; done
  if [ $ok = 1 ]; then PASS=$((PASS+1)); printf 'PASS %-62s %s\n' "$n" "$HTTP_CODE"
  else FAIL=$((FAIL+1)); printf 'FAIL %-62s esperado=%s real=%s %s\n' "$n" "$e" "$HTTP_CODE" "$(echo "$BODY" | head -c 260)"; fi
}
igual() { if [ "$2" = "$3" ]; then PASS=$((PASS+1)); printf 'PASS %-62s %s\n' "$1" "$2"; else FAIL=$((FAIL+1)); printf 'FAIL %-62s esperado=%s real=%s\n' "$1" "$3" "$2"; fi; }

echo "== Cuentas"
req $AUTH POST /auth/login "" "{\"correo\":\"$ADMIN_EMAIL\",\"clave\":\"$ADMIN_PASSWORD\"}"; chk "login del administrador" 200; ADMIN=$(echo "$BODY" | json accessToken)
req $AUTH POST /admin/usuarios "$ADMIN" "{\"correo\":\"e2e-doc@test.local\",\"clave\":\"$CLAVE\",\"rol\":\"PROFESIONAL\"}"; chk "cuenta de profesional" 201
req $AUTH POST /auth/login "" "{\"correo\":\"e2e-doc@test.local\",\"clave\":\"$CLAVE\"}"; DOC=$(echo "$BODY" | json accessToken)

echo "== API externa 1: festivos de Colombia (Nager.Date) a traves de catalog"
T0=$(date +%s%N)
req $CAT GET "/festivos?anio=2026" "$ADMIN"; chk "festivos 2026" 200 0.fecha=2026-01-01
T1=$(date +%s%N)
igual "hay mas de 15 festivos" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(len(json.load(sys.stdin))>15)')" True
igual "los nombres vienen en espanol" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(any("Navidad" in f["nombre"] for f in json.load(sys.stdin)))')" True
req $CAT GET "/festivos?anio=2026" "$ADMIN"; chk "segunda consulta (desde cache)" 200
T2=$(date +%s%N)
echo "   primera: $(( (T1-T0)/1000000 )) ms, segunda (cache): $(( (T2-T1)/1000000 )) ms"
req $CAT GET "/festivos?anio=2027" "$ADMIN"; chk "otro anio" 200
req $CAT GET "/festivos?anio=1999" "$ADMIN"; chk "anio fuera de rango" 400 codigo=ANIO_INVALIDO
req $CAT GET "/festivos?anio=2026" ""; chk "sin token" 401

echo "== API externa 2: catalogo CIE-10 (NLM Clinical Tables) a traves de clinical-history"
req $CLI GET "/diagnosticos?q=diabetes" "$DOC"; chk "busqueda de diabetes" 200
igual "hay resultados con codigo y descripcion" "$(echo "$BODY" | python3 -I -c 'import sys,json; d=json.load(sys.stdin); print(len(d)>0 and "codigo" in d[0] and "descripcion" in d[0])')" True
echo "   primeros: $(echo "$BODY" | python3 -I -c 'import sys,json; print([d["codigo"] for d in json.load(sys.stdin)][:5])')"
req $CLI GET "/diagnosticos?q=E11.9" "$DOC"; chk "busqueda por codigo" 200
req $CLI GET "/diagnosticos?q=zzzzqqqq" "$DOC"; chk "sin coincidencias devuelve lista vacia" 200
igual "lista vacia" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(len(json.load(sys.stdin)))')" 0
req $CLI GET "/diagnosticos?q=a" "$DOC"; chk "termino corto" 400 codigo=TERMINO_INVALIDO
req $CLI GET "/diagnosticos?q=gripe" "$ADMIN"; chk "solo profesionales" 403
req $CLI GET "/diagnosticos?q=gripe" ""; chk "sin token" 401
igual "el termino buscado no queda en el log de clinical" "$(grep -c -i 'diabetes' "$LOGK")" 0

echo; echo "RESULTADO: $PASS correctas, $FAIL fallidas"; [ $FAIL = 0 ]
