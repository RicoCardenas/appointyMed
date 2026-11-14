export type Rol = 'PACIENTE' | 'PROFESIONAL' | 'ADMINISTRATIVO'
export type EstadoCita = 'CONFIRMADA' | 'CANCELADA' | 'COMPLETADA' | 'NO_ASISTIDA'

export interface LoginRespuesta { accessToken: string; tokenType: string; expiraEn: string; usuarioId: string; rol: Rol }
export interface Paciente {
  usuarioId: string; correo: string; cedula: string; nombres: string; apellidos: string; fechaNacimiento: string
  telefono?: string | null; direccion?: string | null; ciudad?: string | null
}
export interface Cuenta { usuarioId: string; correo: string; rol: Rol; activo: boolean; creadoEn: string }
export interface EstadisticasUsuarios { pacientes: number; profesionales: number; administrativos: number; activos: number; inactivos: number }

export interface Especialidad { id: string; nombre: string; descripcion?: string | null; activo: boolean }
export interface Centro { id: string; nombre: string; direccion: string; ciudad: string; telefono?: string | null; activo: boolean }
export interface Profesional { id: string; usuarioId: string; nombres: string; apellidos: string; registroMedico: string; activo: boolean }
export interface DetalleProfesional { profesional: Profesional; especialidadIds: string[]; centroIds: string[] }
export interface Horario { id: string; profesionalId: string; centroId: string; diaSemana: number; horaInicio: string; horaFin: string; duracionFranjaMin: number; activo: boolean }
export interface ResultadoHorario { horario: Horario; franjasGeneradas: boolean; franjasCreadas: number; franjasExistentes: number; mensaje: string }
export interface Parametro { clave: string; valor: string; descripcion?: string | null }
export interface Festivo { fecha: string; nombre: string }

export interface Franja { id: string; profesionalId: string; centroId: string; fecha: string; horaInicio: string; horaFin: string; estado: string }
export interface Cita {
  id: string; pacienteId: string; profesionalId: string; especialidadId: string; centroId: string; franjaId: string
  estado: EstadoCita; fecha: string | null; horaInicio: string | null; horaFin: string | null; creadoEn: string; modificadoEn: string
}
export interface HistorialCita { id: string; estadoAnterior?: string | null; estadoNuevo: string; usuarioId: string; rolUsuario: string; detalle?: string | null; fecha: string }
export interface EstadisticasCitas { confirmadas: number; canceladas: number; completadas: number; noAsistidas: number }

export interface Entrada {
  id: string; pacienteId: string; profesionalId: string; citaId: string; motivo: string; diagnostico: string
  notas?: string | null; indicaciones?: string | null; corrigeAId?: string | null; creadoEn: string
}
export interface Resumen { atenciones: number; correcciones: number; ultimaAtencionEn?: string | null; ultimoDiagnostico?: string | null }
export interface Diagnostico { codigo: string; descripcion: string }

export interface Notificacion {
  id: string; citaId: string; tipo: string; destinatario: string; asunto: string; estado: string; intentos: number
  ultimoError?: string | null; programadaPara: string; enviadaEn?: string | null; creadoEn: string
}
export interface EstadisticasNotificaciones {
  pendientes: number; enviadas: number; fallidas: number; canceladas: number
  confirmaciones: number; reprogramaciones: number; cancelaciones: number; recordatorios: number
}
