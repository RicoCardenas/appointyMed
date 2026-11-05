package com.appointy.appointment.application.config;

import com.appointy.appointment.application.usecase.BloquearFranjasUseCaseImpl;
import com.appointy.appointment.application.usecase.CancelarCitaTransaccion;
import com.appointy.appointment.application.usecase.CancelarCitaUseCaseImpl;
import com.appointy.appointment.application.usecase.ConsultarAgendaUseCaseImpl;
import com.appointy.appointment.application.usecase.ConsultarCitaUseCaseImpl;
import com.appointy.appointment.application.usecase.ConsultarDisponibilidadUseCaseImpl;
import com.appointy.appointment.application.usecase.ConsultarHistorialCitaUseCaseImpl;
import com.appointy.appointment.application.usecase.ConsultarMisCitasUseCaseImpl;
import com.appointy.appointment.application.usecase.GenerarFranjasUseCaseImpl;
import com.appointy.appointment.application.usecase.MarcarCitaAtendidaTransaccion;
import com.appointy.appointment.application.usecase.MarcarCitaAtendidaUseCaseImpl;
import com.appointy.appointment.application.usecase.RelojClinica;
import com.appointy.appointment.application.usecase.ReprogramarCitaTransaccion;
import com.appointy.appointment.application.usecase.ReprogramarCitaUseCaseImpl;
import com.appointy.appointment.application.usecase.ReservarCitaTransaccion;
import com.appointy.appointment.application.usecase.ReservarCitaUseCaseImpl;
import com.appointy.appointment.application.usecase.ValidarRelacionUseCaseImpl;
import com.appointy.appointment.domain.usecase.BloquearFranjasUseCase;
import com.appointy.appointment.domain.usecase.CancelarCitaUseCase;
import com.appointy.appointment.domain.usecase.ConsultarAgendaUseCase;
import com.appointy.appointment.domain.usecase.ConsultarCitaUseCase;
import com.appointy.appointment.domain.usecase.ConsultarDisponibilidadUseCase;
import com.appointy.appointment.domain.usecase.ConsultarHistorialCitaUseCase;
import com.appointy.appointment.domain.usecase.ConsultarMisCitasUseCase;
import com.appointy.appointment.domain.usecase.GenerarFranjasUseCase;
import com.appointy.appointment.domain.usecase.MarcarCitaAtendidaUseCase;
import com.appointy.appointment.domain.usecase.ReprogramarCitaUseCase;
import com.appointy.appointment.domain.usecase.ReservarCitaUseCase;
import com.appointy.appointment.domain.usecase.ValidarRelacionUseCase;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.gateway.HistorialCitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.PublicadorEventosGateway;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    RelojClinica relojClinica(Clock reloj) {
        return new RelojClinica(reloj);
    }

    @Bean
    ConsultarDisponibilidadUseCase consultarDisponibilidad(FranjaRepositoryGateway franjas, RelojClinica reloj) {
        return new ConsultarDisponibilidadUseCaseImpl(franjas, reloj);
    }

    @Bean
    ReservarCitaTransaccion reservarCitaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas,
            HistorialCitaRepositoryGateway historial, PublicadorEventosGateway eventos, RelojClinica reloj) {
        return new ReservarCitaTransaccion(franjas, citas, historial, eventos, reloj);
    }

    @Bean
    ReservarCitaUseCase reservarCita(CatalogClientGateway catalog, ReservarCitaTransaccion transaccion) {
        return new ReservarCitaUseCaseImpl(catalog, transaccion);
    }

    @Bean
    ReprogramarCitaTransaccion reprogramarCitaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas,
            HistorialCitaRepositoryGateway historial, PublicadorEventosGateway eventos, RelojClinica reloj) {
        return new ReprogramarCitaTransaccion(franjas, citas, historial, eventos, reloj);
    }

    @Bean
    ReprogramarCitaUseCase reprogramarCita(CitaRepositoryGateway citas, FranjaRepositoryGateway franjas, CatalogClientGateway catalog,
            ReprogramarCitaTransaccion transaccion) {
        return new ReprogramarCitaUseCaseImpl(citas, franjas, catalog, transaccion);
    }

    @Bean
    CancelarCitaTransaccion cancelarCitaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas,
            HistorialCitaRepositoryGateway historial, PublicadorEventosGateway eventos, RelojClinica reloj) {
        return new CancelarCitaTransaccion(franjas, citas, historial, eventos, reloj);
    }

    @Bean
    CancelarCitaUseCase cancelarCita(CitaRepositoryGateway citas, CatalogClientGateway catalog, CancelarCitaTransaccion transaccion) {
        return new CancelarCitaUseCaseImpl(citas, catalog, transaccion);
    }

    @Bean
    ConsultarMisCitasUseCase consultarMisCitas(CitaRepositoryGateway citas, FranjaRepositoryGateway franjas, RelojClinica reloj) {
        return new ConsultarMisCitasUseCaseImpl(citas, franjas, reloj);
    }

    @Bean
    ConsultarAgendaUseCase consultarAgenda(CatalogClientGateway catalog, CitaRepositoryGateway citas, FranjaRepositoryGateway franjas,
            RelojClinica reloj) {
        return new ConsultarAgendaUseCaseImpl(catalog, citas, franjas, reloj);
    }

    @Bean
    ConsultarCitaUseCase consultarCita(CatalogClientGateway catalog, CitaRepositoryGateway citas, FranjaRepositoryGateway franjas) {
        return new ConsultarCitaUseCaseImpl(catalog, citas, franjas);
    }

    @Bean
    MarcarCitaAtendidaTransaccion marcarCitaAtendidaTransaccion(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas,
            HistorialCitaRepositoryGateway historial, RelojClinica reloj) {
        return new MarcarCitaAtendidaTransaccion(franjas, citas, historial, reloj);
    }

    @Bean
    MarcarCitaAtendidaUseCase marcarCitaAtendida(CatalogClientGateway catalog, CitaRepositoryGateway citas, MarcarCitaAtendidaTransaccion transaccion) {
        return new MarcarCitaAtendidaUseCaseImpl(catalog, citas, transaccion);
    }

    @Bean
    ConsultarHistorialCitaUseCase consultarHistorialCita(CatalogClientGateway catalog, CitaRepositoryGateway citas, HistorialCitaRepositoryGateway historial) {
        return new ConsultarHistorialCitaUseCaseImpl(catalog, citas, historial);
    }

    @Bean
    GenerarFranjasUseCase generarFranjas(FranjaRepositoryGateway franjas) {
        return new GenerarFranjasUseCaseImpl(franjas);
    }

    @Bean
    BloquearFranjasUseCase bloquearFranjas(FranjaRepositoryGateway franjas, CitaRepositoryGateway citas) {
        return new BloquearFranjasUseCaseImpl(franjas, citas);
    }

    @Bean
    ValidarRelacionUseCase validarRelacion(CitaRepositoryGateway citas) {
        return new ValidarRelacionUseCaseImpl(citas);
    }
}
