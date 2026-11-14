#!/usr/bin/env bash
# Demo local: levanta los 5 servicios con secretos temporales, siembra datos de ejemplo por la API y espera.
# Con Ctrl+C se detienen los servicios y se BORRAN todos los datos creados (la base queda como estaba).
# Luego, en otra terminal: cd frontend && corepack pnpm dev  (abre http://localhost:5173)
# Requisitos: mvn -DskipTests package, .env con las claves de BD, puertos 8081 a 8085 libres, base sin datos.
# Las cuentas de demo usan una clave fija y conocida (Demo12345): solo para uso local.
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR/../.."
S=/tmp/appointy-e2e; mkdir -p $S
set -a; source .env; set +a
export JWT_SECRET="$(openssl rand -hex 32)"
export INTERNAL_API_KEY="$(openssl rand -hex 16)"
export ADMIN_EMAIL="admin@demo.local"; export ADMIN_PASSWORD="Demo12345"
CLAVE="Demo12345"
SQL() { PGPASSWORD="$2" psql -h localhost -U "$1" -d appointy -Atqc "$3"; }
for p in 8081 8082 8083 8084 8085; do ss -ltn "sport = :$p" | grep -q LISTEN && { echo "ABORTO: el puerto $p esta ocupado"; exit 1; }; done
[ "$(SQL auth_svc "$AUTH_DB_PASSWORD" 'select count(*) from usuario')" = "0" ] || { echo "ABORTO: la base de auth ya tiene cuentas; la demo borra lo que crea y no quiere tocar datos reales"; exit 1; }
[ "$(SQL catalog_svc "$CATALOG_DB_PASSWORD" 'select count(*) from profesional')" = "0" ] || { echo "ABORTO: catalog ya tiene datos"; exit 1; }

PIDS=""
for n in auth catalog appointment clinical-history notification; do java -jar $n-service/target/$n-service-0.1.0-SNAPSHOT.jar > $S/demo-$n.log 2>&1 & PIDS="$PIDS $!"; done
cleanup() {
  echo; echo "Deteniendo servicios y borrando los datos de la demo..."
  kill $PIDS 2>/dev/null; wait 2>/dev/null
  SQL clinical_svc "$CLINICAL_DB_PASSWORD" "alter table entrada_clinica disable trigger tr_entrada_inmutable; alter table log_acceso_clinico disable trigger tr_log_inmutable; delete from entrada_clinica; delete from log_acceso_clinico; alter table entrada_clinica enable trigger tr_entrada_inmutable; alter table log_acceso_clinico enable trigger tr_log_inmutable;" >/dev/null 2>&1
  SQL notification_svc "$NOTIFICATION_DB_PASSWORD" "delete from notificacion;" >/dev/null 2>&1
  SQL appointment_svc "$APPOINTMENT_DB_PASSWORD" "delete from historial_cita; delete from cita; delete from franja;" >/dev/null 2>&1
  SQL catalog_svc "$CATALOG_DB_PASSWORD" "delete from bloqueo; delete from horario_atencion; delete from profesional_centro; delete from profesional_especialidad; delete from profesional; delete from centro; delete from especialidad; update parametro_servicio set valor='2' where clave='anticipacion_minima_horas'; update parametro_servicio set valor='24' where clave='limite_cancelacion_horas'; update parametro_servicio set valor='60' where clave='horizonte_max_dias'; update parametro_servicio set valor='30' where clave='duracion_franja_min';" >/dev/null 2>&1
  SQL auth_svc "$AUTH_DB_PASSWORD" "delete from paciente_perfil; delete from token_revocado; delete from usuario;" >/dev/null 2>&1
  echo "Limpieza: usuarios=$(SQL auth_svc "$AUTH_DB_PASSWORD" 'select count(*) from usuario') profesionales=$(SQL catalog_svc "$CATALOG_DB_PASSWORD" 'select count(*) from profesional') citas=$(SQL appointment_svc "$APPOINTMENT_DB_PASSWORD" 'select count(*) from cita') entradas=$(SQL clinical_svc "$CLINICAL_DB_PASSWORD" 'select count(*) from entrada_clinica')"
}
trap cleanup EXIT INT TERM

for p in 8081 8082 8083 8084 8085; do
  for i in $(seq 1 60); do curl -s -m 2 "localhost:$p/actuator/health" | grep -q '"UP"' && break; sleep 1; done
done
for i in $(seq 1 20); do [ "$(SQL auth_svc "$AUTH_DB_PASSWORD" "select count(*) from usuario where correo='$ADMIN_EMAIL'")" = "1" ] && break; sleep 0.5; done

# api METODO URL TOKEN [CUERPO]  ->  imprime el cuerpo JSON
api() { local a=(-s -m 30 -X "$1" "$2" -H 'Content-Type: application/json'); [ -n "${3:-}" ] && a+=(-H "Authorization: Bearer $3"); [ -n "${4:-}" ] && a+=(-d "$4"); curl "${a[@]}"; }
campo() { python3 -I -c "import sys,json; print(json.load(sys.stdin)$1)"; }
A=http://localhost:8081/api/v1; C=http://localhost:8082/api/v1

ADMIN=$(api POST $A/auth/login "" "{\"correo\":\"$ADMIN_EMAIL\",\"clave\":\"$ADMIN_PASSWORD\"}" | campo "['accessToken']") || { echo "No se pudo iniciar sesion como admin"; exit 1; }
echo "Sembrando datos de ejemplo..."
nueva_cuenta() { api POST $A/admin/usuarios "$ADMIN" "{\"correo\":\"$1\",\"clave\":\"$CLAVE\",\"rol\":\"PROFESIONAL\"}" | campo "['usuarioId']"; }
U1=$(nueva_cuenta dra.rojas@demo.local); U2=$(nueva_cuenta dr.mejia@demo.local)
api POST $A/auth/register "" "{\"correo\":\"paciente@demo.local\",\"clave\":\"$CLAVE\",\"cedula\":\"DEMO-0001\",\"nombres\":\"Laura\",\"apellidos\":\"Gomez\",\"fechaNacimiento\":\"1991-04-12\",\"telefono\":\"3001234567\",\"ciudad\":\"Bogota\"}" >/dev/null
E1=$(api POST $C/especialidades "$ADMIN" '{"nombre":"Medicina general","descripcion":"Consulta general y control"}' | campo "['id']")
E2=$(api POST $C/especialidades "$ADMIN" '{"nombre":"Cardiologia","descripcion":"Corazon y circulacion"}' | campo "['id']")
E3=$(api POST $C/especialidades "$ADMIN" '{"nombre":"Pediatria","descripcion":"Salud infantil"}' | campo "['id']")
N=$(api POST $C/centros "$ADMIN" '{"nombre":"Sede Norte","direccion":"Calle 127 # 15-30","ciudad":"Bogota","telefono":"6015550101"}' | campo "['id']")
M=$(api POST $C/centros "$ADMIN" '{"nombre":"Sede Centro","direccion":"Carrera 7 # 32-16","ciudad":"Bogota","telefono":"6015550102"}' | campo "['id']")
P1=$(api POST $C/profesionales "$ADMIN" "{\"usuarioId\":\"$U1\",\"nombres\":\"Camila\",\"apellidos\":\"Rojas\",\"registroMedico\":\"RM-1001\"}" | campo "['id']")
P2=$(api POST $C/profesionales "$ADMIN" "{\"usuarioId\":\"$U2\",\"nombres\":\"Andres\",\"apellidos\":\"Mejia\",\"registroMedico\":\"RM-2002\"}" | campo "['id']")
api PUT $C/profesionales/$P1/especialidades "$ADMIN" "{\"ids\":[\"$E1\",\"$E2\"]}" >/dev/null
api PUT $C/profesionales/$P1/centros "$ADMIN" "{\"ids\":[\"$N\",\"$M\"]}" >/dev/null
api PUT $C/profesionales/$P2/especialidades "$ADMIN" "{\"ids\":[\"$E3\"]}" >/dev/null
api PUT $C/profesionales/$P2/centros "$ADMIN" "{\"ids\":[\"$N\"]}" >/dev/null
horario() { api POST $C/profesionales/$1/horarios "$ADMIN" "{\"centroId\":\"$2\",\"diaSemana\":$3,\"horaInicio\":\"$4\",\"horaFin\":\"$5\",\"duracionFranjaMin\":30}" >/dev/null; }
for d in 1 2 3 4 5; do horario $P1 $N $d 08:00 12:00; done
horario $P1 $M 1 14:00 17:00; horario $P1 $M 3 14:00 17:00
for d in 1 3 5; do horario $P2 $N $d 09:00 13:00; done

cat <<TXT

============================================================
 Demo lista. Servicios en 8081 a 8085 (datos temporales).
 Front:  cd frontend && corepack pnpm dev   ->  http://localhost:5173
 Cuentas (clave $CLAVE):
   Paciente        paciente@demo.local
   Profesional     dra.rojas@demo.local   (Medicina general, Cardiologia)
   Profesional     dr.mejia@demo.local    (Pediatria)
   Administrativo  admin@demo.local
 Ctrl+C para detener todo y borrar los datos.
============================================================
TXT
wait
