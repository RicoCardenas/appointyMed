package com.appointy.auth.domain.port.out;

public interface CodificadorClavePort {

    String codificar(String claveEnClaro);

    boolean coincide(String claveEnClaro, String claveHash);
}
