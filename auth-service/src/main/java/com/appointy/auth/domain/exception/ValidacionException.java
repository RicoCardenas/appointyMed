package com.appointy.auth.domain.exception;

/** Dato de entrada que incumple una regla de negocio (HTTP 400). */
public class ValidacionException extends DomainException {

    public ValidacionException(String codigo, String mensaje) {
        super(codigo, mensaje);
    }
}
