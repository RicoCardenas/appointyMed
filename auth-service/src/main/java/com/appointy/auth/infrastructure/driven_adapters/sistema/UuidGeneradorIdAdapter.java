package com.appointy.auth.infrastructure.driven_adapters.sistema;

import com.appointy.auth.domain.gateway.GeneradorIdGateway;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidGeneradorIdAdapter implements GeneradorIdGateway {

    @Override
    public String nuevoId() {
        return UUID.randomUUID().toString();
    }
}
