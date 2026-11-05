package com.appointy.appointment.infrastructure.driven_adapters.events;

import com.appointy.appointment.domain.model.EventoCita;
import com.appointy.appointment.domain.port.out.PublicadorEventosPort;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class SpringEventoCitaPublisher implements PublicadorEventosPort {

    private final ApplicationEventPublisher publicador;

    public SpringEventoCitaPublisher(ApplicationEventPublisher publicador) {
        this.publicador = publicador;
    }

    @Override
    public void publicar(EventoCita evento) {
        publicador.publishEvent(evento);
    }
}
