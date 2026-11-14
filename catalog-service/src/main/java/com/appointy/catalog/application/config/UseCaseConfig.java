package com.appointy.catalog.application.config;

import com.appointy.catalog.application.usecase.ActualizarCentroUseCaseImpl;
import com.appointy.catalog.application.usecase.ActualizarEspecialidadUseCaseImpl;
import com.appointy.catalog.application.usecase.ActualizarProfesionalUseCaseImpl;
import com.appointy.catalog.application.usecase.AsociarCentrosUseCaseImpl;
import com.appointy.catalog.application.usecase.AsociarEspecialidadesUseCaseImpl;
import com.appointy.catalog.application.usecase.BloquearFranjasUseCaseImpl;
import com.appointy.catalog.application.usecase.BuscarProfesionalPorUsuarioUseCaseImpl;
import com.appointy.catalog.application.usecase.ConfigurarParametrosUseCaseImpl;
import com.appointy.catalog.application.usecase.ConsultarParametrosUseCaseImpl;
import com.appointy.catalog.application.usecase.ConsultarProfesionalUseCaseImpl;
import com.appointy.catalog.application.usecase.CrearCentroUseCaseImpl;
import com.appointy.catalog.application.usecase.CrearEspecialidadUseCaseImpl;
import com.appointy.catalog.application.usecase.CrearProfesionalUseCaseImpl;
import com.appointy.catalog.application.usecase.DefinirHorarioUseCaseImpl;
import com.appointy.catalog.application.usecase.DesactivarCentroUseCaseImpl;
import com.appointy.catalog.application.usecase.DesactivarEspecialidadUseCaseImpl;
import com.appointy.catalog.application.usecase.DesactivarProfesionalUseCaseImpl;
import com.appointy.catalog.application.usecase.GenerarFranjasUseCaseImpl;
import com.appointy.catalog.application.usecase.ListarCentrosDeProfesionalUseCaseImpl;
import com.appointy.catalog.application.usecase.ListarEspecialidadesUseCaseImpl;
import com.appointy.catalog.application.usecase.ListarHorariosUseCaseImpl;
import com.appointy.catalog.application.usecase.ListarProfesionalesPorEspecialidadUseCaseImpl;
import com.appointy.catalog.application.usecase.ModificarHorarioUseCaseImpl;
import com.appointy.catalog.application.usecase.ConsultarFestivosUseCaseImpl;
import com.appointy.catalog.application.usecase.ListarProfesionalesUseCaseImpl;
import com.appointy.catalog.application.usecase.PublicadorDeFranjas;
import com.appointy.catalog.domain.gateway.FestivosClientGateway;
import com.appointy.catalog.domain.usecase.ConsultarFestivosUseCase;
import com.appointy.catalog.domain.usecase.ListarProfesionalesUseCase;
import com.appointy.catalog.application.usecase.ValidarAsociacionUseCaseImpl;
import com.appointy.catalog.domain.usecase.ActualizarCentroUseCase;
import com.appointy.catalog.domain.usecase.ActualizarEspecialidadUseCase;
import com.appointy.catalog.domain.usecase.ActualizarProfesionalUseCase;
import com.appointy.catalog.domain.usecase.AsociarCentrosUseCase;
import com.appointy.catalog.domain.usecase.AsociarEspecialidadesUseCase;
import com.appointy.catalog.domain.usecase.BloquearFranjasUseCase;
import com.appointy.catalog.domain.usecase.BuscarProfesionalPorUsuarioUseCase;
import com.appointy.catalog.domain.usecase.ConfigurarParametrosUseCase;
import com.appointy.catalog.domain.usecase.ConsultarParametrosUseCase;
import com.appointy.catalog.domain.usecase.ConsultarProfesionalUseCase;
import com.appointy.catalog.domain.usecase.CrearCentroUseCase;
import com.appointy.catalog.domain.usecase.CrearEspecialidadUseCase;
import com.appointy.catalog.domain.usecase.CrearProfesionalUseCase;
import com.appointy.catalog.domain.usecase.DefinirHorarioUseCase;
import com.appointy.catalog.domain.usecase.DesactivarCentroUseCase;
import com.appointy.catalog.domain.usecase.DesactivarEspecialidadUseCase;
import com.appointy.catalog.domain.usecase.DesactivarProfesionalUseCase;
import com.appointy.catalog.domain.usecase.GenerarFranjasUseCase;
import com.appointy.catalog.domain.usecase.ListarCentrosDeProfesionalUseCase;
import com.appointy.catalog.domain.usecase.ListarEspecialidadesUseCase;
import com.appointy.catalog.domain.usecase.ListarHorariosUseCase;
import com.appointy.catalog.domain.usecase.ListarProfesionalesPorEspecialidadUseCase;
import com.appointy.catalog.domain.usecase.ModificarHorarioUseCase;
import com.appointy.catalog.domain.usecase.ValidarAsociacionUseCase;
import com.appointy.catalog.domain.gateway.AppointmentClientGateway;
import com.appointy.catalog.domain.gateway.BloqueoRepositoryGateway;
import com.appointy.catalog.domain.gateway.CentroRepositoryGateway;
import com.appointy.catalog.domain.gateway.EspecialidadRepositoryGateway;
import com.appointy.catalog.domain.gateway.HorarioRepositoryGateway;
import com.appointy.catalog.domain.gateway.ParametroRepositoryGateway;
import com.appointy.catalog.domain.gateway.ProfesionalRepositoryGateway;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    CrearEspecialidadUseCase crearEspecialidad(EspecialidadRepositoryGateway especialidades) {
        return new CrearEspecialidadUseCaseImpl(especialidades);
    }

    @Bean
    ActualizarEspecialidadUseCase actualizarEspecialidad(EspecialidadRepositoryGateway especialidades) {
        return new ActualizarEspecialidadUseCaseImpl(especialidades);
    }

    @Bean
    DesactivarEspecialidadUseCase desactivarEspecialidad(EspecialidadRepositoryGateway especialidades) {
        return new DesactivarEspecialidadUseCaseImpl(especialidades);
    }

    @Bean
    ListarEspecialidadesUseCase listarEspecialidades(EspecialidadRepositoryGateway especialidades) {
        return new ListarEspecialidadesUseCaseImpl(especialidades);
    }

    @Bean
    CrearCentroUseCase crearCentro(CentroRepositoryGateway centros) {
        return new CrearCentroUseCaseImpl(centros);
    }

    @Bean
    ActualizarCentroUseCase actualizarCentro(CentroRepositoryGateway centros) {
        return new ActualizarCentroUseCaseImpl(centros);
    }

    @Bean
    DesactivarCentroUseCase desactivarCentro(CentroRepositoryGateway centros) {
        return new DesactivarCentroUseCaseImpl(centros);
    }

    @Bean
    ListarCentrosDeProfesionalUseCase listarCentrosDeProfesional(ProfesionalRepositoryGateway profesionales, CentroRepositoryGateway centros) {
        return new ListarCentrosDeProfesionalUseCaseImpl(profesionales, centros);
    }

    @Bean
    CrearProfesionalUseCase crearProfesional(ProfesionalRepositoryGateway profesionales) {
        return new CrearProfesionalUseCaseImpl(profesionales);
    }

    @Bean
    ActualizarProfesionalUseCase actualizarProfesional(ProfesionalRepositoryGateway profesionales) {
        return new ActualizarProfesionalUseCaseImpl(profesionales);
    }

    @Bean
    DesactivarProfesionalUseCase desactivarProfesional(ProfesionalRepositoryGateway profesionales) {
        return new DesactivarProfesionalUseCaseImpl(profesionales);
    }

    @Bean
    ListarProfesionalesPorEspecialidadUseCase listarProfesionalesPorEspecialidad(
            EspecialidadRepositoryGateway especialidades, ProfesionalRepositoryGateway profesionales) {
        return new ListarProfesionalesPorEspecialidadUseCaseImpl(especialidades, profesionales);
    }

    @Bean
    ConsultarProfesionalUseCase consultarProfesional(ProfesionalRepositoryGateway profesionales) {
        return new ConsultarProfesionalUseCaseImpl(profesionales);
    }

    @Bean
    AsociarEspecialidadesUseCase asociarEspecialidades(ProfesionalRepositoryGateway profesionales, EspecialidadRepositoryGateway especialidades) {
        return new AsociarEspecialidadesUseCaseImpl(profesionales, especialidades);
    }

    @Bean
    AsociarCentrosUseCase asociarCentros(
            ProfesionalRepositoryGateway profesionales, CentroRepositoryGateway centros, HorarioRepositoryGateway horarios, BloqueoRepositoryGateway bloqueos) {
        return new AsociarCentrosUseCaseImpl(profesionales, centros, horarios, bloqueos);
    }

    @Bean
    PublicadorDeFranjas publicadorDeFranjas(ParametroRepositoryGateway parametros, AppointmentClientGateway appointment, FestivosClientGateway festivos, Clock reloj) {
        return new PublicadorDeFranjas(parametros, appointment, festivos, reloj);
    }

    @Bean
    DefinirHorarioUseCase definirHorario(
            ProfesionalRepositoryGateway profesionales,
            CentroRepositoryGateway centros,
            HorarioRepositoryGateway horarios,
            ParametroRepositoryGateway parametros,
            PublicadorDeFranjas publicador) {
        return new DefinirHorarioUseCaseImpl(profesionales, centros, horarios, parametros, publicador);
    }

    @Bean
    ModificarHorarioUseCase modificarHorario(
            ProfesionalRepositoryGateway profesionales,
            CentroRepositoryGateway centros,
            HorarioRepositoryGateway horarios,
            ParametroRepositoryGateway parametros,
            PublicadorDeFranjas publicador) {
        return new ModificarHorarioUseCaseImpl(profesionales, centros, horarios, parametros, publicador);
    }

    @Bean
    GenerarFranjasUseCase generarFranjas(HorarioRepositoryGateway horarios, PublicadorDeFranjas publicador) {
        return new GenerarFranjasUseCaseImpl(horarios, publicador);
    }

    @Bean
    ListarHorariosUseCase listarHorarios(ProfesionalRepositoryGateway profesionales, HorarioRepositoryGateway horarios) {
        return new ListarHorariosUseCaseImpl(profesionales, horarios);
    }

    @Bean
    ConsultarParametrosUseCase consultarParametros(ParametroRepositoryGateway parametros) {
        return new ConsultarParametrosUseCaseImpl(parametros);
    }

    @Bean
    ConfigurarParametrosUseCase configurarParametros(ParametroRepositoryGateway parametros) {
        return new ConfigurarParametrosUseCaseImpl(parametros);
    }

    @Bean
    BloquearFranjasUseCase bloquearFranjas(
            ProfesionalRepositoryGateway profesionales,
            CentroRepositoryGateway centros,
            BloqueoRepositoryGateway bloqueos,
            AppointmentClientGateway appointment,
            Clock reloj) {
        return new BloquearFranjasUseCaseImpl(profesionales, centros, bloqueos, appointment, reloj);
    }

    @Bean
    ValidarAsociacionUseCase validarAsociacion(ProfesionalRepositoryGateway profesionales) {
        return new ValidarAsociacionUseCaseImpl(profesionales);
    }

    @Bean
    BuscarProfesionalPorUsuarioUseCase buscarProfesionalPorUsuario(ProfesionalRepositoryGateway profesionales) {
        return new BuscarProfesionalPorUsuarioUseCaseImpl(profesionales);
    }

    @Bean
    ListarProfesionalesUseCase listarProfesionales(ProfesionalRepositoryGateway profesionales) {
        return new ListarProfesionalesUseCaseImpl(profesionales);
    }

    @Bean
    ConsultarFestivosUseCase consultarFestivos(FestivosClientGateway festivos) {
        return new ConsultarFestivosUseCaseImpl(festivos);
    }
}
