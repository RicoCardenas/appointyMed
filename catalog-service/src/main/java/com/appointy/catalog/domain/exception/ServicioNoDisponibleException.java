package com.appointy.catalog.domain.exception;

/** Otro servicio no respondio a tiempo o fallo (HTTP 503); no se guardo nada que dependa de el. */
public class ServicioNoDisponibleException extends DomainException {

    public ServicioNoDisponibleException(String codigo, String mensaje) {
        super(codigo, mensaje, null);
    }
}
