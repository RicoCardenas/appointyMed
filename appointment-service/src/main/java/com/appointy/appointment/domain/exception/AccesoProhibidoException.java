package com.appointy.appointment.domain.exception;

/** El usuario autenticado no puede actuar sobre ese recurso (HTTP 403). */
public class AccesoProhibidoException extends DomainException {

    public AccesoProhibidoException(String codigo, String mensaje) {
        super(codigo, mensaje, null);
    }
}
