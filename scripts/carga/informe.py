#!/usr/bin/env python3
"""Convierte los resultados JSON (carga.py y ejecutar.mjs) en fragmentos de LaTeX para el articulo:
tablas, macros numericos y coordenadas de las graficas (pgfplots).
Uso: python3 -I scripts/carga/informe.py carpeta_salida carpeta_resultados
Espera en carpeta_resultados: carga-local.json, postman-local.json, carga-render-like.json, postman-render-like.json
y, opcionales, estres-local.json (niveles extra de lectura) y carga-local-segunda-ejecucion.json."""
import json, os, sys

salida, res = sys.argv[1], sys.argv[2]
os.makedirs(salida, exist_ok=True)


def cargar(nombre):
    ruta = os.path.join(res, nombre)
    return json.load(open(ruta)) if os.path.exists(ruta) else None


def escribir(nombre, texto):
    open(os.path.join(salida, nombre), 'w', encoding='utf-8').write(texto)


def f(x, d=1):
    return ('%.*f' % (d, x)).replace('.', ',') if isinstance(x, float) else str(x)


def coords(pares):
    return ' '.join('(%s,%s)' % (a, b) for a, b in pares)


A = cargar('carga-local.json')
extra = cargar('estres-local.json')
A2 = cargar('carga-local-segunda-ejecucion.json')
B = cargar('carga-render-like.json')
PA = cargar('postman-local.json')
PB = cargar('postman-render-like.json')
lecturaA = A['escenarios']['lectura'] + (extra['escenarios']['lectura'] if extra else [])
lecturaB = B['escenarios']['lectura']
lecturaA2 = A2['escenarios']['lectura'] if A2 else []

# ---- tablas completas (tabular) ----
def tabla(cabecera, filas, cols):
    return ('\\begin{tabular}{%s}\n\\toprule\n%s \\\\\n\\midrule\n' % (cols, cabecera) + '\n'.join(filas) + '\n\\bottomrule\n\\end{tabular}\n')

filas = [' & '.join(str(x) for x in (r['hilos'], r['tipo'], f(r['rps']), f(r['p50_ms']), f(r['p95_ms']), f(r['p99_ms']), f(r['errores_pct'], 2))) + ' \\\\' for r in lecturaA]
escribir('tabla_lectura.tex', tabla('Hilos & Tipo & Pet./s & p50 & p95 & p99 & Err. (\\%)', filas, 'rlrrrrr'))


def filas_login(carga):
    return [' & '.join(str(x) for x in (r['hilos'], f(r['rps']), f(r['p50_ms']), f(r['p95_ms']), f(r['p99_ms']), f(r['errores_pct'], 2))) + ' \\\\'
            for r in carga['escenarios']['login']]
escribir('tabla_login_ab.tex', ('\\begin{tabular}{rrrrrr}\n\\toprule\nHilos & Pet./s & p50 & p95 & p99 & Err. (\\%) \\\\\n\\midrule\n' + '\n'.join(filas_login(A)) +
                               '\n\\midrule\n' + '\n'.join(filas_login(B)) + '\n\\bottomrule\n\\end{tabular}\n'))

# ---- macros numericos (los mismos nombres de siempre; sufijo Rl = configuracion B) ----
def macros(carga, postman, mc):
    esc = carga['escenarios']
    m = []
    r = esc['reservas_distintas']
    m += [('resPeticiones', r['peticiones']), ('resHilos', r['hilos']), ('resRps', f(r['rps'])), ('resPcincuenta', f(r['p50_ms'])),
          ('resPnoventaycinco', f(r['p95_ms'])), ('resPnoventaynueve', f(r['p99_ms'])), ('resErrores', f(r['errores_pct'], 2))]
    c = esc['contencion']
    m += [('conRondas', len(c['rondas'])), ('conCompetidores', c['competidores_por_ronda']), ('conGanadores', sum(x['ganadores_201'] for x in c['rondas'])),
          ('conConflictos', sum(x['conflictos_409'] for x in c['rondas'])), ('conOtros', sum(len(x['otros']) for x in c['rondas'])),
          ('conMaxMs', f(max(x['max_ms'] for x in c['rondas'])))]
    ms = sorted(x['ms'] for x in postman)
    m += [('pmPeticiones', len(postman)), ('pmPruebas', sum(len(x['pruebas']) for x in postman)),
          ('pmFallas', sum(1 for x in postman for p in x['pruebas'] if not p['ok'])), ('pmMedia', f(sum(ms) / len(ms))), ('pmMax', ms[-1]),
          ('pmPcincuenta', ms[len(ms) // 2]), ('pmPnoventaycinco', ms[int(0.95 * (len(ms) - 1))]), ('pmBajo', sum(1 for x in ms if x < 500))]
    return ''.join('\\newcommand{\\%s%s}{%s}\n' % (n, mc, v) for n, v in m)

escribir('macros.tex', macros(A, PA, ''))
escribir('macros_rl.tex', macros(B, PB, 'Rl'))
# coordenadas historicas de las figuras de latencia (A y B)
for sf, lec in (('', lecturaA), ('_rl', lecturaB)):
    mc = 'Rl' if sf else ''
    escribir('coords%s.tex' % sf, ''.join('\\def\\coords%s%s{%s}\n' % (n, mc, coords((r['hilos'], r[c]) for r in lec))
                                          for n, c in [('Pcincuenta', 'p50_ms'), ('Pnoventaycinco', 'p95_ms'), ('Pnoventaynueve', 'p99_ms'), ('Rps', 'rps')]))

# ---- graficas nuevas ----
def cdf(postman):
    v = sorted(max(x['ms'], 1) for x in postman)
    return coords((ms, round(100 * (i + 1) / len(v), 2)) for i, ms in enumerate(v))

def login(carga, clave):
    return coords((r['hilos'], r[clave]) for r in carga['escenarios']['login'])

def contencion(carga):
    return coords((i + 1, r['max_ms']) for i, r in enumerate(carga['escenarios']['contencion']['rondas']))

def pct(lec):
    return coords((r['hilos'], r['sobre_500ms_pct']) for r in lec)

simbolos = ','.join(str(r['hilos']) for r in lecturaA)
g = ['\\def\\cdfA{%s}' % cdf(PA), '\\def\\cdfB{%s}' % cdf(PB),
     '\\def\\loginAcincuenta{%s}' % login(A, 'p50_ms'), '\\def\\loginBcincuenta{%s}' % login(B, 'p50_ms'),
     '\\def\\loginAnoventaycinco{%s}' % login(A, 'p95_ms'), '\\def\\loginBnoventaycinco{%s}' % login(B, 'p95_ms'),
     '\\def\\conA{%s}' % contencion(A), '\\def\\conB{%s}' % contencion(B),
     '\\def\\pctA{%s}' % pct(lecturaA), '\\def\\pctAdos{%s}' % pct(lecturaA2), '\\def\\pctB{%s}' % pct(lecturaB),
     '\\def\\simbolosHilos{%s}' % simbolos,
     '\\def\\coordsPcincuentaAdos{%s}' % coords((r['hilos'], r['p50_ms']) for r in lecturaA2),
     '\\def\\coordsPnoventaynueveAdos{%s}' % coords((r['hilos'], r['p99_ms']) for r in lecturaA2)]
escribir('graficas.tex', '\n'.join(g) + '\n')
print('fragmentos escritos en', salida)
