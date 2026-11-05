package com.appointy.auth.infrastructure.security;

import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptCodificadorAdapter implements CodificadorClaveGateway {

    private final BCryptPasswordEncoder codificador = new BCryptPasswordEncoder(12);

    @Override
    public String codificar(String claveEnClaro) {
        return codificador.encode(claveEnClaro);
    }

    @Override
    public boolean coincide(String claveEnClaro, String claveHash) {
        return codificador.matches(claveEnClaro, claveHash);
    }
}
