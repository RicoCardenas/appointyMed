"""notification-service simulado para probar appointment (la Fase 5 trae el real).

  POST /internal/notificaciones {tipo, citaId, pacienteId, profesionalId, centroId, fecha, horaInicio, actorId} -> 202
Cada llamada se anota en el archivo de registro (una linea JSON): ruta, si la llave es correcta, id de correlacion y cuerpo.
"""
import json, os, sys
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

LLAVE = os.environ["INTERNAL_API_KEY"]
REGISTRO = sys.argv[1]
PUERTO = int(sys.argv[2])


class H(BaseHTTPRequestHandler):
    def log_message(self, *a):
        pass

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
        with open(REGISTRO, "a") as f:
            f.write(json.dumps({"ruta": self.path, "llave_ok": self.headers.get("X-Api-Key") == LLAVE,
                                "correlacion": self.headers.get("X-Correlation-Id"), "cuerpo": cuerpo}) + "\n")
        self.send_response(202)
        self.send_header("Content-Length", "0")
        self.end_headers()


ThreadingHTTPServer(("127.0.0.1", PUERTO), H).serve_forever()
