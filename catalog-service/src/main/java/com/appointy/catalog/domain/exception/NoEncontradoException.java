package com.appointy.catalog.domain.exception;

/** El recurso pedido no existe (HTTP 404). */
public class NoEncontradoException extends DomainException {

    public NoEncontradoException(String codigo, String mensaje) {
        super(codigo, mensaje, null);
    }
}
