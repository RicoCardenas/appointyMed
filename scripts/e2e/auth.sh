#!/usr/bin/env bash
# Prueba de punta a punta de auth-service contra PostgreSQL local.
# Requisitos: mvn -DskipTests package, .env con las claves de BD, puerto 8081 libre. Borra los datos que crea.
# Secretos y claves de prueba se generan aqui, no se imprimen y no se guardan.
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR/../.."
set -a; source .env; set +a
export JWT_SECRET="$(openssl rand -hex 32)"
export INTERNAL_API_KEY="$(openssl rand -hex 16)"
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
req GET /admin/usuarios "$TOKEN";            chk "paciente no lista cuentas (GET all es solo del administrativo)" 403
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

igual() { if [ "$2" = "$3" ]; then PASS=$((PASS+1)); printf 'PASS %-62s %s\n' "$1" "$2"; else FAIL=$((FAIL+1)); printf 'FAIL %-62s esperado=%s real=%s\n' "$1" "$3" "$2"; fi; }

echo "== RA-03 y RA-04: administracion de cuentas (GET all paginado, estadisticas, activar, desactivar, cambiar clave, contacto interno)"
req GET "/admin/usuarios?size=2" "$ADMIN"; chk "GET all paginado" 200 pagina=0 tamano=2
req GET "/admin/usuarios?rol=PACIENTE&activo=true" "$ADMIN"; chk "filtro por rol y estado" 200 contenido.0.rol=PACIENTE
req GET "/admin/usuarios?size=0" "$ADMIN"; chk "tamano invalido" 400 codigo=TAMANO_INVALIDO
req GET "/admin/usuarios?page=-1" "$ADMIN"; chk "pagina negativa" 400 codigo=PAGINA_INVALIDA
req GET "/admin/usuarios?rol=RARO" "$ADMIN"; chk "rol desconocido" 400
req POST /auth/login "" "{\"correo\":\"e2e-nueva@test.local\",\"clave\":\"$PACIENTE_CLAVE\"}"; PT=$(echo "$BODY" | json accessToken)
req GET /admin/usuarios/estadisticas "$ADMIN"; chk "estadisticas por rol y estado" 200 inactivos=0
req GET /admin/usuarios/estadisticas "$PT"; chk "paciente no ve estadisticas" 403
req POST /auth/login "" "{\"correo\":\"$ADMIN_EMAIL\",\"clave\":\"$ADMIN_PASSWORD\"}"; ADMIN_ID=$(echo "$BODY" | json usuarioId)
req POST "/admin/usuarios/$ADMIN_ID/desactivar" "$ADMIN"; chk "un administrativo no se desactiva a si mismo" 409 codigo=NO_AUTODESACTIVAR
req POST "/admin/usuarios/$PACIENTE_ID/desactivar" "$ADMIN"; chk "desactivar una cuenta" 200 activo=False
req POST "/admin/usuarios/$PACIENTE_ID/desactivar" "$ADMIN"; chk "repetirlo es idempotente" 200 activo=False
req POST /auth/login "" "{\"correo\":\"e2e-nueva@test.local\",\"clave\":\"$PACIENTE_CLAVE\"}"; chk "una cuenta inactiva no inicia sesion" 401 codigo=CREDENCIALES_INVALIDAS
req POST "/admin/usuarios/$PACIENTE_ID/activar" "$ADMIN"; chk "reactivar la cuenta" 200 activo=True
req POST "/admin/usuarios/$(cat /proc/sys/kernel/random/uuid)/activar" "$ADMIN"; chk "cuenta inexistente" 404 codigo=USUARIO_NO_ENCONTRADO
req POST "/admin/usuarios/$PACIENTE_ID/activar" "$PT"; chk "paciente no activa cuentas" 403
req POST /auth/login "" "{\"correo\":\"e2e-nueva@test.local\",\"clave\":\"$PACIENTE_CLAVE\"}"; chk "tras reactivar vuelve a entrar" 200
NT=$(echo "$BODY" | json accessToken)
req POST /auth/cambiar-clave "$NT" "{\"claveActual\":\"Equivocada1\",\"claveNueva\":\"Otra12345\"}"; chk "clave actual incorrecta" 400 codigo=CLAVE_ACTUAL_INCORRECTA
req POST /auth/cambiar-clave "$NT" "{\"claveActual\":\"$PACIENTE_CLAVE\",\"claveNueva\":\"corta\"}"; chk "clave nueva debil" 400 codigo=CLAVE_DEBIL
req POST /auth/cambiar-clave "$NT" "{\"claveActual\":\"$PACIENTE_CLAVE\",\"claveNueva\":\"$PACIENTE_CLAVE\"}"; chk "clave nueva igual a la actual" 400 codigo=CLAVE_IGUAL
req POST /auth/cambiar-clave "" "{\"claveActual\":\"x\",\"claveNueva\":\"y\"}"; chk "cambiar clave sin token" 401
req POST /auth/cambiar-clave "$NT" "{\"claveActual\":\"$PACIENTE_CLAVE\",\"claveNueva\":\"Nueva$PACIENTE_CLAVE\"}"; chk "cambio de clave correcto" 204
req POST /auth/login "" "{\"correo\":\"e2e-nueva@test.local\",\"clave\":\"$PACIENTE_CLAVE\"}"; chk "la clave anterior ya no sirve" 401
req POST /auth/login "" "{\"correo\":\"e2e-nueva@test.local\",\"clave\":\"Nueva$PACIENTE_CLAVE\"}"; chk "la clave nueva si" 200
INT=http://localhost:8081/internal
CODE=$(curl -s -m 5 -o /tmp/e2e-auth-int -w '%{http_code}' "$INT/usuarios/$PACIENTE_ID/contacto" -H "X-Api-Key: $INTERNAL_API_KEY"); igual "contacto interno con llave de servicio" "$CODE" 200
igual "devuelve solo el correo" "$(python3 -I -c "import json; print(sorted(json.load(open('/tmp/e2e-auth-int')).keys()))")" "['correo']"
igual "sin llave" "$(curl -s -m 5 -o /dev/null -w '%{http_code}' "$INT/usuarios/$PACIENTE_ID/contacto")" 401
igual "un JWT de usuario no abre /internal" "$(curl -s -m 5 -o /dev/null -w '%{http_code}' "$INT/usuarios/$PACIENTE_ID/contacto" -H "Authorization: Bearer $ADMIN")" 401
igual "la llave no abre la API de usuarios" "$(curl -s -m 5 -o /dev/null -w '%{http_code}' "$BASE/admin/usuarios" -H "X-Api-Key: $INTERNAL_API_KEY")" 401
igual "usuario inexistente en /internal" "$(curl -s -m 5 -o /dev/null -w '%{http_code}' "$INT/usuarios/$(cat /proc/sys/kernel/random/uuid)/contacto" -H "X-Api-Key: $INTERNAL_API_KEY")" 404

echo "== Nombres de pacientes para profesionales y administrativos"
req POST /pacientes/nombres "$ADMIN" "{\"ids\":[\"$PACIENTE_ID\"]}"; chk "el administrativo obtiene el nombre" 200 0.nombres=Ana 0.apellidos=Perez
igual "solo nombre y apellido (sin cedula ni contacto)" "$(echo "$BODY" | python3 -I -c 'import sys,json; print(sorted(json.load(sys.stdin)[0].keys()))')" "['apellidos', 'nombres', 'usuarioId']"
req POST /pacientes/nombres "$ADMIN" "{\"ids\":[\"$(cat /proc/sys/kernel/random/uuid)\"]}"; chk "un id sin perfil simplemente no aparece" 200
req POST /pacientes/nombres "$ADMIN" '{"ids":[]}'; chk "lote vacio" 400
req POST /pacientes/nombres "$PT" "{\"ids\":[\"$PACIENTE_ID\"]}"; chk "un paciente no consulta nombres de otros" 403
req POST /pacientes/nombres "" "{\"ids\":[\"$PACIENTE_ID\"]}"; chk "sin token" 401

echo "== OpenAPI (RNF-11)"
igual "la especificacion es publica" "$(curl -s -m 10 -o /tmp/e2e-auth-api -w '%{http_code}' http://localhost:8081/v3/api-docs)" 200
igual "documenta /api/v1 y no /internal" "$(python3 -I -c "import json; p=json.load(open('/tmp/e2e-auth-api'))['paths']; print(any(k.startswith('/api/v1') for k in p) and not any(k.startswith('/internal') for k in p))")" True
igual "declara el esquema bearer JWT" "$(python3 -I -c "import json; print(json.load(open('/tmp/e2e-auth-api'))['components']['securitySchemes']['bearer']['bearerFormat'])")" JWT
igual "swagger-ui responde" "$(curl -s -m 10 -o /dev/null -w '%{http_code}' -L http://localhost:8081/swagger-ui/index.html)" 200
igual "publicar la especificacion no abre la API" "$(curl -s -m 5 -o /dev/null -w '%{http_code}' http://localhost:8081/api/v1/admin/usuarios)" 401

echo "== Estado en BD (RNF-14, RNF-05)"
[ "$(SQL "select count(*) from usuario where password_hash like '\$2%'")" = "$(SQL 'select count(*) from usuario')" ] && { PASS=$((PASS+1)); echo "PASS todas las claves son hashes BCrypt"; } || { FAIL=$((FAIL+1)); echo "FAIL hay claves sin BCrypt"; }
[ "$(SQL "select count(*) from usuario where correo<>lower(correo)")" = "0" ] && { PASS=$((PASS+1)); echo "PASS todos los correos en minusculas"; } || { FAIL=$((FAIL+1)); echo "FAIL correos con mayusculas"; }
echo "   tokens revocados: $(SQL 'select count(*) from token_revocado')  usuarios: $(SQL 'select count(*) from usuario')  perfiles: $(SQL 'select count(*) from paciente_perfil')"
echo "   la clave en claro no aparece en el log: $(grep -c "$PACIENTE_CLAVE\|$ADMIN_PASSWORD" "$LOG") coincidencias; el token JWT: $(grep -c "$TOKEN" "$LOG") coincidencias"
echo "   ERROR en log: $(grep -c ' ERROR ' "$LOG")"

echo; echo "RESULTADO: $PASS correctas, $FAIL fallidas"
