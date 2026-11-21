package com.appointy.appointment.infrastructure.driven_adapters.sistema;

import com.appointy.appointment.domain.gateway.GeneradorIdGateway;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidGeneradorIdAdapter implements GeneradorIdGateway {

    @Override
    public String nuevoId() {
        return UUID.randomUUID().toString();
    }
}
