#!/usr/bin/env bash
# Prueba de punta a punta de auth-service contra PostgreSQL local.
# Requisitos: mvn -DskipTests package, .env con las claves de BD, puerto 8081 libre. Borra los datos que crea.
# Secretos y claves de prueba se generan aqui, no se imprimen y no se guardan.
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR/../.."
set -a; source .env; set +a
export JWT_SECRET="$(openssl rand -hex 32)"
export ADMIN_EMAIL="e2e-admin@test.local"
export ADMIN_PASSWORD="Aa1$(openssl rand -hex 8)"
PACIENTE_CLAVE="Aa1$(openssl rand -hex 6)"
BASE=http://localhost:8081/api/v1
mkdir -p /tmp/appointy-e2e
LOG=/tmp/appointy-e2e/auth.log
SQL() { PGPASSWORD="$AUTH_DB_PASSWORD" psql -h localhost -U auth_svc -d appointy -Atqc "$1"; }
PASS=0; FAIL=0

java -jar auth-service/target/auth-service-0.1.0-SNAPSHOT.jar > "$LOG" 2>&1 &
PID=$!
cleanup() {
  kill $PID 2>/dev/null; wait $PID 2>/dev/null
  SQL "delete from paciente_perfil; delete from token_revocado; delete from usuario;" >/dev/null 2>&1
  echo "limpieza: filas restantes en usuario = $(SQL 'select count(*) from usuario' 2>&1)"
}
trap cleanup EXIT

for i in $(seq 1 40); do curl -s -m 2 localhost:8081/actuator/health | grep -q '"UP"' && break; sleep 1; done
curl -s -m 2 localhost:8081/actuator/health | grep -q '"UP"' || { echo "El servicio no arranco"; tail -20 "$LOG"; exit 1; }

# Seguridad de la prueba: solo hay datos si es el administrador temporal recien sembrado.
[ "$(SQL "select count(*) from usuario")" = "1" ] && [ "$(SQL "select count(*) from usuario where correo='$ADMIN_EMAIL'")" = "1" ] \
  || { echo "ABORTO: la tabla usuario ya tenia datos; no se toca"; trap - EXIT; kill $PID; exit 1; }

json() { python3 -I -c "import sys,json
try:
    d=json.load(sys.stdin)
    v=d
    for k in '$1'.split('.'):
        v=v[k] if isinstance(v,dict) else v[int(k)]
    print(v)
except Exception:
    print('')"; }

# req METODO RUTA TOKEN CUERPO  -> deja HTTP_CODE y BODY
req() {
  local m=$1 p=$2 t=${3:-} b=${4:-}
  local args=(-s -m 10 -o /tmp/e2e-body -w '%{http_code}' -X "$m" "$BASE$p" -H 'Content-Type: application/json')
  [ -n "$t" ] && args+=(-H "Authorization: Bearer $t")
  [ -n "$b" ] && args+=(-d "$b")
  HTTP_CODE=$(curl "${args[@]}"); BODY=$(cat /tmp/e2e-body)
}
chk() {  # chk NOMBRE ESPERADO [campo=valor]
  local ok=1
  [ "$HTTP_CODE" = "$2" ] || ok=0
  if [ -n "${3:-}" ]; then [ "$(echo "$BODY" | json "${3%%=*}")" = "${3#*=}" ] || ok=0; fi
  if [ $ok = 1 ]; then PASS=$((PASS+1)); printf 'PASS %-52s %s\n' "$1" "$HTTP_CODE"
  else FAIL=$((FAIL+1)); printf 'FAIL %-52s esperado=%s real=%s %s\n' "$1" "$2" "$HTTP_CODE" "$(echo "$BODY" | head -c 220)"; fi
}

REG='{"correo":"%s","clave":"%s","cedula":"%s","nombres":"Ana","apellidos":"Perez","fechaNacimiento":"1990-05-01","telefono":"3001234567","direccion":"Calle 1","ciudad":"Cali"}'

echo "== CU-01 registrar paciente"
req POST /auth/register "" "$(printf "$REG" 'E2E-Ana@Test.Local' "$PACIENTE_CLAVE" E2E0001)"; chk "registro correcto" 201 correo=e2e-ana@test.local
PACIENTE_ID=$(echo "$BODY" | json usuarioId)
req POST /auth/register "" "$(printf "$REG" 'e2e-ana@test.local' "$PACIENTE_CLAVE" E2E0002)"; chk "correo duplicado" 409 codigo=CORREO_YA_REGISTRADO
req POST /auth/register "" "$(printf "$REG" 'e2e-otra@test.local' "$PACIENTE_CLAVE" E2E0001)"; chk "cedula duplicada" 409 codigo=CEDULA_YA_REGISTRADA
req POST /auth/register "" "$(printf "$REG" 'e2e-debil@test.local' 'corta' E2E0003)"; chk "clave debil" 400
req POST /auth/register "" '{"correo":"x@y.com"}'; chk "faltan campos obligatorios" 400 codigo=VALIDACION_ENTRADA
req POST /auth/register "" '{no es json'; chk "JSON ilegible" 400 codigo=SOLICITUD_ILEGIBLE
req POST /auth/register "" "$(printf "$REG" 'e2e-seg@test.local' "$PACIENTE_CLAVE" E2E0004)"; chk "segundo paciente" 201
OTRO_ID=$(echo "$BODY" | json usuarioId)

echo "== CU-02 iniciar sesion"
req POST /auth/login "" '{"correo":"e2e-ana@test.local","clave":"incorrecta1"}'; chk "clave incorrecta" 401 codigo=CREDENCIALES_INVALIDAS
MSG1=$(echo "$BODY" | json mensaje)
req POST /auth/login "" '{"correo":"no-existe@test.local","clave":"incorrecta1"}'; chk "correo inexistente (mismo mensaje)" 401 codigo=CREDENCIALES_INVALIDAS
[ "$(echo "$BODY" | json mensaje)" = "$MSG1" ] && { PASS=$((PASS+1)); echo "PASS no se distingue cuenta inexistente de clave mala"; } || { FAIL=$((FAIL+1)); echo "FAIL mensajes distintos"; }
req POST /auth/login "" "{\"correo\":\"  E2E-ANA@test.local \",\"clave\":\"$PACIENTE_CLAVE\"}"; chk "login correcto (correo normalizado)" 200 tokenType=Bearer
TOKEN=$(echo "$BODY" | json accessToken)
echo "   rol=$(echo "$BODY" | json rol) token_longitud=${#TOKEN}"

echo "== Proteccion de rutas (RF-02)"
req GET /pacientes/me;                       chk "sin token" 401 codigo=NO_AUTENTICADO
req GET /pacientes/me "token.basura.invalido"; chk "token invalido" 401 codigo=NO_AUTENTICADO
req GET /admin/usuarios "$TOKEN";            chk "paciente no entra a admin (GET sin ruta)" 405
req POST /admin/usuarios "$TOKEN" '{"correo":"a@b.com","clave":"Clave1234","rol":"PROFESIONAL"}'; chk "paciente no crea cuentas" 403 codigo=ACCESO_DENEGADO

echo "== CU-04 consultar perfil"
req GET /pacientes/me "$TOKEN"; chk "perfil propio" 200 cedula=E2E0001
[ "$(echo "$BODY" | json usuarioId)" = "$PACIENTE_ID" ] && { PASS=$((PASS+1)); echo "PASS el id sale del token"; } || { FAIL=$((FAIL+1)); echo "FAIL id distinto"; }

echo "== CU-05 actualizar datos editables / RF-06 inmutables"
req PATCH /pacientes/me "$TOKEN" '{"telefono":"3119998877","direccion":"Carrera 7 # 8-9","correo":"E2E-Nueva@Test.Local"}'; chk "actualiza telefono, direccion y correo" 200 correo=e2e-nueva@test.local
req PATCH /pacientes/me "$TOKEN" '{"cedula":"HACK0001"}';           chk "paciente no puede cambiar la cedula" 400 codigo=CAMPO_NO_PERMITIDO
req PATCH /pacientes/me "$TOKEN" '{"nombres":"Otro"}';              chk "paciente no puede cambiar nombres" 400 codigo=CAMPO_NO_PERMITIDO
req PATCH /pacientes/me "$TOKEN" '{"fechaNacimiento":"1980-01-01"}'; chk "paciente no puede cambiar nacimiento" 400 codigo=CAMPO_NO_PERMITIDO
req PATCH /pacientes/me "$TOKEN" '{}';                              chk "sin datos" 400 codigo=SIN_CAMBIOS
req PATCH /pacientes/me "$TOKEN" '{"correo":"e2e-seg@test.local"}'; chk "correo de otra cuenta" 409 codigo=CORREO_YA_REGISTRADO
req PATCH /pacientes/me "$TOKEN" '{"telefono":"abc"}';              chk "telefono invalido" 400
req GET /pacientes/me "$TOKEN"; chk "cedula intacta tras los intentos" 200 cedula=E2E0001

echo "== CU-06 crear cuenta de profesional / administrativo"
req POST /auth/login "" "{\"correo\":\"$ADMIN_EMAIL\",\"clave\":\"$ADMIN_PASSWORD\"}"; chk "login del administrador sembrado" 200 rol=ADMINISTRATIVO
ADMIN=$(echo "$BODY" | json accessToken)
req POST /admin/usuarios "$ADMIN" "{\"correo\":\"e2e-doc@test.local\",\"clave\":\"$PACIENTE_CLAVE\",\"rol\":\"PROFESIONAL\"}"; chk "crea profesional" 201 rol=PROFESIONAL
req POST /admin/usuarios "$ADMIN" "{\"correo\":\"e2e-adm2@test.local\",\"clave\":\"$PACIENTE_CLAVE\",\"rol\":\"ADMINISTRATIVO\"}"; chk "crea administrativo" 201 rol=ADMINISTRATIVO
req POST /admin/usuarios "$ADMIN" "{\"correo\":\"e2e-pac@test.local\",\"clave\":\"$PACIENTE_CLAVE\",\"rol\":\"PACIENTE\"}"; chk "no crea pacientes por aqui" 400 codigo=ROL_NO_PERMITIDO
req POST /admin/usuarios "$ADMIN" "{\"correo\":\"e2e-doc@test.local\",\"clave\":\"$PACIENTE_CLAVE\",\"rol\":\"PROFESIONAL\"}"; chk "correo duplicado" 409
req POST /admin/usuarios "$ADMIN" "{\"correo\":\"e2e-x@test.local\",\"clave\":\"$PACIENTE_CLAVE\",\"rol\":\"SUPERMAN\"}"; chk "rol inexistente" 400
req POST /auth/login "" "{\"correo\":\"e2e-doc@test.local\",\"clave\":\"$PACIENTE_CLAVE\"}"; chk "el profesional inicia sesion" 200 rol=PROFESIONAL
DOC=$(echo "$BODY" | json accessToken)
req GET /pacientes/me "$DOC"; chk "profesional no tiene perfil de paciente (403 por rol)" 403

echo "== CU-07 corregir datos inmutables (administrativo)"
req PATCH "/admin/pacientes/$PACIENTE_ID/identidad" "$ADMIN" '{"nombres":"Ana Maria","fechaNacimiento":"1991-06-02"}'; chk "corrige nombres y nacimiento" 200 nombres="Ana Maria"
req PATCH "/admin/pacientes/$PACIENTE_ID/identidad" "$ADMIN" '{"cedula":"E2E0001"}'; chk "misma cedula, sin error" 200
req PATCH "/admin/pacientes/$PACIENTE_ID/identidad" "$ADMIN" '{"cedula":"E2E0004"}'; chk "cedula de otro paciente" 409 codigo=CEDULA_YA_REGISTRADA
req PATCH "/admin/pacientes/$PACIENTE_ID/identidad" "$ADMIN" '{"cedula":"E2E0099"}'; chk "corrige la cedula" 200 cedula=E2E0099
req PATCH "/admin/pacientes/$(cat /proc/sys/kernel/random/uuid)/identidad" "$ADMIN" '{"nombres":"X"}'; chk "paciente inexistente" 404 codigo=USUARIO_NO_ENCONTRADO
req PATCH "/admin/pacientes/$PACIENTE_ID/identidad" "$ADMIN" '{}'; chk "sin datos" 400
req PATCH "/admin/pacientes/$PACIENTE_ID/identidad" "$TOKEN" '{"nombres":"X"}'; chk "el paciente no puede usar la ruta admin" 403
req PATCH "/admin/pacientes/$PACIENTE_ID/identidad" "$DOC" '{"nombres":"X"}'; chk "el profesional tampoco" 403

echo "== CU-03 cerrar sesion"
req POST /auth/logout "$TOKEN"; chk "logout" 204
req GET /pacientes/me "$TOKEN"; chk "el token revocado ya no sirve" 401
req GET /pacientes/me "$ADMIN"; chk "otros tokens siguen vigentes (admin sin perfil: 403)" 403
req POST /auth/login "" "{\"correo\":\"e2e-nueva@test.local\",\"clave\":\"$PACIENTE_CLAVE\"}"; chk "nuevo login con el correo actualizado" 200
req POST /auth/logout; chk "logout sin token" 401

echo "== Errores uniformes y correlacion (RNF-10, RNF-17)"
req GET /ruta-que-no-existe "$ADMIN"; chk "ruta inexistente" 404
echo "   cuerpo: $(echo "$BODY" | python3 -I -c 'import sys,json; d=json.load(sys.stdin); print(sorted(d.keys()))')"
H=$(curl -s -m 5 -D - -o /dev/null -H 'X-Correlation-Id: abc12345-corr' "$BASE/pacientes/me" | tr -d '\r' | grep -i '^x-correlation-id' )
[ "$H" = "X-Correlation-Id: abc12345-corr" ] && { PASS=$((PASS+1)); echo "PASS se respeta un correlation id valido"; } || { FAIL=$((FAIL+1)); echo "FAIL correlation id: $H"; }
H2=$(curl -s -m 5 -D - -o /dev/null -H 'X-Correlation-Id: x;DROP' "$BASE/pacientes/me" | tr -d '\r' | grep -i '^x-correlation-id' | cut -d' ' -f2)
[ -n "$H2" ] && [ "$H2" != "x;DROP" ] && { PASS=$((PASS+1)); echo "PASS un correlation id inseguro se reemplaza"; } || { FAIL=$((FAIL+1)); echo "FAIL id inseguro: $H2"; }
grep -q "$(echo abc12345-corr)" "$LOG" && echo "   (el id aparece en los logs)" || echo "   (aviso: el id no aparece en los logs)"

echo "== Estado en BD (RNF-14, RNF-05)"
[ "$(SQL "select count(*) from usuario where password_hash like '\$2%'")" = "$(SQL 'select count(*) from usuario')" ] && { PASS=$((PASS+1)); echo "PASS todas las claves son hashes BCrypt"; } || { FAIL=$((FAIL+1)); echo "FAIL hay claves sin BCrypt"; }
[ "$(SQL "select count(*) from usuario where correo<>lower(correo)")" = "0" ] && { PASS=$((PASS+1)); echo "PASS todos los correos en minusculas"; } || { FAIL=$((FAIL+1)); echo "FAIL correos con mayusculas"; }
echo "   tokens revocados: $(SQL 'select count(*) from token_revocado')  usuarios: $(SQL 'select count(*) from usuario')  perfiles: $(SQL 'select count(*) from paciente_perfil')"
echo "   la clave en claro no aparece en el log: $(grep -c "$PACIENTE_CLAVE\|$ADMIN_PASSWORD" "$LOG") coincidencias; el token JWT: $(grep -c "$TOKEN" "$LOG") coincidencias"
echo "   ERROR en log: $(grep -c ' ERROR ' "$LOG")"

echo; echo "RESULTADO: $PASS correctas, $FAIL fallidas"
