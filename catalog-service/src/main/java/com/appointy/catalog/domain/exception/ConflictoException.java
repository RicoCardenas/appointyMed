package com.appointy.catalog.domain.exception;

/** El estado actual impide la operacion, p. ej. un duplicado (HTTP 409). */
public class ConflictoException extends DomainException {

    public ConflictoException(String codigo, String mensaje) {
        super(codigo, mensaje, null);
    }

    protected ConflictoException(String codigo, String mensaje, String[] detalle) {
        super(codigo, mensaje, detalle);
    }
}
