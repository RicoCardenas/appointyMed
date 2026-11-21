package com.appointy.appointment.application.config;

import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.gateway.GeneradorIdGateway;
import com.appointy.appointment.domain.gateway.HistorialCitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.PublicadorEventosGateway;
import com.appointy.appointment.domain.gateway.RelojGateway;
import com.appointy.appointment.domain.gateway.TransactionGateway;
import com.appointy.appointment.domain.usecase.CancelarCitaTransaccion;
import com.appointy.appointment.domain.usecase.CitaUseCase;
import com.appointy.appointment.domain.usecase.ConsultaCitaUseCase;
import com.appointy.appointment.domain.usecase.FranjaUseCase;
import com.appointy.appointment.domain.usecase.MarcarCitaAtendidaTransaccion;
import com.appointy.appointment.domain.usecase.ReprogramarCitaTransaccion;
import com.appointy.appointment.domain.usecase.ReservarCitaTransaccion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    CitaUseCase citaUseCase(CatalogClientGateway catalog, ReservarCitaTransaccion reservarTransaccion, ReprogramarCitaTransaccion reprogramarTransaccion, CancelarCitaTransaccion cancelarTransaccion, MarcarCitaAtendidaTransaccion marcarTransaccion, CitaRepositoryGateway citas, FranjaRepositoryGateway franjas) {
        return new CitaUseCase(catalog, reservarTransaccion, reprogramarTransaccion, cancelarTransaccion, marcarTransaccion, citas, franjas);
    }

    @Bean
    ConsultaCitaUseCase consultaCitaUseCase(CatalogClientGateway catalog, CitaRepositoryGateway citas, FranjaRepositoryGateway franjas, RelojGateway reloj, HistorialCitaRepositoryGateway historial, TransactionGateway transacciones) {
        return new ConsultaCitaUseCase(catalog, citas, franjas, reloj, historial, transacciones);
    }

    @Bean
    FranjaUseCase franjaUseCase(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas, RelojGateway reloj, TransactionGateway transacciones, GeneradorIdGateway ids) {
        return new FranjaUseCase(franjas, citas, reloj, transacciones, ids);
    }

    @Bean
    ReservarCitaTransaccion reservarCitaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas, HistorialCitaRepositoryGateway historial, PublicadorEventosGateway eventos, RelojGateway reloj, TransactionGateway transacciones, GeneradorIdGateway ids) {
        return new ReservarCitaTransaccion(franjas, citas, historial, eventos, reloj, transacciones, ids);
    }

    @Bean
    ReprogramarCitaTransaccion reprogramarCitaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas, HistorialCitaRepositoryGateway historial, PublicadorEventosGateway eventos, RelojGateway reloj, TransactionGateway transacciones, GeneradorIdGateway ids) {
        return new ReprogramarCitaTransaccion(franjas, citas, historial, eventos, reloj, transacciones, ids);
    }

    @Bean
    CancelarCitaTransaccion cancelarCitaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas, HistorialCitaRepositoryGateway historial, PublicadorEventosGateway eventos, RelojGateway reloj, TransactionGateway transacciones, GeneradorIdGateway ids) {
        return new CancelarCitaTransaccion(franjas, citas, historial, eventos, reloj, transacciones, ids);
    }

    @Bean
    MarcarCitaAtendidaTransaccion marcarCitaAtendidaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas, HistorialCitaRepositoryGateway historial, RelojGateway reloj, TransactionGateway transacciones, GeneradorIdGateway ids) {
        return new MarcarCitaAtendidaTransaccion(franjas, citas, historial, reloj, transacciones, ids);
    }

}
