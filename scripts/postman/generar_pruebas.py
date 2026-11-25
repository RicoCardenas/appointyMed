#!/usr/bin/env python3
"""Genera la coleccion de Postman con PRUEBAS (pm.test) de los 5 servicios y sus entornos.
Cada peticion valida codigo HTTP, formato y tiempo de respuesta < 500 ms (prueba comun a nivel de coleccion).
Salida: entregables/postman/ (se importa en Postman y luego se puede borrar).
Uso: python3 -I scripts/postman/generar_pruebas.py"""
import json, os

RAIZ = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
SALIDA = os.path.join(RAIZ, 'entregables', 'postman')
LIMITE_MS = 500
SVC = {'auth': 'auth_url', 'catalog': 'catalog_url', 'appointment': 'appointment_url', 'clinical': 'clinical_url', 'notification': 'notification_url'}

carpetas = []


def carpeta(nombre):
    c = {'name': nombre, 'item': []}
    carpetas.append(c)
    return c


def js(lineas):
    return {'listen': 'test', 'script': {'type': 'text/javascript', 'exec': lineas}}


def previa(lineas):
    return {'listen': 'prerequest', 'script': {'type': 'text/javascript', 'exec': lineas}}


def peticion(c, nombre, svc, metodo, ruta, token=None, cuerpo=None, estado=200, tests=None, guardar=None, query=None, antes=None, extra_estado=None):
    """token: nombre de la variable con el JWT (None = sin cabecera Authorization). estado: codigo esperado (o lista)."""
    cab = [{'key': 'Content-Type', 'value': 'application/json'}]
    if token:
        cab.append({'key': 'Authorization', 'value': 'Bearer {{%s}}' % token})
    url = '{{%s}}%s' % (SVC[svc], ruta)
    if query:
        url += '?' + '&'.join('%s=%s' % (k, v) for k, v in query.items())
    esperado = estado if isinstance(estado, list) else [estado]
    codigo = ["pm.test('codigo HTTP %s', function () { pm.expect(pm.response.code).to.be.oneOf(%s); });" % ('/'.join(map(str, esperado)), json.dumps(esperado))]
    scripts = codigo + (tests or [])
    if guardar:
        scripts += ["if (pm.response.code < 300) { const j = pm.response.json(); %s }" % ' '.join(
            "pm.collectionVariables.set('%s', %s);" % (k, v) for k, v in guardar.items())]
    # Postman moderno necesita la URL desglosada (host, path y query), no solo el texto
    base, _, qs = url.partition('?')
    host, _, ruta_url = base.partition('/')
    url_obj = {'raw': url, 'host': [host], 'path': ruta_url.split('/')}
    if qs:
        url_obj['query'] = [{'key': kv.split('=', 1)[0], 'value': kv.split('=', 1)[1]} for kv in qs.split('&')]
    item = {'name': nombre, 'event': [js(scripts)], 'response': [],
            'request': {'method': metodo, 'header': cab, 'url': url_obj}}
    if antes:
        item['event'].insert(0, previa(antes))
    if cuerpo is not None:
        item['request']['body'] = {'mode': 'raw', 'raw': cuerpo if isinstance(cuerpo, str) else json.dumps(cuerpo, ensure_ascii=False, indent=2),
                                   'options': {'raw': {'language': 'json'}}}
    c['item'].append(item)


def campo(*nombres):
    return ["pm.test('la respuesta trae %s', function () { const j = pm.response.json(); %s });" % (
        ', '.join(nombres), ' '.join("pm.expect(j).to.have.property('%s');" % n for n in nombres))]


def pagina():
    return ["pm.test('respuesta paginada', function () { const j = pm.response.json(); pm.expect(j).to.have.property('contenido'); "
            "pm.expect(j).to.have.property('totalElementos'); pm.expect(j.pagina).to.equal(0); });"]


def lista():
    return ["pm.test('es una lista', function () { pm.expect(pm.response.json()).to.be.a('array'); });"]


def codigo_error(cod):
    return ["pm.test('error uniforme con codigo %s', function () { const j = pm.response.json(); pm.expect(j.codigo).to.equal('%s'); pm.expect(j).to.have.property('mensaje'); });" % (cod, cod)]


def error_uniforme():
    return ["pm.test('error uniforme (codigo, mensaje, timestamp)', function () { const j = pm.response.json(); pm.expect(j).to.have.property('codigo'); pm.expect(j).to.have.property('mensaje'); pm.expect(j).to.have.property('timestamp'); });"]


# ---------------------------------------------------------------------------------------------- auth
c = carpeta('1. auth-service')
peticion(c, 'Login del administrador', 'auth', 'POST', '/api/v1/auth/login',
         cuerpo={'correo': '{{admin_correo}}', 'clave': '{{admin_clave}}'},
         tests=campo('accessToken', 'rol') + ["pm.test('rol ADMINISTRATIVO', function () { pm.expect(pm.response.json().rol).to.equal('ADMINISTRATIVO'); });"],
         guardar={'adminToken': 'j.accessToken'})
peticion(c, 'Login con clave incorrecta (401)', 'auth', 'POST', '/api/v1/auth/login',
         cuerpo={'correo': '{{admin_correo}}', 'clave': 'ClaveIncorrecta1'}, estado=401, tests=error_uniforme())
peticion(c, 'Registrar paciente', 'auth', 'POST', '/api/v1/auth/register', estado=201,
         antes=["const t = Date.now().toString(); pm.collectionVariables.set('pacCorreo', 'paciente.' + t + '@prueba.local'); pm.collectionVariables.set('pacCedula', 'P' + t); pm.collectionVariables.set('sufijo', t);"],
         cuerpo={'correo': '{{pacCorreo}}', 'clave': 'Prueba12345', 'cedula': '{{pacCedula}}', 'nombres': 'Ana', 'apellidos': 'Perez', 'fechaNacimiento': '1990-05-01'})
peticion(c, 'Registro con datos invalidos (400)', 'auth', 'POST', '/api/v1/auth/register', estado=400,
         cuerpo={'correo': 'no-es-correo', 'clave': '1'}, tests=error_uniforme())
peticion(c, 'Registro con correo repetido (409)', 'auth', 'POST', '/api/v1/auth/register', estado=409,
         cuerpo={'correo': '{{pacCorreo}}', 'clave': 'Prueba12345', 'cedula': 'X{{sufijo}}', 'nombres': 'Ana', 'apellidos': 'Perez', 'fechaNacimiento': '1990-05-01'},
         tests=error_uniforme())
peticion(c, 'Login del paciente', 'auth', 'POST', '/api/v1/auth/login', cuerpo={'correo': '{{pacCorreo}}', 'clave': 'Prueba12345'},
         tests=campo('accessToken'),
         guardar={'pacToken': 'j.accessToken', 'pacienteId': "JSON.parse(atob(j.accessToken.split('.')[1].replace(/-/g, '+').replace(/_/g, '/'))).sub"})
peticion(c, 'Perfil del paciente (CU-04)', 'auth', 'GET', '/api/v1/pacientes/me', token='pacToken', tests=campo('correo', 'cedula'))
peticion(c, 'Crear cuenta de profesional (CU-06)', 'auth', 'POST', '/api/v1/admin/usuarios', token='adminToken', estado=201,
         cuerpo={'correo': 'profesional.{{sufijo}}@prueba.local', 'clave': 'Prueba12345', 'rol': 'PROFESIONAL'},
         tests=campo('usuarioId'), guardar={'profUsuarioId': 'j.usuarioId'})
peticion(c, 'Login del profesional', 'auth', 'POST', '/api/v1/auth/login', cuerpo={'correo': 'profesional.{{sufijo}}@prueba.local', 'clave': 'Prueba12345'},
         guardar={'profToken': 'j.accessToken'})
peticion(c, 'GET all usuarios paginado (admin)', 'auth', 'GET', '/api/v1/admin/usuarios', token='adminToken', query={'page': 0, 'size': 5}, tests=pagina())
peticion(c, 'Estadisticas de usuarios (admin)', 'auth', 'GET', '/api/v1/admin/usuarios/estadisticas', token='adminToken')
peticion(c, 'Paciente no lista usuarios (403)', 'auth', 'GET', '/api/v1/admin/usuarios', token='pacToken', estado=403, tests=error_uniforme())
peticion(c, 'Sin token (401)', 'auth', 'GET', '/api/v1/pacientes/me', estado=401, tests=error_uniforme())

# ------------------------------------------------------------------------------------------- catalog
c = carpeta('2. catalog-service')
peticion(c, 'Crear especialidad', 'catalog', 'POST', '/api/v1/especialidades', token='adminToken', estado=201,
         cuerpo={'nombre': 'Prueba {{sufijo}}', 'descripcion': 'Especialidad de prueba'}, tests=campo('id', 'nombre'), guardar={'especialidadId': 'j.id'})
peticion(c, 'Especialidad invalida (400)', 'catalog', 'POST', '/api/v1/especialidades', token='adminToken', estado=400, cuerpo={'nombre': ''}, tests=error_uniforme())
peticion(c, 'Paciente no crea centros (403)', 'catalog', 'POST', '/api/v1/centros', token='pacToken', estado=403,
         cuerpo={'nombre': 'X', 'direccion': 'X', 'ciudad': 'X'}, tests=error_uniforme())
peticion(c, 'Crear centro', 'catalog', 'POST', '/api/v1/centros', token='adminToken', estado=201,
         cuerpo={'nombre': 'Sede {{sufijo}}', 'direccion': 'Calle 1 # 2-3', 'ciudad': 'Bogota', 'telefono': '6015550100'}, tests=campo('id'), guardar={'centroId': 'j.id'})
peticion(c, 'Crear profesional', 'catalog', 'POST', '/api/v1/profesionales', token='adminToken', estado=201,
         cuerpo={'usuarioId': '{{profUsuarioId}}', 'nombres': 'Camila', 'apellidos': 'Rojas', 'registroMedico': 'RM{{sufijo}}'}, tests=campo('id'), guardar={'profesionalId': 'j.id'})
peticion(c, 'Asociar especialidades (CU-12)', 'catalog', 'PUT', '/api/v1/profesionales/{{profesionalId}}/especialidades', token='adminToken',
         cuerpo={'ids': ['{{especialidadId}}']})
peticion(c, 'Asociar centros (CU-12)', 'catalog', 'PUT', '/api/v1/profesionales/{{profesionalId}}/centros', token='adminToken',
         cuerpo={'ids': ['{{centroId}}']})
for dia, nombre in enumerate(['lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo'], start=1):
    peticion(c, 'Horario de %s y generacion de franjas (CU-13)' % nombre, 'catalog', 'POST', '/api/v1/profesionales/{{profesionalId}}/horarios', token='adminToken', estado=201,
             cuerpo={'centroId': '{{centroId}}', 'diaSemana': dia, 'horaInicio': '08:00', 'horaFin': '12:00', 'duracionFranjaMin': 30},
             tests=["pm.test('franjas generadas en appointment', function () { pm.expect(pm.response.json().franjasGeneradas).to.equal(true); });"])
peticion(c, 'Horario traslapado (409)', 'catalog', 'POST', '/api/v1/profesionales/{{profesionalId}}/horarios', token='adminToken', estado=409,
         cuerpo={'centroId': '{{centroId}}', 'diaSemana': 1, 'horaInicio': '09:00', 'horaFin': '11:00'}, tests=codigo_error('HORARIO_TRASLAPADO'))
peticion(c, 'Listar especialidades (CU-08)', 'catalog', 'GET', '/api/v1/especialidades', token='pacToken', tests=lista())
peticion(c, 'Profesionales por especialidad (CU-09)', 'catalog', 'GET', '/api/v1/especialidades/{{especialidadId}}/profesionales', token='pacToken', tests=lista())
peticion(c, 'Centros del profesional (CU-10)', 'catalog', 'GET', '/api/v1/profesionales/{{profesionalId}}/centros', token='pacToken', tests=lista())
peticion(c, 'Listar centros', 'catalog', 'GET', '/api/v1/centros', token='adminToken', tests=lista())
peticion(c, 'GET all profesionales paginado', 'catalog', 'GET', '/api/v1/profesionales', token='adminToken', query={'page': 0, 'size': 5}, tests=pagina())
peticion(c, 'Detalle de profesional', 'catalog', 'GET', '/api/v1/profesionales/{{profesionalId}}', token='pacToken', tests=["pm.test('trae el profesional', function () { const p = pm.response.json().profesional; pm.expect(p).to.have.property('id'); pm.expect(p).to.have.property('nombres'); });"])
peticion(c, 'Profesional inexistente (404)', 'catalog', 'GET', '/api/v1/profesionales/00000000-0000-0000-0000-000000000000', token='pacToken', estado=404, tests=error_uniforme())
peticion(c, 'Horarios del profesional', 'catalog', 'GET', '/api/v1/profesionales/{{profesionalId}}/horarios', token='adminToken', tests=lista())
peticion(c, 'Parametros del servicio (CU-15)', 'catalog', 'GET', '/api/v1/parametros', token='adminToken')
peticion(c, 'Festivos (API externa Nager.Date)', 'catalog', 'GET', '/api/v1/festivos', token='adminToken', query={'anio': 2026}, tests=lista())

# --------------------------------------------------------------------------------------- appointment
c = carpeta('3. appointment-service')
sel = ["const f = pm.response.json();",
       "if (Array.isArray(f) && f.length < 3 && Number(pm.collectionVariables.get('reintentos')) < 10) {",
       "  const d = new Date(pm.collectionVariables.get('fecha') + 'T00:00:00Z'); d.setUTCDate(d.getUTCDate() + 1);",
       "  pm.collectionVariables.set('fecha', d.toISOString().slice(0, 10));",
       "  pm.collectionVariables.set('reintentos', String(Number(pm.collectionVariables.get('reintentos')) + 1));",
       "  postman.setNextRequest(pm.info.requestName);",
       "} else {",
       "  pm.test('hay franjas disponibles ese dia', function () { pm.expect(f.length).to.be.at.least(3); });",
       "  pm.collectionVariables.set('franjaId', f[0].id); pm.collectionVariables.set('franja2Id', f[1].id);",
       "}"]
peticion(c, 'Disponibilidad por profesional y fecha (CU-17)', 'appointment', 'GET', '/api/v1/disponibilidad', token='pacToken',
         query={'profesionalId': '{{profesionalId}}', 'centroId': '{{centroId}}', 'fecha': '{{fecha}}'}, tests=sel,
         antes=["if (!pm.collectionVariables.get('fecha') || pm.collectionVariables.get('reintentos') === undefined) { const d = new Date(); d.setUTCDate(d.getUTCDate() + 3); pm.collectionVariables.set('fecha', d.toISOString().slice(0, 10)); pm.collectionVariables.set('reintentos', '0'); }"])
peticion(c, 'Disponibilidad sin fecha (400)', 'appointment', 'GET', '/api/v1/disponibilidad', token='pacToken', estado=400,
         query={'profesionalId': '{{profesionalId}}', 'centroId': '{{centroId}}'}, tests=error_uniforme())
peticion(c, 'Reservar cita (CU-18)', 'appointment', 'POST', '/api/v1/citas', token='pacToken', estado=201,
         cuerpo={'centroId': '{{centroId}}', 'especialidadId': '{{especialidadId}}', 'franjaId': '{{franjaId}}', 'profesionalId': '{{profesionalId}}'},
         tests=campo('id', 'estado') + ["pm.test('cita CONFIRMADA', function () { pm.expect(pm.response.json().estado).to.equal('CONFIRMADA'); });"],
         guardar={'citaId': 'j.id'})
peticion(c, 'Reservar la misma franja (409)', 'appointment', 'POST', '/api/v1/citas', token='pacToken', estado=409,
         cuerpo={'centroId': '{{centroId}}', 'especialidadId': '{{especialidadId}}', 'franjaId': '{{franjaId}}', 'profesionalId': '{{profesionalId}}'},
         tests=error_uniforme())
peticion(c, 'Mis citas (CU-21)', 'appointment', 'GET', '/api/v1/citas/mias', token='pacToken')
peticion(c, 'Detalle de la cita', 'appointment', 'GET', '/api/v1/citas/{{citaId}}', token='pacToken', tests=campo('id', 'estado'))
peticion(c, 'Historial de estados (CU-24)', 'appointment', 'GET', '/api/v1/citas/{{citaId}}/historial', token='pacToken', tests=lista())
peticion(c, 'Reprogramar cita (CU-19)', 'appointment', 'POST', '/api/v1/citas/{{citaId}}/reprogramar', token='pacToken',
         cuerpo={'franjaId': '{{franja2Id}}'}, tests=campo('id', 'estado'))
peticion(c, 'Agenda del profesional (CU-22)', 'appointment', 'GET', '/api/v1/citas/agenda', token='profToken')
peticion(c, 'Paciente no ve la agenda (403)', 'appointment', 'GET', '/api/v1/citas/agenda', token='pacToken', estado=403, tests=error_uniforme())
peticion(c, 'GET all citas paginado (admin)', 'appointment', 'GET', '/api/v1/citas', token='adminToken', query={'page': 0, 'size': 5}, tests=pagina())
peticion(c, 'Estadisticas de citas (admin)', 'appointment', 'GET', '/api/v1/citas/estadisticas', token='adminToken')
peticion(c, 'Cancelar cita (CU-20)', 'appointment', 'POST', '/api/v1/citas/{{citaId}}/cancelar', token='pacToken',
         cuerpo={'motivo': 'Prueba automatizada'}, tests=["pm.test('cita CANCELADA', function () { pm.expect(pm.response.json().estado).to.equal('CANCELADA'); });"])
peticion(c, 'Cita cancelada no se vuelve a cancelar', 'appointment', 'POST', '/api/v1/citas/{{citaId}}/cancelar', token='pacToken',
         cuerpo={'motivo': 'Otra vez'}, estado=[409, 422], tests=error_uniforme())

# -------------------------------------------------------------------------------------------- clinical
c = carpeta('4. clinical-history-service')
peticion(c, 'Buscar diagnosticos CIE-10 (API externa NLM)', 'clinical', 'GET', '/api/v1/diagnosticos', token='profToken', query={'q': 'diabetes'}, tests=lista())
peticion(c, 'Mi historia clinica (CU-30)', 'clinical', 'GET', '/api/v1/historia/mia', token='pacToken')
peticion(c, 'Resumen de mi historia', 'clinical', 'GET', '/api/v1/historia/mia/resumen', token='pacToken')
peticion(c, 'Registrar atencion de una cita cancelada (rechazada)', 'clinical', 'POST', '/api/v1/entradas', token='profToken', estado=[403, 404, 409, 422],
         cuerpo={'citaId': '{{citaId}}', 'motivo': 'Control', 'diagnostico': 'E11'}, tests=error_uniforme())
peticion(c, 'Profesional sin relacion no ve la historia (403)', 'clinical', 'GET', '/api/v1/pacientes/{{pacienteId}}/historia', token='profToken', estado=403, tests=error_uniforme())
peticion(c, 'Paciente no lista atenciones (403)', 'clinical', 'GET', '/api/v1/atenciones', token='pacToken', estado=403, tests=error_uniforme())
peticion(c, 'GET all atenciones paginado (profesional)', 'clinical', 'GET', '/api/v1/atenciones', token='profToken', query={'page': 0, 'size': 5}, tests=pagina())

# --------------------------------------------------------------------------------------- notification
c = carpeta('5. notification-service')
peticion(c, 'GET all notificaciones paginado (admin)', 'notification', 'GET', '/api/v1/notificaciones', token='adminToken', query={'page': 0, 'size': 5}, tests=pagina())
peticion(c, 'Estadisticas de notificaciones', 'notification', 'GET', '/api/v1/notificaciones/estadisticas', token='adminToken')
peticion(c, 'Notificaciones de la cita', 'notification', 'GET', '/api/v1/notificaciones/cita/{{citaId}}', token='adminToken', tests=lista())
peticion(c, 'Ejecutar recordatorios (CU-34)', 'notification', 'POST', '/api/v1/notificaciones/recordatorios/ejecutar', token='adminToken')
peticion(c, 'Ejecutar reintentos (CU-35)', 'notification', 'POST', '/api/v1/notificaciones/reintentos/ejecutar', token='adminToken')
peticion(c, 'Paciente no lista notificaciones (403)', 'notification', 'GET', '/api/v1/notificaciones', token='pacToken', estado=403, tests=error_uniforme())

# ------------------------------------------------------------------------------------------- sesion
c = carpeta('6. Cierre de sesion')
peticion(c, 'Cerrar sesion del paciente (CU-03)', 'auth', 'POST', '/api/v1/auth/logout', token='pacToken', estado=[200, 204])
peticion(c, 'El token cerrado ya no sirve (401)', 'auth', 'GET', '/api/v1/pacientes/me', token='pacToken', estado=401, tests=error_uniforme())

coleccion = {
    'info': {'name': 'Appointy Med - pruebas automatizadas',
             'description': 'Flujo completo de los 5 servicios con pruebas de codigo HTTP, formato y tiempo de respuesta (< %d ms). '
                            'Elige el entorno (local o Render), completa admin_clave y ejecuta la coleccion con el Runner en orden.' % LIMITE_MS,
             'schema': 'https://schema.getpostman.com/json/collection/v2.1.0/collection.json'},
    'event': [
        previa(["pm.request.headers.upsert({key: 'X-Correlation-Id', value: 'postman-' + Date.now() + '-' + Math.floor(Math.random() * 1e6)});"]),
        js(["pm.test('tiempo de respuesta < %d ms' , function () { pm.expect(pm.response.responseTime).to.be.below(%d); });" % (LIMITE_MS, LIMITE_MS),
            "if (pm.response.text().length > 0) { pm.test('responde JSON', function () { pm.expect(pm.response.headers.get('Content-Type')).to.include('application/json'); }); }"])],
    'variable': [{'key': k, 'value': ''} for k in ['adminToken', 'pacToken', 'profToken', 'pacCorreo', 'pacCedula', 'sufijo', 'pacienteId', 'profUsuarioId',
                                                   'especialidadId', 'centroId', 'profesionalId', 'fecha', 'reintentos', 'franjaId', 'franja2Id', 'citaId']],
    'item': carpetas,
}


def entorno(nombre, urls, clave):
    return {'name': nombre, '_postman_variable_scope': 'environment', 'values':
            [{'key': k, 'value': v, 'enabled': True} for k, v in urls.items()] +
            [{'key': 'admin_correo', 'value': 'admin@demo.local' if 'local' in nombre.lower() else '', 'enabled': True},
             {'key': 'admin_clave', 'value': clave, 'type': 'secret', 'enabled': True}]}


local = {v: 'http://localhost:%d' % p for v, p in zip(SVC.values(), [8081, 8082, 8083, 8084, 8085])}
render = {v: 'https://appointy-%s.onrender.com' % n for n, v in SVC.items()}
os.makedirs(SALIDA, exist_ok=True)
json.dump(coleccion, open(os.path.join(SALIDA, 'appointy-pruebas.postman_collection.json'), 'w', encoding='utf-8'), indent=2, ensure_ascii=False)
json.dump(entorno('Appointy local', local, 'Demo12345'), open(os.path.join(SALIDA, 'appointy-local.postman_environment.json'), 'w', encoding='utf-8'), indent=2)
json.dump(entorno('Appointy Render', render, ''), open(os.path.join(SALIDA, 'appointy-render.postman_environment.json'), 'w', encoding='utf-8'), indent=2)
print('peticiones:', sum(len(c['item']) for c in carpetas))
