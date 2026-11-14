package com.appointy.appointment.domain.rule;

import com.appointy.appointment.domain.exception.AccesoProhibidoException;
import com.appointy.appointment.domain.exception.ConflictoException;
import com.appointy.appointment.domain.exception.ReglaNegocioException;
import com.appointy.appointment.domain.exception.ValidacionException;
import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.AsociacionProfesional;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.Franja;

/**
 * Reglas componibles de reserva, reprogramacion y cancelacion. Sin fechas del JDK: el caso de uso entrega los
 * instantes ya calculados como minutos locales desde 1970 y los limites como texto ISO (que ordena como texto).
 */
public final class ReglasCita {

    public static final int TAMANO_MAXIMO_PAGINA = 100;

    private ReglasCita() {}

    public static void validarPaginacion(int pagina, int tamano) {
        if (pagina < 0) {
            throw new ValidacionException("PAGINA_INVALIDA", "La pagina no puede ser negativa");
        }
        if (tamano < 1 || tamano > TAMANO_MAXIMO_PAGINA) {
            throw new ValidacionException("TAMANO_INVALIDO", "El tamano debe estar entre 1 y " + TAMANO_MAXIMO_PAGINA);
        }
    }

    /** Franja DISPONIBLE (409), con la anticipacion minima (422) y dentro del horizonte de agendamiento (422). */
    public static void exigirFranjaReservable(Franja franja, long inicioMin, long ahoraMin, int anticipacionHoras, String fechaLimite) {
        if (franja.estado() != EstadoFranja.DISPONIBLE) {
            throw new ConflictoException("FRANJA_NO_DISPONIBLE", "La franja ya no esta disponible; elige otra");
        }
        exigirAnticipacion(inicioMin, ahoraMin, anticipacionHoras, "ANTICIPACION_INSUFICIENTE",
                "La cita debe agendarse con al menos " + anticipacionHoras + " horas de anticipacion");
        if (franja.fecha().compareTo(fechaLimite) > 0) {
            throw new ReglaNegocioException("FUERA_DEL_HORIZONTE", "La franja esta mas alla del horizonte maximo de agendamiento");
        }
    }

    /** La franja elegida es del profesional y el centro que pidio el paciente. */
    public static void exigirFranjaDelProfesional(Franja franja, String profesionalId, String centroId) {
        if (!franja.profesionalId().equals(profesionalId) || !franja.centroId().equals(centroId)) {
            throw new ReglaNegocioException("FRANJA_NO_COINCIDE", "La franja no pertenece al profesional y centro indicados");
        }
    }

    public static void exigirProfesionalAsociado(AsociacionProfesional asociacion) {
        if (!asociacion.profesionalActivo() || !asociacion.asociado()) {
            throw new ReglaNegocioException("PROFESIONAL_NO_ASOCIADO", "El profesional no atiende esa especialidad en ese centro");
        }
    }

    /** Sin cruce con otras franjas del paciente el mismo dia. Dos franjas contiguas no se cruzan. */
    public static void exigirSinCruce(Franja nueva, Franja[] existentes) {
        for (Franja otra : existentes) {
            if (otra.fecha().equals(nueva.fecha()) && otra.horaInicio() < nueva.horaFin() && nueva.horaInicio() < otra.horaFin()) {
                throw new ConflictoException("CRUCE_DE_CITAS", "Ya tienes una cita que se cruza con ese horario");
            }
        }
    }

    /** RN-13: solo una cita CONFIRMADA se puede cambiar. */
    public static void exigirConfirmada(Cita cita) {
        if (cita.estado() != EstadoCita.CONFIRMADA) {
            throw new ConflictoException("CITA_NO_MODIFICABLE", "La cita esta " + cita.estado() + " y ya no se puede cambiar");
        }
    }

    /** Solo el paciente de la cita. */
    public static void exigirPacienteDuenio(Cita cita, Actor actor) {
        if (!actor.esPaciente() || !cita.pacienteId().equals(actor.usuarioId())) {
            throw new AccesoProhibidoException("NO_ES_TU_CITA", "No puedes modificar una cita de otro paciente");
        }
    }

    /** El paciente de la cita o personal administrativo. */
    public static void exigirDuenioOAdministrativo(Cita cita, Actor actor) {
        boolean duenio = actor.esPaciente() && cita.pacienteId().equals(actor.usuarioId());
        if (!duenio && !actor.esAdministrativo()) {
            throw new AccesoProhibidoException("NO_ES_TU_CITA", "No puedes modificar una cita de otro paciente");
        }
    }

    public static void exigirAnticipacionParaReprogramar(long inicioMin, long ahoraMin, int horas) {
        exigirAnticipacion(inicioMin, ahoraMin, horas, "ANTICIPACION_INSUFICIENTE",
                "La cita solo se puede reprogramar con al menos " + horas + " horas de anticipacion");
    }

    public static void exigirDentroDelLimiteDeCancelacion(long inicioMin, long ahoraMin, int horas) {
        exigirAnticipacion(inicioMin, ahoraMin, horas, "FUERA_DEL_PLAZO_DE_CANCELACION",
                "La cita solo se puede cancelar hasta " + horas + " horas antes");
    }

    public static void exigirEstadoDeAtencion(EstadoCita nuevo) {
        if (nuevo != EstadoCita.COMPLETADA && nuevo != EstadoCita.NO_ASISTIDA) {
            throw new ValidacionException("ESTADO_NO_PERMITIDO", "Solo se puede marcar COMPLETADA o NO_ASISTIDA");
        }
    }

    public static void exigirProfesionalDeLaCita(Cita cita, String profesionalId) {
        if (!cita.profesionalId().equals(profesionalId)) {
            throw new AccesoProhibidoException("NO_ES_TU_CITA", "La cita es de otro profesional");
        }
    }

    private static void exigirAnticipacion(long inicioMin, long ahoraMin, int horas, String codigo, String mensaje) {
        if (inicioMin - ahoraMin < (long) horas * 60) {
            throw new ReglaNegocioException(codigo, mensaje);
        }
    }
}
