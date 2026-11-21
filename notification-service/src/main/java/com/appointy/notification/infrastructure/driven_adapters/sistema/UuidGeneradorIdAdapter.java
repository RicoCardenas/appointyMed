package com.appointy.notification.infrastructure.driven_adapters.sistema;

import com.appointy.notification.domain.gateway.GeneradorIdGateway;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidGeneradorIdAdapter implements GeneradorIdGateway {

    @Override
    public String nuevoId() {
        return UUID.randomUUID().toString();
    }
}
