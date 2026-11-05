package com.appointy.appointment.application.config;

import com.appointy.appointment.application.usecase.BloquearFranjasUseCase;
import com.appointy.appointment.application.usecase.CancelarCitaTransaccion;
import com.appointy.appointment.application.usecase.CancelarCitaUseCase;
import com.appointy.appointment.application.usecase.ConsultarAgendaUseCase;
import com.appointy.appointment.application.usecase.ConsultarCitaUseCase;
import com.appointy.appointment.application.usecase.ConsultarDisponibilidadUseCase;
import com.appointy.appointment.application.usecase.ConsultarHistorialCitaUseCase;
import com.appointy.appointment.application.usecase.ConsultarMisCitasUseCase;
import com.appointy.appointment.application.usecase.GenerarFranjasUseCase;
import com.appointy.appointment.application.usecase.MarcarCitaAtendidaTransaccion;
import com.appointy.appointment.application.usecase.MarcarCitaAtendidaUseCase;
import com.appointy.appointment.application.usecase.RelojClinica;
import com.appointy.appointment.application.usecase.ReprogramarCitaTransaccion;
import com.appointy.appointment.application.usecase.ReprogramarCitaUseCase;
import com.appointy.appointment.application.usecase.ReservarCitaTransaccion;
import com.appointy.appointment.application.usecase.ReservarCitaUseCase;
import com.appointy.appointment.application.usecase.ValidarRelacionUseCase;
import com.appointy.appointment.domain.port.in.BloquearFranjasPort;
import com.appointy.appointment.domain.port.in.CancelarCitaPort;
import com.appointy.appointment.domain.port.in.ConsultarAgendaPort;
import com.appointy.appointment.domain.port.in.ConsultarCitaPort;
import com.appointy.appointment.domain.port.in.ConsultarDisponibilidadPort;
import com.appointy.appointment.domain.port.in.ConsultarHistorialCitaPort;
import com.appointy.appointment.domain.port.in.ConsultarMisCitasPort;
import com.appointy.appointment.domain.port.in.GenerarFranjasPort;
import com.appointy.appointment.domain.port.in.MarcarCitaAtendidaPort;
import com.appointy.appointment.domain.port.in.ReprogramarCitaPort;
import com.appointy.appointment.domain.port.in.ReservarCitaPort;
import com.appointy.appointment.domain.port.in.ValidarRelacionPort;
import com.appointy.appointment.domain.port.out.CatalogClientPort;
import com.appointy.appointment.domain.port.out.CitaRepositoryPort;
import com.appointy.appointment.domain.port.out.FranjaRepositoryPort;
import com.appointy.appointment.domain.port.out.HistorialCitaRepositoryPort;
import com.appointy.appointment.domain.port.out.PublicadorEventosPort;
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
    ConsultarDisponibilidadPort consultarDisponibilidad(FranjaRepositoryPort franjas, RelojClinica reloj) {
        return new ConsultarDisponibilidadUseCase(franjas, reloj);
    }

    @Bean
    ReservarCitaTransaccion reservarCitaTransaccion(FranjaRepositoryPort franjas, CitaRepositoryPort citas,
            HistorialCitaRepositoryPort historial, PublicadorEventosPort eventos, RelojClinica reloj) {
        return new ReservarCitaTransaccion(franjas, citas, historial, eventos, reloj);
    }

    @Bean
    ReservarCitaPort reservarCita(CatalogClientPort catalog, ReservarCitaTransaccion transaccion) {
        return new ReservarCitaUseCase(catalog, transaccion);
    }

    @Bean
    ReprogramarCitaTransaccion reprogramarCitaTransaccion(FranjaRepositoryPort franjas, CitaRepositoryPort citas,
            HistorialCitaRepositoryPort historial, PublicadorEventosPort eventos, RelojClinica reloj) {
        return new ReprogramarCitaTransaccion(franjas, citas, historial, eventos, reloj);
    }

    @Bean
    ReprogramarCitaPort reprogramarCita(CitaRepositoryPort citas, FranjaRepositoryPort franjas, CatalogClientPort catalog,
            ReprogramarCitaTransaccion transaccion) {
        return new ReprogramarCitaUseCase(citas, franjas, catalog, transaccion);
    }

    @Bean
    CancelarCitaTransaccion cancelarCitaTransaccion(FranjaRepositoryPort franjas, CitaRepositoryPort citas,
            HistorialCitaRepositoryPort historial, PublicadorEventosPort eventos, RelojClinica reloj) {
        return new CancelarCitaTransaccion(franjas, citas, historial, eventos, reloj);
    }

    @Bean
    CancelarCitaPort cancelarCita(CitaRepositoryPort citas, CatalogClientPort catalog, CancelarCitaTransaccion transaccion) {
        return new CancelarCitaUseCase(citas, catalog, transaccion);
    }

    @Bean
    ConsultarMisCitasPort consultarMisCitas(CitaRepositoryPort citas, FranjaRepositoryPort franjas, RelojClinica reloj) {
        return new ConsultarMisCitasUseCase(citas, franjas, reloj);
    }

    @Bean
    ConsultarAgendaPort consultarAgenda(CatalogClientPort catalog, CitaRepositoryPort citas, FranjaRepositoryPort franjas,
            RelojClinica reloj) {
        return new ConsultarAgendaUseCase(catalog, citas, franjas, reloj);
    }

    @Bean
    ConsultarCitaPort consultarCita(CatalogClientPort catalog, CitaRepositoryPort citas, FranjaRepositoryPort franjas) {
        return new ConsultarCitaUseCase(catalog, citas, franjas);
    }

    @Bean
    MarcarCitaAtendidaTransaccion marcarCitaAtendidaTransaccion(FranjaRepositoryPort franjas, CitaRepositoryPort citas,
            HistorialCitaRepositoryPort historial, RelojClinica reloj) {
        return new MarcarCitaAtendidaTransaccion(franjas, citas, historial, reloj);
    }

    @Bean
    MarcarCitaAtendidaPort marcarCitaAtendida(CatalogClientPort catalog, CitaRepositoryPort citas, MarcarCitaAtendidaTransaccion transaccion) {
        return new MarcarCitaAtendidaUseCase(catalog, citas, transaccion);
    }

    @Bean
    ConsultarHistorialCitaPort consultarHistorialCita(CatalogClientPort catalog, CitaRepositoryPort citas, HistorialCitaRepositoryPort historial) {
        return new ConsultarHistorialCitaUseCase(catalog, citas, historial);
    }

    @Bean
    GenerarFranjasPort generarFranjas(FranjaRepositoryPort franjas) {
        return new GenerarFranjasUseCase(franjas);
    }

    @Bean
    BloquearFranjasPort bloquearFranjas(FranjaRepositoryPort franjas, CitaRepositoryPort citas) {
        return new BloquearFranjasUseCase(franjas, citas);
    }

    @Bean
    ValidarRelacionPort validarRelacion(CitaRepositoryPort citas) {
        return new ValidarRelacionUseCase(citas);
    }
}
