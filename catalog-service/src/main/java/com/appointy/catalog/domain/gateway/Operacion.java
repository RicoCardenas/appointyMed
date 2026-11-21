package com.appointy.catalog.domain.gateway;

/** Trabajo que se ejecuta dentro de una transaccion; el dominio no conoce el mecanismo. */
@FunctionalInterface
public interface Operacion<T> {

    T ejecutar();
}
