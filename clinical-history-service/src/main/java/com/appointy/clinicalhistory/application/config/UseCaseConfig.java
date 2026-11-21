package com.appointy.clinicalhistory.application.config;

import com.appointy.clinicalhistory.domain.gateway.AppointmentClientGateway;
import com.appointy.clinicalhistory.domain.gateway.CatalogClientGateway;
import com.appointy.clinicalhistory.domain.gateway.Cie10ClientGateway;
import com.appointy.clinicalhistory.domain.gateway.EntradaClinicaRepositoryGateway;
import com.appointy.clinicalhistory.domain.gateway.GeneradorIdGateway;
import com.appointy.clinicalhistory.domain.gateway.LogAccesoRepositoryGateway;
import com.appointy.clinicalhistory.domain.gateway.RegistroGateway;
import com.appointy.clinicalhistory.domain.gateway.RelojGateway;
import com.appointy.clinicalhistory.domain.gateway.TransactionGateway;
import com.appointy.clinicalhistory.domain.usecase.Auditoria;
import com.appointy.clinicalhistory.domain.usecase.EntradaClinicaUseCase;
import com.appointy.clinicalhistory.domain.usecase.GuardarEntradaTransaccion;
import com.appointy.clinicalhistory.domain.usecase.HistoriaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    Auditoria auditoria(LogAccesoRepositoryGateway logs, RelojGateway reloj, RegistroGateway registro, GeneradorIdGateway ids) {
        return new Auditoria(logs, reloj, registro, ids);
    }

    @Bean
    GuardarEntradaTransaccion guardarEntradaTransaccion(EntradaClinicaRepositoryGateway entradas, LogAccesoRepositoryGateway logs, Auditoria auditoria, TransactionGateway transacciones) {
        return new GuardarEntradaTransaccion(entradas, logs, auditoria, transacciones);
    }

    @Bean
    HistoriaUseCase historiaUseCase(CatalogClientGateway catalog, AppointmentClientGateway appointment, EntradaClinicaRepositoryGateway entradas, Auditoria auditoria, Cie10ClientGateway cie10) {
        return new HistoriaUseCase(catalog, appointment, entradas, auditoria, cie10);
    }

    @Bean
    EntradaClinicaUseCase entradaClinicaUseCase(CatalogClientGateway catalog, AppointmentClientGateway appointment, EntradaClinicaRepositoryGateway entradas, GuardarEntradaTransaccion transaccion, Auditoria auditoria, RelojGateway reloj, HistoriaUseCase historia, GeneradorIdGateway ids) {
        return new EntradaClinicaUseCase(catalog, appointment, entradas, transaccion, auditoria, reloj, historia, ids);
    }

}
