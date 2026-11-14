package com.appointy.notification.domain.rule;

import com.appointy.notification.domain.exception.ConflictoException;
import com.appointy.notification.domain.exception.ValidacionException;
import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.SolicitudCita;
import com.appointy.notification.domain.model.TipoNotificacion;

/** Reglas de las notificaciones. Sin dependencias fuera del dominio: el caso de uso entrega los valores ya calculados. */
public final class ReglasNotificacion {

    public static final long ANTICIPACION_RECORDATORIO_MS = 24L * 60 * 60 * 1000;
    public static final int TAMANO_MAXIMO_PAGINA = 100;

    private ReglasNotificacion() {}

    public static TipoNotificacion tipoDe(String tipoCita) {
        if ("CONFIRMADA".equals(tipoCita)) return TipoNotificacion.CONFIRMACION;
        if ("REPROGRAMADA".equals(tipoCita)) return TipoNotificacion.REPROGRAMACION;
        if ("CANCELADA".equals(tipoCita)) return TipoNotificacion.CANCELACION;
        throw new ValidacionException("TIPO_INVALIDO", "El tipo debe ser CONFIRMADA, REPROGRAMADA o CANCELADA");
    }

    public static void validarSolicitud(SolicitudCita s) {
        if (s == null) {
            throw new ValidacionException("SOLICITUD_INVALIDA", "La solicitud es obligatoria");
        }
        tipoDe(s.tipo());
        exigirUuid(s.citaId(), "citaId");
        exigirUuid(s.pacienteId(), "pacienteId");
        if (s.fecha() == null || !s.fecha().matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new ValidacionException("FECHA_INVALIDA", "La fecha debe tener formato yyyy-MM-dd");
        }
        if (s.horaInicio() == null || !s.horaInicio().matches("([01]\\d|2[0-3]):[0-5]\\d")) {
            throw new ValidacionException("HORA_INVALIDA", "La hora debe tener formato HH:mm");
        }
    }

    public static String asunto(TipoNotificacion tipo) {
        switch (tipo) {
            case CONFIRMACION:
                return "Cita confirmada";
            case REPROGRAMACION:
                return "Cita reprogramada";
            case CANCELACION:
                return "Cita cancelada";
            default:
                return "Recordatorio de cita";
        }
    }

    /** Cuerpo sin datos clinicos ni de otros pacientes: solo la fecha, la hora y el codigo de la cita. */
    public static String cuerpo(TipoNotificacion tipo, String fecha, String hora, String citaId) {
        String detalle = "Fecha: " + fecha + ", hora: " + hora + ". Codigo de la cita: " + citaId + ".";
        switch (tipo) {
            case CONFIRMACION:
                return "Tu cita quedo confirmada. " + detalle;
            case REPROGRAMACION:
                return "Tu cita fue reprogramada. Nuevo horario. " + detalle;
            case CANCELACION:
                return "Tu cita fue cancelada. " + detalle;
            default:
                return "Te recordamos tu cita de manana. " + detalle;
        }
    }

    /** El recordatorio solo se programa si faltan mas de 24 h para la cita. */
    public static boolean requiereRecordatorio(TipoNotificacion tipo, long inicioCita, long ahora) {
        boolean vigente = tipo == TipoNotificacion.CONFIRMACION || tipo == TipoNotificacion.REPROGRAMACION;
        return vigente && inicioCita - ANTICIPACION_RECORDATORIO_MS > ahora;
    }

    public static long momentoRecordatorio(long inicioCita) {
        return inicioCita - ANTICIPACION_RECORDATORIO_MS;
    }

    /** Solo se reenvian las pendientes o fallidas: una enviada duplicaria el correo y una cancelada ya no aplica. */
    public static void validarReenvio(Notificacion n) {
        if (n.estado() == EstadoNotificacion.ENVIADA) {
            throw new ConflictoException("YA_ENVIADA", "La notificacion ya fue enviada");
        }
        if (n.estado() == EstadoNotificacion.CANCELADA) {
            throw new ConflictoException("NOTIFICACION_CANCELADA", "La notificacion fue cancelada y no se puede enviar");
        }
    }

    public static void validarPaginacion(int pagina, int tamano) {
        if (pagina < 0) {
            throw new ValidacionException("PAGINA_INVALIDA", "La pagina no puede ser negativa");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO_PAGINA) {
            throw new ValidacionException("TAMANO_INVALIDO", "El tamano debe estar entre 1 y " + TAMANO_MAXIMO_PAGINA);
        }
    }

    private static void exigirUuid(String valor, String campo) {
        if (valor == null || !valor.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            throw new ValidacionException("ID_INVALIDO", campo + " debe ser un UUID");
        }
    }
}
