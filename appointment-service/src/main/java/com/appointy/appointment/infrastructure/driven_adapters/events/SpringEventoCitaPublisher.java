package com.appointy.appointment.infrastructure.driven_adapters.events;

import com.appointy.appointment.domain.model.EventoCita;
import com.appointy.appointment.domain.gateway.PublicadorEventosGateway;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventoCitaPublisher implements PublicadorEventosGateway {

    private final ApplicationEventPublisher publicador;

    public SpringEventoCitaPublisher(ApplicationEventPublisher publicador) {
        this.publicador = publicador;
    }

    @Override
    public void publicar(EventoCita evento) {
        publicador.publishEvent(evento);
    }
}
