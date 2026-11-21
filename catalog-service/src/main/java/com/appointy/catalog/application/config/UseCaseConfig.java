package com.appointy.catalog.application.config;

import com.appointy.catalog.domain.gateway.AppointmentClientGateway;
import com.appointy.catalog.domain.gateway.BloqueoRepositoryGateway;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.gateway.EspecialidadRepositoryGateway;
import com.appointy.catalog.domain.gateway.FestivosClientGateway;
import com.appointy.catalog.domain.gateway.GeneradorIdGateway;
import com.appointy.catalog.domain.gateway.HorarioRepositoryGateway;
import com.appointy.catalog.domain.gateway.ParametroRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import com.appointy.catalog.domain.gateway.RegistroGateway;
import com.appointy.catalog.domain.gateway.RelojGateway;
import com.appointy.catalog.domain.gateway.TransactionGateway;
import com.appointy.catalog.domain.usecase.CentroUseCase;
import com.appointy.catalog.domain.usecase.EspecialidadUseCase;
import com.appointy.catalog.domain.usecase.HorarioUseCase;
import com.appointy.catalog.domain.usecase.ParametroUseCase;
import com.appointy.catalog.domain.usecase.ProfesionalUseCase;
import com.appointy.catalog.domain.usecase.PublicadorDeFranjas;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    EspecialidadUseCase especialidadUseCase(EspecialidadRepositoryGateway especialidades, TransactionGateway transacciones, GeneradorIdGateway ids) {
        return new EspecialidadUseCase(especialidades, transacciones, ids);
    }

    @Bean
    CentroUseCase centroUseCase(CentroRepositoryGateway centros, ProfesionalRepositoryGateway profesionales, TransactionGateway transacciones, GeneradorIdGateway ids) {
        return new CentroUseCase(centros, profesionales, transacciones, ids);
    }

    @Bean
    ProfesionalUseCase profesionalUseCase(ProfesionalRepositoryGateway profesionales, EspecialidadRepositoryGateway especialidades, CentroRepositoryGateway centros, HorarioRepositoryGateway horarios, BloqueoRepositoryGateway bloqueos, TransactionGateway transacciones, GeneradorIdGateway ids) {
        return new ProfesionalUseCase(profesionales, especialidades, centros, horarios, bloqueos, transacciones, ids);
    }

    @Bean
    PublicadorDeFranjas publicadorDeFranjas(ParametroRepositoryGateway parametros, AppointmentClientGateway appointment, FestivosClientGateway festivos, RelojGateway reloj, RegistroGateway registro) {
        return new PublicadorDeFranjas(parametros, appointment, festivos, reloj, registro);
    }

    @Bean
    HorarioUseCase horarioUseCase(ProfesionalRepositoryGateway profesionales, CentroRepositoryGateway centros, HorarioRepositoryGateway horarios, ParametroRepositoryGateway parametros, PublicadorDeFranjas publicador, BloqueoRepositoryGateway bloqueos, AppointmentClientGateway appointment, RelojGateway reloj, TransactionGateway transacciones, GeneradorIdGateway ids) {
        return new HorarioUseCase(profesionales, centros, horarios, parametros, publicador, bloqueos, appointment, reloj, transacciones, ids);
    }

    @Bean
    ParametroUseCase parametroUseCase(ParametroRepositoryGateway parametros, FestivosClientGateway festivos, TransactionGateway transacciones) {
        return new ParametroUseCase(parametros, festivos, transacciones);
    }

}
