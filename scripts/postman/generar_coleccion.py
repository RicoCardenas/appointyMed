#!/usr/bin/env python3
"""Genera la coleccion y el entorno de Postman a partir de docs/openapi/*.json (RNF-11).
Uso: python3 -I scripts/postman/generar_coleccion.py"""
import json, os, re

RAIZ = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
SERVICIOS = [('auth', 8081), ('catalog', 8082), ('appointment', 8083), ('clinical-history', 8084), ('notification', 8085)]
UUID = '00000000-0000-0000-0000-000000000001'


def resolver(spec, nodo):
    while isinstance(nodo, dict) and '$ref' in nodo:
        ruta = nodo['$ref'].lstrip('#/').split('/')
        nodo = spec
        for k in ruta:
            nodo = nodo[k]
    return nodo


def ejemplo(spec, esquema, prof=0):
    esquema = resolver(spec, esquema)
    if prof > 5 or not isinstance(esquema, dict):
        return None
    if 'example' in esquema:
        return esquema['example']
    if 'enum' in esquema:
        return esquema['enum'][0]
    t = esquema.get('type')
    f = esquema.get('format')
    if t == 'object' or 'properties' in esquema:
        props = esquema.get('properties', {})
        if not props and isinstance(esquema.get('additionalProperties'), dict):
            return {'clave': ejemplo(spec, esquema['additionalProperties'], prof + 1)}
        return {k: ejemplo(spec, v, prof + 1) for k, v in props.items()}
    if t == 'array':
        return [ejemplo(spec, esquema.get('items', {}), prof + 1)]
    if t == 'integer' or t == 'number':
        return esquema.get('minimum', 1)
    if t == 'boolean':
        return True
    if f == 'uuid':
        return UUID
    if f == 'date':
        return '2026-12-01'
    if f == 'date-time':
        return '2026-12-01T08:00:00Z'
    if f == 'email':
        return 'persona@correo.co'
    if 'pattern' in esquema and 'HH' in esquema.get('message', '') or (esquema.get('pattern') or '').startswith('^([01]'):
        return '08:00'
    return 'texto'


def parametro_ejemplo(spec, p):
    p = resolver(spec, p)
    v = ejemplo(spec, p.get('schema', {}))
    if p.get('schema', {}).get('default') is not None:
        v = p['schema']['default']
    return str(v).lower() if isinstance(v, bool) else str(v)


def peticion(spec, servicio, ruta, metodo, op):
    partes = [x for x in ruta.strip('/').split('/')]
    path = [re.sub(r'\{(\w+)\}', r':\1', x) for x in partes]
    params = [resolver(spec, p) for p in op.get('parameters', [])]
    query = [{'key': p['name'], 'value': parametro_ejemplo(spec, p), 'disabled': not p.get('required', False)} for p in params if p['in'] == 'query']
    variables = [{'key': p['name'], 'value': parametro_ejemplo(spec, p)} for p in params if p['in'] == 'path']
    req = {
        'method': metodo.upper(),
        'header': [{'key': 'Content-Type', 'value': 'application/json'}] if 'requestBody' in op else [],
        'url': {'raw': '{{%s_url}}/%s' % (servicio.replace('-', '_'), '/'.join(path)), 'host': ['{{%s_url}}' % servicio.replace('-', '_')], 'path': path, 'query': query, 'variable': variables},
        'description': op.get('summary', ''),
    }
    if 'security' in op and op['security'] == []:
        req['auth'] = {'type': 'noauth'}
    if 'requestBody' in op:
        cuerpo = resolver(spec, op['requestBody'])['content']
        esquema = next(iter(cuerpo.values()))['schema']
        req['body'] = {'mode': 'raw', 'raw': json.dumps(ejemplo(spec, esquema), indent=2, ensure_ascii=False), 'options': {'raw': {'language': 'json'}}}
    item = {'name': op.get('summary', metodo.upper() + ' ' + ruta), 'request': req, 'response': []}
    if ruta.endswith('/auth/login'):
        item['event'] = [{'listen': 'test', 'script': {'type': 'text/javascript', 'exec': [
            'if (pm.response.code === 200) { pm.collectionVariables.set("token", pm.response.json().accessToken); }']}}]
    return item


def main():
    carpetas = []
    for servicio, _ in SERVICIOS:
        ruta = os.path.join(RAIZ, 'docs', 'openapi', servicio + '.json')
        spec = json.load(open(ruta, encoding='utf-8'))
        por_tag = {}
        for r, ops in spec['paths'].items():
            for m, op in ops.items():
                if m not in ('get', 'post', 'put', 'patch', 'delete'):
                    continue
                por_tag.setdefault((op.get('tags') or ['General'])[0], []).append(peticion(spec, servicio, r, m, op))
        carpetas.append({'name': servicio, 'item': [{'name': t, 'item': i} for t, i in por_tag.items()]})
    coleccion = {
        'info': {'name': 'Appointy Med', 'description': 'Generada desde OpenAPI. Haz login en auth-service (guarda {{token}}) y usa las demas carpetas.',
                 'schema': 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'},
        'auth': {'type': 'bearer', 'bearer': [{'key': 'token', 'value': '{{token}}', 'type': 'string'}]},
        'event': [{'listen': 'prerequest', 'script': {'type': 'text/javascript', 'exec': [
            "pm.request.headers.upsert({key: 'X-Correlation-Id', value: pm.variables.replaceIn('{{$guid}}')});"]}}],
        'variable': [{'key': 'token', 'value': ''}],
        'item': carpetas,
    }
    entorno = {'name': 'Appointy Med local', '_postman_variable_scope': 'environment',
               'values': [{'key': s.replace('-', '_') + '_url', 'value': 'http://localhost:%d' % p, 'enabled': True} for s, p in SERVICIOS]}
    salida = os.path.join(RAIZ, 'docs', 'postman')
    os.makedirs(salida, exist_ok=True)
    json.dump(coleccion, open(os.path.join(salida, 'appointy.postman_collection.json'), 'w', encoding='utf-8'), indent=2, ensure_ascii=False)
    json.dump(entorno, open(os.path.join(salida, 'appointy.postman_environment.json'), 'w', encoding='utf-8'), indent=2, ensure_ascii=False)
    total = sum(len(c['item']) for car in carpetas for c in car['item'])
    print('peticiones:', sum(len(g['item']) for car in carpetas for g in car['item']), 'grupos:', total)


if __name__ == '__main__':
    main()
