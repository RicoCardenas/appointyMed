package com.appointy.clinicalhistory.application.config;

import com.appointy.clinicalhistory.application.usecase.Auditoria;
import com.appointy.clinicalhistory.application.usecase.ConsultarHistoriaPacienteUseCaseImpl;
import com.appointy.clinicalhistory.application.usecase.ConsultarMiHistoriaUseCaseImpl;
import com.appointy.clinicalhistory.application.usecase.CorregirEntradaUseCaseImpl;
import com.appointy.clinicalhistory.application.usecase.GuardarEntradaTransaccion;
import com.appointy.clinicalhistory.application.usecase.RegistrarAtencionUseCaseImpl;
import com.appointy.clinicalhistory.domain.gateway.AppointmentClientGateway;
import com.appointy.clinicalhistory.domain.gateway.CatalogClientGateway;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.gateway.LogAccesoRepositoryGateway;
import com.appointy.clinicalhistory.domain.usecase.ConsultarHistoriaPacienteUseCase;
import com.appointy.clinicalhistory.domain.usecase.ConsultarMiHistoriaUseCase;
import com.appointy.clinicalhistory.domain.usecase.CorregirEntradaUseCase;
import com.appointy.clinicalhistory.domain.usecase.RegistrarAtencionUseCase;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    Auditoria auditoria(LogAccesoRepositoryGateway logs, Clock reloj) {
        return new Auditoria(logs, reloj);
    }

    @Bean
    GuardarEntradaTransaccion guardarEntradaTransaccion(EntradaClinicaRepositoryGateway entradas, LogAccesoRepositoryGateway logs, Auditoria auditoria) {
        return new GuardarEntradaTransaccion(entradas, logs, auditoria);
    }

    @Bean
    RegistrarAtencionUseCase registrarAtencion(CatalogClientGateway catalog, AppointmentClientGateway appointment, EntradaClinicaRepositoryGateway entradas,
            GuardarEntradaTransaccion transaccion, Auditoria auditoria, Clock reloj) {
        return new RegistrarAtencionUseCaseImpl(catalog, appointment, entradas, transaccion, auditoria, reloj);
    }

    @Bean
    ConsultarHistoriaPacienteUseCase consultarHistoriaPaciente(CatalogClientGateway catalog, AppointmentClientGateway appointment,
            EntradaClinicaRepositoryGateway entradas, Auditoria auditoria) {
        return new ConsultarHistoriaPacienteUseCaseImpl(catalog, appointment, entradas, auditoria);
    }

    @Bean
    ConsultarMiHistoriaUseCase consultarMiHistoria(EntradaClinicaRepositoryGateway entradas, Auditoria auditoria) {
        return new ConsultarMiHistoriaUseCaseImpl(entradas, auditoria);
    }

    @Bean
    CorregirEntradaUseCase corregirEntrada(CatalogClientGateway catalog, EntradaClinicaRepositoryGateway entradas, GuardarEntradaTransaccion transaccion,
            Auditoria auditoria, Clock reloj) {
        return new CorregirEntradaUseCaseImpl(catalog, entradas, transaccion, auditoria, reloj);
    }
}
