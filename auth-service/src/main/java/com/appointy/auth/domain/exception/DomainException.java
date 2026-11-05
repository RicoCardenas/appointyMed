package com.appointy.auth.domain.exception;

/** Base de los errores de negocio. {@code codigo} es estable para los clientes de la API. */
public abstract class DomainException extends RuntimeException {

    private final String codigo;

    protected DomainException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
