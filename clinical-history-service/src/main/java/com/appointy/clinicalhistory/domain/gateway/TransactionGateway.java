package com.appointy.clinicalhistory.domain.gateway;

/** Puerto de salida: delimita una transaccion. Si la operacion lanza una excepcion, se revierte. */
public interface TransactionGateway {

    <T> T ejecutar(Operacion<T> operacion);

    <T> T ejecutarSoloLectura(Operacion<T> operacion);
}
