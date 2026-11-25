#!/usr/bin/env python3
"""Pruebas de carga y de estres de las APIs de Appointy Med (solo libreria estandar de Python).
Siembra datos por la API, mide latencia (p50/p95/p99), rendimiento y errores por escenario y nivel de concurrencia,
y comprueba la invariante de reserva (una franja = una cita) bajo contencion. Resultado en JSON.

Uso: python3 -I scripts/carga/carga.py --clave-admin Demo12345 --salida resultados.json [--url-base http://localhost] [--duracion 15]
Apunta solo a un entorno propio y desechable (por defecto, el docker compose local con puertos 8081 a 8085)."""
import argparse, base64, http.client, json, os, platform, random, statistics, sys, threading, time, urllib.parse
from concurrent.futures import ThreadPoolExecutor

PUERTOS = {'auth': 8081, 'catalog': 8082, 'appointment': 8083}
LIMITE_MS = 500
TIEMPO_ESPERA_S = 15   # se cambia con --espera (con 0,1 CPU una peticion puede tardar mas)


class Cliente:
    """Una conexion keep-alive por hilo y servicio."""
    def __init__(self, base):
        self.base = urllib.parse.urlparse(base)
        self.local = threading.local()

    def _con(self, svc):
        cache = getattr(self.local, 'c', None)
        if cache is None:
            cache = self.local.c = {}
        if svc not in cache:
            cache[svc] = http.client.HTTPConnection(self.base.hostname, PUERTOS[svc], timeout=TIEMPO_ESPERA_S)
        return cache[svc]

    def pedir(self, svc, metodo, ruta, token=None, cuerpo=None):
        cab = {'Content-Type': 'application/json', 'X-Correlation-Id': 'carga-%d' % random.getrandbits(40)}
        if token:
            cab['Authorization'] = 'Bearer ' + token
        datos = json.dumps(cuerpo) if cuerpo is not None else None
        t0 = time.perf_counter()
        try:
            con = self._con(svc)
            con.request(metodo, ruta, body=datos, headers=cab)
            r = con.getresponse()
            texto = r.read()
            estado = r.status
        except Exception:
            self.local.c.pop(svc, None)
            return 0, (time.perf_counter() - t0) * 1000, b''
        return estado, (time.perf_counter() - t0) * 1000, texto


def pct(valores, p):
    if not valores:
        return 0.0
    s = sorted(valores)
    return s[min(len(s) - 1, int(round(p / 100 * (len(s) - 1))))]


def resumen(muestras, seg, ok_codigos):
    lat = [m[1] for m in muestras]
    errores = [m for m in muestras if m[0] not in ok_codigos]
    return {'peticiones': len(muestras), 'rps': round(len(muestras) / seg, 1), 'p50_ms': round(pct(lat, 50), 1), 'p95_ms': round(pct(lat, 95), 1),
            'p99_ms': round(pct(lat, 99), 1), 'max_ms': round(max(lat), 1) if lat else 0, 'media_ms': round(statistics.mean(lat), 1) if lat else 0,
            'errores_pct': round(100 * len(errores) / max(1, len(muestras)), 2), 'sobre_500ms_pct': round(100 * sum(1 for x in lat if x > LIMITE_MS) / max(1, len(lat)), 2),
            'estados': {str(k): sum(1 for m in muestras if m[0] == k) for k in sorted({m[0] for m in muestras})}}


def correr(accion, hilos, segundos):
    """Ejecuta `accion()` (devuelve (estado, ms)) en `hilos` hilos durante `segundos`."""
    muestras, fin = [], time.perf_counter() + segundos
    cerrojo = threading.Lock()

    def bucle():
        local = []
        while time.perf_counter() < fin:
            local.append(accion())
        with cerrojo:
            muestras.extend(local)
    t0 = time.perf_counter()
    hs = [threading.Thread(target=bucle) for _ in range(hilos)]
    [h.start() for h in hs]
    [h.join() for h in hs]
    return muestras, time.perf_counter() - t0


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--url-base', default='http://localhost')
    ap.add_argument('--correo-admin', default='admin@demo.local')
    ap.add_argument('--clave-admin', required=True)
    ap.add_argument('--duracion', type=int, default=15, help='segundos por nivel')
    ap.add_argument('--pacientes', type=int, default=100)
    ap.add_argument('--salida', default='resultados.json')
    ap.add_argument('--niveles', default='10,50,100,200,400,800', help='hilos de lectura (carga hasta 100, estres por encima)')
    ap.add_argument('--niveles-login', default='1,5,20,50')
    ap.add_argument('--escenarios', default='lectura,login,reservas,contencion')
    ap.add_argument('--hilos-reserva', type=int, default=20)
    ap.add_argument('--espera', type=int, default=15, help='segundos maximos por peticion')
    ap.add_argument('--hilos-siembra', type=int, default=10)
    a = ap.parse_args()
    global TIEMPO_ESPERA_S
    TIEMPO_ESPERA_S = a.espera
    cli = Cliente(a.url_base)
    sufijo = str(int(time.time()))
    salida = {'maquina': {'cpu': platform.processor() or platform.machine(), 'nucleos': os.cpu_count(), 'python': platform.python_version()},
              'duracion_nivel_s': a.duracion, 'escenarios': {}}

    def j(r):
        return json.loads(r[2]) if r[2] else {}

    # ---------------------------------------------------------------- siembra (no se mide)
    print('Sembrando datos...', flush=True)
    est, _, txt = cli.pedir('auth', 'POST', '/api/v1/auth/login', None, {'correo': a.correo_admin, 'clave': a.clave_admin})
    if est != 200:
        sys.exit('No se pudo iniciar sesion como administrador (HTTP %s)' % est)
    adm = json.loads(txt)['accessToken']
    esp = j(cli.pedir('catalog', 'POST', '/api/v1/especialidades', adm, {'nombre': 'Carga ' + sufijo, 'descripcion': 'carga'}))['id']
    cen = j(cli.pedir('catalog', 'POST', '/api/v1/centros', adm, {'nombre': 'Sede carga ' + sufijo, 'direccion': 'Calle 1', 'ciudad': 'Bogota'}))['id']
    pros = []
    for i in range(3):
        u = j(cli.pedir('auth', 'POST', '/api/v1/admin/usuarios', adm, {'correo': 'pro%d.%s@carga.local' % (i, sufijo), 'clave': 'Carga12345', 'rol': 'PROFESIONAL'}))['usuarioId']
        p = j(cli.pedir('catalog', 'POST', '/api/v1/profesionales', adm, {'usuarioId': u, 'nombres': 'Pro%d' % i, 'apellidos': 'Carga', 'registroMedico': 'RM%s%d' % (sufijo, i)}))['id']
        cli.pedir('catalog', 'PUT', '/api/v1/profesionales/%s/especialidades' % p, adm, {'ids': [esp]})
        cli.pedir('catalog', 'PUT', '/api/v1/profesionales/%s/centros' % p, adm, {'ids': [cen]})
        for dia in range(1, 8):
            cli.pedir('catalog', 'POST', '/api/v1/profesionales/%s/horarios' % p, adm, {'centroId': cen, 'diaSemana': dia, 'horaInicio': '08:00', 'horaFin': '12:00', 'duracionFranjaMin': 30})
        pros.append(p)

    def nuevo_paciente(k):
        correo = 'pac%d.%s@carga.local' % (k, sufijo)
        cli.pedir('auth', 'POST', '/api/v1/auth/register', None, {'correo': correo, 'clave': 'Carga12345', 'cedula': 'K%s%d' % (sufijo, k), 'nombres': 'Pac', 'apellidos': str(k), 'fechaNacimiento': '1990-01-01'})
        return j(cli.pedir('auth', 'POST', '/api/v1/auth/login', None, {'correo': correo, 'clave': 'Carga12345'})).get('accessToken')
    with ThreadPoolExecutor(a.hilos_siembra) as ex:
        tokens = [t for t in ex.map(nuevo_paciente, range(a.pacientes)) if t]
    print('  pacientes listos:', len(tokens), flush=True)

    # franjas libres por (profesional, fecha)
    hoy = time.gmtime()
    fechas = [time.strftime('%Y-%m-%d', time.gmtime(time.time() + 86400 * d)) for d in range(3, 40)]
    pool = []   # (fecha, profesional, franjaId)
    for f in fechas:
        for p in pros:
            fr = j(cli.pedir('appointment', 'GET', '/api/v1/disponibilidad?profesionalId=%s&centroId=%s&fecha=%s' % (p, cen, f), tokens[0]))
            if isinstance(fr, list):
                pool += [(f, p, x['id']) for x in fr]
    print('  franjas disponibles:', len(pool), flush=True)
    fecha_d = fechas[0]

    # ---------------------------------------------------------------- escenario 1: lectura (carga y estres)
    def lectura():
        t = random.choice(tokens)
        n = random.randrange(5)
        if n == 0:
            r = cli.pedir('catalog', 'GET', '/api/v1/especialidades', t)
        elif n == 1:
            r = cli.pedir('catalog', 'GET', '/api/v1/especialidades/%s/profesionales' % esp, t)
        elif n == 2:
            r = cli.pedir('catalog', 'GET', '/api/v1/profesionales/%s/centros' % random.choice(pros), t)
        elif n == 3:
            r = cli.pedir('appointment', 'GET', '/api/v1/disponibilidad?profesionalId=%s&centroId=%s&fecha=%s' % (random.choice(pros), cen, fecha_d), t)
        else:
            r = cli.pedir('appointment', 'GET', '/api/v1/citas/mias', t)
        return r[0], r[1]
    print('Calentamiento...', flush=True)
    correr(lectura, 10, 8)
    niveles = [(int(h), 'carga' if int(h) <= 100 else 'estres') for h in a.niveles.split(',')]
    esc_ok = set(a.escenarios.split(','))
    salida['escenarios']['lectura'] = []
    for h, tipo in (niveles if 'lectura' in esc_ok else []):
        m, seg = correr(lectura, h, a.duracion)
        r = resumen(m, seg, {200}); r.update({'hilos': h, 'tipo': tipo})
        salida['escenarios']['lectura'].append(r)
        print('  lectura %4d hilos: %7.1f rps p50=%6.1f p95=%7.1f p99=%7.1f err=%5.2f%% >500ms=%5.2f%%' % (h, r['rps'], r['p50_ms'], r['p95_ms'], r['p99_ms'], r['errores_pct'], r['sobre_500ms_pct']), flush=True)

    # ---------------------------------------------------------------- escenario 2: login (BCrypt)
    salida['escenarios']['login'] = []
    cuerpo_login = lambda: ('pac0.%s@carga.local' % sufijo)
    def login():
        r = cli.pedir('auth', 'POST', '/api/v1/auth/login', None, {'correo': cuerpo_login(), 'clave': 'Carga12345'})
        return r[0], r[1]
    for h in ([int(x) for x in a.niveles_login.split(',')] if 'login' in esc_ok else []):
        m, seg = correr(login, h, a.duracion)
        r = resumen(m, seg, {200}); r['hilos'] = h
        salida['escenarios']['login'].append(r)
        print('  login   %4d hilos: %7.1f rps p50=%6.1f p95=%7.1f p99=%7.1f err=%5.2f%%' % (h, r['rps'], r['p50_ms'], r['p95_ms'], r['p99_ms'], r['errores_pct']), flush=True)

    # ---------------------------------------------------------------- escenario 3: reservas distintas (escritura)
    usadas = pool[:a.pacientes]
    resultados = []
    def reservar(k):
        f, p, fr = usadas[k]
        r = cli.pedir('appointment', 'POST', '/api/v1/citas', tokens[k], {'centroId': cen, 'especialidadId': esp, 'franjaId': fr, 'profesionalId': p})
        return r[0], r[1]
    if 'reservas' in esc_ok:
        t0 = time.perf_counter()
        with ThreadPoolExecutor(a.hilos_reserva) as ex:
            res = list(ex.map(reservar, range(min(len(usadas), len(tokens)))))
        salida['escenarios']['reservas_distintas'] = resumen(res, time.perf_counter() - t0, {201}); salida['escenarios']['reservas_distintas']['hilos'] = a.hilos_reserva
        print('  reservas distintas (%d hilos):' % a.hilos_reserva, {k: v for k, v in salida['escenarios']['reservas_distintas'].items() if k in ('peticiones', 'rps', 'p50_ms', 'p95_ms', 'p99_ms', 'errores_pct')}, flush=True)

    # ---------------------------------------------------------------- escenario 4: contencion sobre la misma franja
    rondas, rest = [], pool[a.pacientes:]
    vistos, candidatas = set(), []
    for f, p, fr in rest:
        if f not in vistos and f != fecha_d:     # una franja por fecha: los pacientes no se cruzan entre rondas
            vistos.add(f); candidatas.append((f, p, fr))
    concursantes = tokens[:20]
    for f, p, fr in (candidatas[:10] if 'contencion' in esc_ok else []):
        barrera = threading.Barrier(len(concursantes))
        def disputar(t):
            barrera.wait()
            return cli.pedir('appointment', 'POST', '/api/v1/citas', t, {'centroId': cen, 'especialidadId': esp, 'franjaId': fr, 'profesionalId': p})
        with ThreadPoolExecutor(len(concursantes)) as ex:
            rs = list(ex.map(disputar, concursantes))
        estados = [r[0] for r in rs]
        rondas.append({'ganadores_201': estados.count(201), 'conflictos_409': estados.count(409), 'otros': [e for e in estados if e not in (201, 409)],
                       'max_ms': round(max(r[1] for r in rs), 1)})
    if rondas:
        salida['escenarios']['contencion'] = {'competidores_por_ronda': len(concursantes), 'rondas': rondas,
                                              'invariante_cumplida': all(r['ganadores_201'] == 1 and not r['otros'] for r in rondas)}
        print('  contencion: %d rondas, invariante (1 ganador, resto 409, sin 5xx): %s' % (len(rondas), salida['escenarios']['contencion']['invariante_cumplida']), flush=True)

    json.dump(salida, open(a.salida, 'w'), indent=2)
    print('Resultados en', a.salida)


if __name__ == '__main__':
    main()
