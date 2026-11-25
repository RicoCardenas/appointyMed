#!/usr/bin/env node
// Ejecutor minimo de colecciones Postman (sin Newman): corre las peticiones en orden, evalua los scripts pm.test
// con un subconjunto de la API pm.* y muestra el resultado. Sirve para verificar la coleccion sin abrir Postman.
// Uso: node scripts/postman/ejecutar.mjs <coleccion.json> <entorno.json> [--clave=...] [--json=salida.json]
import { readFileSync, writeFileSync } from 'node:fs'

const [, , rutaColeccion, rutaEntorno, ...opciones] = process.argv
const opt = Object.fromEntries(opciones.filter((o) => o.startsWith('--')).map((o) => o.slice(2).split('=')))
const coleccion = JSON.parse(readFileSync(rutaColeccion, 'utf8'))
const entorno = Object.fromEntries(JSON.parse(readFileSync(rutaEntorno, 'utf8')).values.map((v) => [v.key, v.value]))
if (opt.clave) entorno.admin_clave = opt.clave
const variables = Object.fromEntries((coleccion.variable || []).map((v) => [v.key, v.value]))

const aplanar = (items) => items.flatMap((i) => (i.item ? aplanar(i.item) : [i]))
const peticiones = aplanar(coleccion.item)
const scriptsColeccion = (tipo) => (coleccion.event || []).filter((e) => e.listen === tipo).flatMap((e) => e.script.exec)

function sustituir(texto) {
  return String(texto).replace(/\{\{([^}]+)\}\}/g, (_, k) => {
    if (k === '$guid') return crypto.randomUUID()
    if (k === '$timestamp') return Math.floor(Date.now() / 1000)
    return variables[k] ?? entorno[k] ?? ''
  })
}

function expect(valor) {
  const estado = { negado: false }
  const afirmar = (ok, msg) => { if (ok === estado.negado) throw new Error(msg) }
  const cadena = {
    get to() { return cadena }, get be() { return cadena }, get have() { return cadena }, get at() { return cadena },
    get not() { estado.negado = !estado.negado; return cadena },
    equal: (x) => afirmar(valor === x, `se esperaba ${JSON.stringify(x)} y llego ${JSON.stringify(valor)}`),
    below: (x) => afirmar(valor < x, `se esperaba menos de ${x} y llego ${valor}`),
    least: (x) => afirmar(valor >= x, `se esperaba al menos ${x} y llego ${valor}`),
    oneOf: (xs) => afirmar(xs.includes(valor), `se esperaba uno de ${JSON.stringify(xs)} y llego ${JSON.stringify(valor)}`),
    include: (x) => afirmar(valor != null && String(valor).includes(x), `se esperaba que incluyera ${JSON.stringify(x)}; llego ${JSON.stringify(valor)}`),
    property: (k) => afirmar(valor != null && Object.prototype.hasOwnProperty.call(valor, k), `falta la propiedad ${k}`),
  }
  const tipo = (t) => afirmar(t === 'array' ? Array.isArray(valor) : typeof valor === t, `se esperaba tipo ${t}`)
  cadena.a = tipo
  cadena.an = tipo
  return cadena
}

const resultados = []
let siguiente = null
async function ejecutar(p) {
  const cab = {}
  const ctx = { nombre: p.name, pruebas: [], respuesta: null }
  const pm = {
    info: { requestName: p.name },
    collectionVariables: { get: (k) => variables[k], set: (k, v) => { variables[k] = v } },
    environment: { get: (k) => entorno[k] },
    request: { headers: { upsert: ({ key, value }) => { cab[key] = sustituir(value) } } },
    expect,
    test: (nombre, fn) => { try { fn(); ctx.pruebas.push({ nombre, ok: true }) } catch (e) { ctx.pruebas.push({ nombre, ok: false, error: e.message }) } },
    get response() { return ctx.respuesta },
  }
  const postman = { setNextRequest: (n) => { siguiente = n } }
  const correr = (lineas) => new Function('pm', 'postman', 'atob', lineas.join('\n'))(pm, postman, atob)
  for (const e of [...(coleccion.event || []).filter((e) => e.listen === 'prerequest'), ...(p.event || []).filter((e) => e.listen === 'prerequest')]) correr(e.script.exec)
  // Como en Postman: primero los scripts previos, despues se resuelven las variables de la URL, cabeceras y cuerpo
  const url = sustituir(p.request.url.raw)
  for (const h of p.request.header || []) cab[h.key] = sustituir(h.value)
  const cuerpo = p.request.body ? sustituir(p.request.body.raw) : undefined
  const t0 = performance.now()
  let res, texto = ''
  try {
    res = await fetch(url, { method: p.request.method, headers: cab, body: cuerpo, signal: AbortSignal.timeout(60000) })
    texto = await res.text()
  } catch (e) {
    resultados.push({ nombre: p.name, estado: 0, ms: 0, pruebas: [{ nombre: 'conexion', ok: false, error: e.message }] }); return
  }
  const ms = Math.round(performance.now() - t0)
  ctx.respuesta = {
    code: res.status, responseTime: ms, text: () => texto, json: () => JSON.parse(texto), headers: { get: (k) => res.headers.get(k) },
    to: { have: { status: (c) => { if (res.status !== c) throw new Error(`codigo ${res.status}`) } } },
  }
  try {
    for (const e of [...(p.event || []).filter((e) => e.listen === 'test'), ...(coleccion.event || []).filter((e) => e.listen === 'test')]) correr(e.script.exec)
  } catch (e) { ctx.pruebas.push({ nombre: 'script de prueba', ok: false, error: e.message }) }
  resultados.push({ nombre: p.name, estado: res.status, ms, pruebas: ctx.pruebas, cuerpo: texto.slice(0, 160) })
}

let i = 0, vueltas = 0
while (i < peticiones.length && vueltas++ < 500) {
  siguiente = null
  await ejecutar(peticiones[i])
  i = siguiente ? peticiones.findIndex((x) => x.name === siguiente) : i + 1
}

let ok = 0, mal = 0
for (const r of resultados) {
  const fallos = r.pruebas.filter((p) => !p.ok)
  ok += r.pruebas.length - fallos.length; mal += fallos.length
  console.log(`${fallos.length ? 'FALLA' : 'ok   '} ${String(r.estado).padStart(3)} ${String(r.ms).padStart(5)} ms  ${r.nombre}`)
  for (const f of fallos) console.log(`        - ${f.nombre}: ${f.error}`)
  if (fallos.length && r.cuerpo) console.log(`        | ${r.cuerpo}`)
}
console.log(`\npeticiones=${resultados.length} pruebas_ok=${ok} pruebas_falla=${mal}`)
if (opt.json) writeFileSync(opt.json, JSON.stringify(resultados, null, 2))
process.exit(mal ? 1 : 0)
