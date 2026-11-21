package com.appointy.notification.application.config;

import com.appointy.notification.domain.gateway.CorreoGateway;
import com.appointy.notification.domain.gateway.GeneradorIdGateway;
import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.gateway.RelojGateway;
import com.appointy.notification.domain.gateway.UsuarioClientGateway;
import com.appointy.notification.domain.usecase.ConsultaNotificacionUseCase;
import com.appointy.notification.domain.usecase.Despachador;
import com.appointy.notification.domain.usecase.NotificacionUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    NotificacionUseCase notificacionUseCase(NotificacionRepositoryGateway notificaciones, UsuarioClientGateway usuarios, Despachador despachador, RelojGateway reloj, @Value("${app.notification.lote:100}") int lote,
            @Value("${app.notification.max-intentos:3}") int maxIntentos, GeneradorIdGateway ids) {
        return new NotificacionUseCase(notificaciones, usuarios, despachador, reloj, lote, maxIntentos, ids);
    }

    @Bean
    ConsultaNotificacionUseCase consultaNotificacionUseCase(NotificacionRepositoryGateway notificaciones) {
        return new ConsultaNotificacionUseCase(notificaciones);
    }

    @Bean
    Despachador despachador(CorreoGateway correo, NotificacionRepositoryGateway notificaciones, RelojGateway reloj) {
        return new Despachador(correo, notificaciones, reloj);
    }

}
