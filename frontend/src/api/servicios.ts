import { llamar, type Pagina } from './http'
import type * as T from './tipos'

type P = { page?: number; size?: number }

export const auth = {
  login: (correo: string, clave: string) => llamar<T.LoginRespuesta>('auth', '/auth/login', { metodo: 'POST', cuerpo: { correo, clave } }),
  registrar: (d: Record<string, unknown>) => llamar<T.Paciente>('auth', '/auth/register', { metodo: 'POST', cuerpo: d }),
  logout: () => llamar<void>('auth', '/auth/logout', { metodo: 'POST' }),
  cambiarClave: (claveActual: string, claveNueva: string) => llamar<void>('auth', '/auth/cambiar-clave', { metodo: 'POST', cuerpo: { claveActual, claveNueva } }),
  miPerfil: () => llamar<T.Paciente>('auth', '/pacientes/me'),
  actualizarPerfil: (d: Record<string, unknown>) => llamar<T.Paciente>('auth', '/pacientes/me', { metodo: 'PATCH', cuerpo: d }),
  usuarios: (q: P & { rol?: string; activo?: boolean }) => llamar<Pagina<T.Cuenta>>('auth', '/admin/usuarios', { query: q }),
  nombresPacientes: (ids: string[]) => llamar<{ usuarioId: string; nombres: string; apellidos: string }[]>('auth', '/pacientes/nombres', { metodo: 'POST', cuerpo: { ids } }),
  estadisticas: () => llamar<T.EstadisticasUsuarios>('auth', '/admin/usuarios/estadisticas'),
  crearCuenta: (d: { correo: string; clave: string; rol: T.Rol }) => llamar<T.Cuenta>('auth', '/admin/usuarios', { metodo: 'POST', cuerpo: d }),
  activar: (id: string) => llamar<T.Cuenta>('auth', `/admin/usuarios/${id}/activar`, { metodo: 'POST' }),
  desactivar: (id: string) => llamar<T.Cuenta>('auth', `/admin/usuarios/${id}/desactivar`, { metodo: 'POST' }),
}

export const catalogo = {
  especialidades: (incluirInactivas = false) => llamar<T.Especialidad[]>('catalog', '/especialidades', { query: { incluirInactivas } }),
  crearEspecialidad: (d: { nombre: string; descripcion?: string }) => llamar<T.Especialidad>('catalog', '/especialidades', { metodo: 'POST', cuerpo: d }),
  actualizarEspecialidad: (id: string, d: { nombre: string; descripcion?: string }) => llamar<T.Especialidad>('catalog', `/especialidades/${id}`, { metodo: 'PUT', cuerpo: d }),
  desactivarEspecialidad: (id: string) => llamar<void>('catalog', `/especialidades/${id}`, { metodo: 'DELETE' }),
  profesionalesDe: (especialidadId: string) => llamar<T.Profesional[]>('catalog', `/especialidades/${especialidadId}/profesionales`),
  centros: (incluirInactivos = false) => llamar<T.Centro[]>('catalog', '/centros', { query: { incluirInactivos } }),
  crearCentro: (d: Omit<T.Centro, 'id' | 'activo'>) => llamar<T.Centro>('catalog', '/centros', { metodo: 'POST', cuerpo: d }),
  actualizarCentro: (id: string, d: Omit<T.Centro, 'id' | 'activo'>) => llamar<T.Centro>('catalog', `/centros/${id}`, { metodo: 'PUT', cuerpo: d }),
  desactivarCentro: (id: string) => llamar<void>('catalog', `/centros/${id}`, { metodo: 'DELETE' }),
  profesionales: (q: P & { texto?: string; activo?: boolean }) => llamar<Pagina<T.Profesional>>('catalog', '/profesionales', { query: q }),
  profesional: (id: string) => llamar<T.DetalleProfesional>('catalog', `/profesionales/${id}`),
  crearProfesional: (d: { usuarioId: string; nombres: string; apellidos: string; registroMedico: string }) => llamar<T.Profesional>('catalog', '/profesionales', { metodo: 'POST', cuerpo: d }),
  desactivarProfesional: (id: string) => llamar<void>('catalog', `/profesionales/${id}`, { metodo: 'DELETE' }),
  centrosDe: (profesionalId: string) => llamar<T.Centro[]>('catalog', `/profesionales/${profesionalId}/centros`),
  asociarEspecialidades: (id: string, ids: string[]) => llamar<T.DetalleProfesional>('catalog', `/profesionales/${id}/especialidades`, { metodo: 'PUT', cuerpo: { ids } }),
  asociarCentros: (id: string, ids: string[]) => llamar<T.DetalleProfesional>('catalog', `/profesionales/${id}/centros`, { metodo: 'PUT', cuerpo: { ids } }),
  horarios: (id: string) => llamar<T.Horario[]>('catalog', `/profesionales/${id}/horarios`),
  definirHorario: (id: string, d: { centroId: string; diaSemana: number; horaInicio: string; horaFin: string; duracionFranjaMin?: number }) =>
    llamar<T.ResultadoHorario>('catalog', `/profesionales/${id}/horarios`, { metodo: 'POST', cuerpo: d }),
  parametros: () => llamar<T.Parametro[]>('catalog', '/parametros'),
  configurarParametros: (parametros: Record<string, string>) => llamar<T.Parametro[]>('catalog', '/parametros', { metodo: 'PUT', cuerpo: { parametros } }),
  festivos: (anio: number) => llamar<T.Festivo[]>('catalog', '/festivos', { query: { anio } }),
}

export const citas = {
  disponibilidad: (profesionalId: string, centroId: string, fecha: string) => llamar<T.Franja[]>('appointment', '/disponibilidad', { query: { profesionalId, centroId, fecha } }),
  reservar: (d: { especialidadId: string; profesionalId: string; centroId: string; franjaId: string }) => llamar<T.Cita>('appointment', '/citas', { metodo: 'POST', cuerpo: d }),
  reprogramar: (id: string, franjaId: string) => llamar<T.Cita>('appointment', `/citas/${id}/reprogramar`, { metodo: 'POST', cuerpo: { franjaId } }),
  cancelar: (id: string, motivo?: string) => llamar<T.Cita>('appointment', `/citas/${id}/cancelar`, { metodo: 'POST', cuerpo: motivo ? { motivo } : {} }),
  mias: (periodo: 'PROXIMAS' | 'PASADAS') => llamar<T.Cita[]>('appointment', '/citas/mias', { query: { periodo } }),
  agenda: (fecha?: string) => llamar<T.Cita[]>('appointment', '/citas/agenda', { query: { fecha } }),
  marcar: (id: string, estado: 'COMPLETADA' | 'NO_ASISTIDA') => llamar<T.Cita>('appointment', `/citas/${id}/estado`, { metodo: 'PATCH', cuerpo: { estado } }),
  historial: (id: string) => llamar<T.HistorialCita[]>('appointment', `/citas/${id}/historial`),
  listar: (q: P & { estado?: string; profesionalId?: string; pacienteId?: string }) => llamar<Pagina<T.Cita>>('appointment', '/citas', { query: q }),
  estadisticas: () => llamar<T.EstadisticasCitas>('appointment', '/citas/estadisticas'),
}

export const clinica = {
  registrar: (d: { citaId: string; motivo: string; diagnostico: string; notas?: string; indicaciones?: string }) => llamar<T.Entrada>('clinical', '/entradas', { metodo: 'POST', cuerpo: d }),
  corregir: (id: string, d: { motivo: string; diagnostico: string; notas?: string; indicaciones?: string }) => llamar<T.Entrada>('clinical', `/entradas/${id}/correcciones`, { metodo: 'POST', cuerpo: d }),
  historiaDe: (pacienteId: string) => llamar<T.Entrada[]>('clinical', `/pacientes/${pacienteId}/historia`),
  resumenDe: (pacienteId: string) => llamar<T.Resumen>('clinical', `/pacientes/${pacienteId}/resumen`),
  miHistoria: () => llamar<T.Entrada[]>('clinical', '/historia/mia'),
  miResumen: () => llamar<T.Resumen>('clinical', '/historia/mia/resumen'),
  atenciones: (q: P) => llamar<Pagina<T.Entrada>>('clinical', '/atenciones', { query: q }),
  diagnosticos: (q: string, signal?: AbortSignal) => llamar<T.Diagnostico[]>('clinical', '/diagnosticos', { query: { q }, signal }),
}

export const notificaciones = {
  listar: (q: P & { estado?: string; tipo?: string; citaId?: string }) => llamar<Pagina<T.Notificacion>>('notification', '/notificaciones', { query: q }),
  estadisticas: () => llamar<T.EstadisticasNotificaciones>('notification', '/notificaciones/estadisticas'),
  reenviar: (id: string) => llamar<T.Notificacion>('notification', `/notificaciones/${id}/reenviar`, { metodo: 'POST' }),
  recordatorios: () => llamar<{ procesadas: number; enviadas: number; fallidas: number }>('notification', '/notificaciones/recordatorios/ejecutar', { metodo: 'POST' }),
  reintentos: () => llamar<{ procesadas: number; enviadas: number; fallidas: number }>('notification', '/notificaciones/reintentos/ejecutar', { metodo: 'POST' }),
}
