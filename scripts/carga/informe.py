#!/usr/bin/env python3
"""Convierte los resultados JSON (carga.py y ejecutar.mjs) en fragmentos de LaTeX para el articulo.
Uso: python3 -I scripts/carga/informe.py carpeta_salida carga.json postman.json [carga_rl.json postman_rl.json]
El segundo conjunto (opcional) son las mediciones con los limites del plan gratuito de Render (sufijo _rl / Rl)."""
import json, os, sys

salida = sys.argv[1]
conjuntos = [('', '', sys.argv[2], sys.argv[3])]
if len(sys.argv) >= 6:
    conjuntos.append(('_rl', 'Rl', sys.argv[4], sys.argv[5]))
os.makedirs(salida, exist_ok=True)


def escribir(nombre, texto):
    if nombre.startswith('tabla'):   # '%' al final de cada fila: sin espacios sobrantes dentro de tabular
        texto = ''.join(l + '%\n' for l in texto.splitlines())
    open(os.path.join(salida, nombre), 'w', encoding='utf-8').write(texto)


def f(x, d=1):
    return ('%.*f' % (d, x)).replace('.', ',') if isinstance(x, float) else str(x)


login_filas = {}
for sf, mc, ruta_carga, ruta_pm in conjuntos:
    carga = json.load(open(ruta_carga))
    postman = json.load(open(ruta_pm))
    esc = carga['escenarios']
    filas_l = [' & '.join(str(x) for x in (r['hilos'], r['tipo'], f(r['rps']), f(r['p50_ms']), f(r['p95_ms']), f(r['p99_ms']), f(r['errores_pct'], 2))) + ' \\\\'
               for r in esc.get('lectura', [])]
    escribir('tabla_lectura%s.tex' % sf, '\\begin{tabular}{rlrrrrr}\n\\toprule\nHilos & Tipo & Pet./s & p50 & p95 & p99 & Err. (\\%) \\\\\n\\midrule\n' + '\n'.join(filas_l) + '\n\\bottomrule\n\\end{tabular}\n')
    filas_g = [' & '.join(str(x) for x in (r['hilos'], f(r['rps']), f(r['p50_ms']), f(r['p95_ms']), f(r['p99_ms']), f(r['errores_pct'], 2))) + ' \\\\'
               for r in esc.get('login', [])]
    login_filas[sf] = filas_g

    def serie(clave, nombre):
        return ' '.join('(%d,%s)' % (r['hilos'], r[clave]) for r in esc.get(nombre, []))
    escribir('coords%s.tex' % sf, ''.join('\\def\\coords%s%s{%s}\n' % (n, mc, serie(c, 'lectura')) for n, c in
                                          [('Pcincuenta', 'p50_ms'), ('Pnoventaycinco', 'p95_ms'), ('Pnoventaynueve', 'p99_ms'), ('Rps', 'rps')]))
    m = []
    if 'reservas_distintas' in esc:
        r = esc['reservas_distintas']
        m += [('resPeticiones', r['peticiones']), ('resHilos', r['hilos']), ('resRps', f(r['rps'])), ('resPcincuenta', f(r['p50_ms'])),
              ('resPnoventaycinco', f(r['p95_ms'])), ('resPnoventaynueve', f(r['p99_ms'])), ('resErrores', f(r['errores_pct'], 2))]
    if 'contencion' in esc:
        c = esc['contencion']
        m += [('conRondas', len(c['rondas'])), ('conCompetidores', c['competidores_por_ronda']), ('conGanadores', sum(x['ganadores_201'] for x in c['rondas'])),
              ('conConflictos', sum(x['conflictos_409'] for x in c['rondas'])), ('conOtros', sum(len(x['otros']) for x in c['rondas'])),
              ('conMaxMs', f(max(x['max_ms'] for x in c['rondas'])))]
    ms = sorted(x['ms'] for x in postman)
    pruebas = sum(len(x['pruebas']) for x in postman)
    falla = sum(1 for x in postman for p in x['pruebas'] if not p['ok'])
    m += [('pmPeticiones', len(postman)), ('pmPruebas', pruebas), ('pmFallas', falla), ('pmMedia', f(sum(ms) / len(ms))), ('pmMax', ms[-1]),
          ('pmPcincuenta', ms[len(ms) // 2]), ('pmPnoventaycinco', ms[int(0.95 * (len(ms) - 1))]), ('pmBajo', sum(1 for x in ms if x < 500))]
    escribir('macros%s.tex' % sf, ''.join('\\newcommand{\\%s%s}{%s}\n' % (n, mc, v) for n, v in m))
escribir('tabla_login_ab.tex', '\\begin{tabular}{rrrrrr}\n\\toprule\nHilos & Pet./s & p50 & p95 & p99 & Err. (\\%) \\\\\n\\midrule\n' + '\n'.join(login_filas.get('', [])) +
         ('\n\\midrule\n' + '\n'.join(login_filas['_rl']) if '_rl' in login_filas else '') + '\n\\bottomrule\n\\end{tabular}\n')
print('fragmentos escritos en', salida)
