package com.appointy.catalog.domain.exception;

/** Base de los errores de negocio. {@code codigo} es estable para los clientes; {@code detalle} puede ir vacio. */
public abstract class DomainException extends RuntimeException {

    private final String codigo;
    private final String[] detalle;

    protected DomainException(String codigo, String mensaje, String[] detalle) {
        super(mensaje);
        this.codigo = codigo;
        this.detalle = detalle == null ? new String[0] : detalle;
    }

    public String codigo() {
        return codigo;
    }

    public String[] detalle() {
        return detalle;
    }
}
