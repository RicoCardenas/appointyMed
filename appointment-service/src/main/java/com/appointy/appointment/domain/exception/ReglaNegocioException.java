package com.appointy.appointment.domain.exception;

/** Se incumple una regla de negocio con la entrada ya valida (HTTP 422). */
public class ReglaNegocioException extends DomainException {

    public ReglaNegocioException(String codigo, String mensaje) {
        super(codigo, mensaje, null);
    }
}
