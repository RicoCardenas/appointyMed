#!/usr/bin/env bash
# Levanta los 5 servicios, descarga docs/openapi/<servicio>.json (/v3/api-docs) y regenera la coleccion de Postman.
# Requisitos: mvn -DskipTests package, .env con las claves de BD y los puertos 8081 a 8085 libres. No crea datos (anula ADMIN_EMAIL para que auth no siembre el administrador).
set -u
DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$DIR/../.."
S=/tmp/appointy-e2e; mkdir -p $S docs/openapi
set -a; source .env; set +a
export JWT_SECRET="$(openssl rand -hex 32)"
export INTERNAL_API_KEY="$(openssl rand -hex 16)"
export ADMIN_EMAIL= ADMIN_PASSWORD=   # no sembrar el administrador de .env en la base: este script solo lee /v3/api-docs
for p in 8081 8082 8083 8084 8085; do ss -ltn "sport = :$p" | grep -q LISTEN && { echo "ABORTO: el puerto $p esta ocupado"; exit 1; }; done
PIDS=""
for s in auth:8081 catalog:8082 appointment:8083 clinical-history:8084 notification:8085; do
  n=${s%%:*}; java -jar $n-service/target/$n-service-0.1.0-SNAPSHOT.jar > $S/doc-$n.log 2>&1 & PIDS="$PIDS $!"
done
trap 'kill $PIDS 2>/dev/null; wait 2>/dev/null' EXIT
for s in auth:8081 catalog:8082 appointment:8083 clinical-history:8084 notification:8085; do
  n=${s%%:*}; p=${s##*:}
  for i in $(seq 1 60); do curl -s -m 2 "localhost:$p/actuator/health" | grep -q '"UP"' && break; sleep 1; done
  curl -s -m 10 "localhost:$p/v3/api-docs" | python3 -I -c "import sys,json; d=json.load(sys.stdin); json.dump(d, open('docs/openapi/$n.json','w'), indent=2, ensure_ascii=False, sort_keys=True); print('$n:', len(d['paths']), 'rutas')" || echo "FALLO $n"
done
python3 -I scripts/postman/generar_coleccion.py
