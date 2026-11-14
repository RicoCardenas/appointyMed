package com.appointy.notification.application.config;

import com.appointy.notification.application.usecase.ConsultarEstadisticasUseCaseImpl;
import com.appointy.notification.application.usecase.Despachador;
import com.appointy.notification.application.usecase.EnviarRecordatoriosUseCaseImpl;
import com.appointy.notification.application.usecase.ListarNotificacionesDeCitaUseCaseImpl;
import com.appointy.notification.application.usecase.ListarNotificacionesUseCaseImpl;
import com.appointy.notification.application.usecase.NotificarCitaUseCaseImpl;
import com.appointy.notification.application.usecase.ReenviarNotificacionUseCaseImpl;
import com.appointy.notification.application.usecase.ReintentarFallidasUseCaseImpl;
import com.appointy.notification.domain.gateway.CorreoGateway;
import com.appointy.notification.domain.gateway.NotificacionRepositoryGateway;
import com.appointy.notification.domain.gateway.UsuarioClientGateway;
import com.appointy.notification.domain.usecase.ConsultarEstadisticasUseCase;
import com.appointy.notification.domain.usecase.EnviarRecordatoriosUseCase;
import com.appointy.notification.domain.usecase.ListarNotificacionesDeCitaUseCase;
import com.appointy.notification.domain.usecase.ListarNotificacionesUseCase;
import com.appointy.notification.domain.usecase.NotificarCitaUseCase;
import com.appointy.notification.domain.usecase.ReenviarNotificacionUseCase;
import com.appointy.notification.domain.usecase.ReintentarFallidasUseCase;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    Despachador despachador(CorreoGateway correo, NotificacionRepositoryGateway notificaciones, Clock reloj) {
        return new Despachador(correo, notificaciones, reloj);
    }

    @Bean
    NotificarCitaUseCase notificarCita(NotificacionRepositoryGateway notificaciones, UsuarioClientGateway usuarios, Despachador despachador,
            Clock reloj, ZoneId zona) {
        return new NotificarCitaUseCaseImpl(notificaciones, usuarios, despachador, reloj, zona);
    }

    @Bean
    EnviarRecordatoriosUseCase enviarRecordatorios(NotificacionRepositoryGateway notificaciones, Despachador despachador, Clock reloj,
            @Value("${app.notification.lote:100}") int lote) {
        return new EnviarRecordatoriosUseCaseImpl(notificaciones, despachador, reloj, lote);
    }

    @Bean
    ReintentarFallidasUseCase reintentarFallidas(NotificacionRepositoryGateway notificaciones, Despachador despachador,
            @Value("${app.notification.max-intentos:3}") int maxIntentos, @Value("${app.notification.lote:100}") int lote) {
        return new ReintentarFallidasUseCaseImpl(notificaciones, despachador, maxIntentos, lote);
    }

    @Bean
    ListarNotificacionesUseCase listarNotificaciones(NotificacionRepositoryGateway notificaciones) {
        return new ListarNotificacionesUseCaseImpl(notificaciones);
    }

    @Bean
    ListarNotificacionesDeCitaUseCase listarNotificacionesDeCita(NotificacionRepositoryGateway notificaciones) {
        return new ListarNotificacionesDeCitaUseCaseImpl(notificaciones);
    }

    @Bean
    ReenviarNotificacionUseCase reenviarNotificacion(NotificacionRepositoryGateway notificaciones, Despachador despachador) {
        return new ReenviarNotificacionUseCaseImpl(notificaciones, despachador);
    }

    @Bean
    ConsultarEstadisticasUseCase consultarEstadisticas(NotificacionRepositoryGateway notificaciones) {
        return new ConsultarEstadisticasUseCaseImpl(notificaciones);
    }
}
