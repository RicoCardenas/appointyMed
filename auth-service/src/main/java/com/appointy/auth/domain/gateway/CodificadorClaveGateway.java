package com.appointy.auth.domain.gateway;

public interface CodificadorClaveGateway {

    String codificar(String claveEnClaro);

    boolean coincide(String claveEnClaro, String claveHash);
}
