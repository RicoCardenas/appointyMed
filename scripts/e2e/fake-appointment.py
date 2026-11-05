"""appointment-service simulado para probar catalog. Contrato de /internal que appointment debera cumplir en la Fase 3.

  POST /internal/franjas/generar  {profesionalId, centroId, franjas:[{fecha, horaInicio, horaFin}]} -> 200 {creadas, existentes}  (idempotente)
  POST /internal/franjas/bloquear {profesionalId, centroId, fechaInicio, fechaFin}                 -> 200 {bloqueadas}
       fechaInicio == 2030-01-01 -> 409 {citasAfectadas:[...]}   fechaInicio == 2031-01-01 -> responde tras 8 s (timeout)
"""
import json, os, sys, time
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

LLAVE = os.environ["INTERNAL_API_KEY"]
REGISTRO = sys.argv[1]
PUERTO = int(sys.argv[2])
VISTAS = set()


class H(BaseHTTPRequestHandler):
    def log_message(self, *a):
        pass

    def _responder(self, estado, cuerpo):
        datos = json.dumps(cuerpo).encode()
        self.send_response(estado)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(datos)))
        self.end_headers()
        self.wfile.write(datos)

    def _leer_cuerpo(self):
        if self.headers.get("Transfer-Encoding", "").lower() == "chunked":  # el cliente de Java envia asi si no conoce el largo
            datos = b""
            while True:
                tam = int(self.rfile.readline().strip().split(b";")[0], 16)
                if tam == 0:
                    self.rfile.readline()
                    return datos
                datos += self.rfile.read(tam)
                self.rfile.readline()
        return self.rfile.read(int(self.headers.get("Content-Length", 0)))

    def do_POST(self):
        cuerpo = json.loads(self._leer_cuerpo() or b"{}")
        llave_ok = self.headers.get("X-Api-Key") == LLAVE
        with open(REGISTRO, "a") as f:
            f.write(json.dumps({
                "ruta": self.path, "llave_ok": llave_ok, "correlacion": self.headers.get("X-Correlation-Id"),
                "profesionalId": cuerpo.get("profesionalId"), "centroId": cuerpo.get("centroId"),
                "n_franjas": len(cuerpo.get("franjas", [])),
                "primera": (cuerpo.get("franjas") or [{}])[0].get("fecha"),
                "ultima": (cuerpo.get("franjas") or [{}])[-1].get("fecha"),
                "fechaInicio": cuerpo.get("fechaInicio"),
            }) + "\n")
        if not llave_ok:
            return self._responder(401, {"error": "llave"})
        if self.path == "/internal/franjas/generar":
            creadas = existentes = 0
            for fr in cuerpo["franjas"]:
                clave = (cuerpo["profesionalId"], cuerpo["centroId"], fr["fecha"], fr["horaInicio"])
                if clave in VISTAS:
                    existentes += 1
                else:
                    VISTAS.add(clave)
                    creadas += 1
            return self._responder(200, {"creadas": creadas, "existentes": existentes})
        if self.path == "/internal/franjas/bloquear":
            if cuerpo["fechaInicio"] == "2030-01-01":
                return self._responder(409, {"citasAfectadas": ["cita-1", "cita-2"]})
            if cuerpo["fechaInicio"] == "2031-01-01":
                time.sleep(8)
            return self._responder(200, {"bloqueadas": 1})
        self._responder(404, {"error": "ruta"})


ThreadingHTTPServer(("127.0.0.1", PUERTO), H).serve_forever()
