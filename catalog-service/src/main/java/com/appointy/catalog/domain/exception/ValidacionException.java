package com.appointy.catalog.domain.exception;

/** Dato que incumple una regla de negocio (HTTP 400). */
public class ValidacionException extends DomainException {

    public ValidacionException(String codigo, String mensaje) {
        super(codigo, mensaje, null);
    }
}
