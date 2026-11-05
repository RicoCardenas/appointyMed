package com.appointy.catalog.application.config;

import com.appointy.catalog.application.usecase.ActualizarCentroUseCase;
import com.appointy.catalog.application.usecase.ActualizarEspecialidadUseCase;
import com.appointy.catalog.application.usecase.ActualizarProfesionalUseCase;
import com.appointy.catalog.application.usecase.AsociarCentrosUseCase;
import com.appointy.catalog.application.usecase.AsociarEspecialidadesUseCase;
import com.appointy.catalog.application.usecase.BloquearFranjasUseCase;
import com.appointy.catalog.application.usecase.ConfigurarParametrosUseCase;
import com.appointy.catalog.application.usecase.ConsultarParametrosUseCase;
import com.appointy.catalog.application.usecase.ConsultarProfesionalUseCase;
import com.appointy.catalog.application.usecase.CrearCentroUseCase;
import com.appointy.catalog.application.usecase.CrearEspecialidadUseCase;
import com.appointy.catalog.application.usecase.CrearProfesionalUseCase;
import com.appointy.catalog.application.usecase.DefinirHorarioUseCase;
import com.appointy.catalog.application.usecase.DesactivarCentroUseCase;
import com.appointy.catalog.application.usecase.DesactivarEspecialidadUseCase;
import com.appointy.catalog.application.usecase.DesactivarProfesionalUseCase;
import com.appointy.catalog.application.usecase.GenerarFranjasUseCase;
import com.appointy.catalog.application.usecase.ListarCentrosDeProfesionalUseCase;
import com.appointy.catalog.application.usecase.ListarEspecialidadesUseCase;
import com.appointy.catalog.application.usecase.ListarHorariosUseCase;
import com.appointy.catalog.application.usecase.ListarProfesionalesPorEspecialidadUseCase;
import com.appointy.catalog.application.usecase.ModificarHorarioUseCase;
import com.appointy.catalog.application.usecase.PublicadorDeFranjas;
import com.appointy.catalog.application.usecase.ValidarAsociacionUseCase;
import com.appointy.catalog.domain.port.in.ActualizarCentroPort;
import com.appointy.catalog.domain.port.in.ActualizarEspecialidadPort;
import com.appointy.catalog.domain.port.in.ActualizarProfesionalPort;
import com.appointy.catalog.domain.port.in.AsociarCentrosPort;
import com.appointy.catalog.domain.port.in.AsociarEspecialidadesPort;
import com.appointy.catalog.domain.port.in.BloquearFranjasPort;
import com.appointy.catalog.domain.port.in.ConfigurarParametrosPort;
import com.appointy.catalog.domain.port.in.ConsultarParametrosPort;
import com.appointy.catalog.domain.port.in.ConsultarProfesionalPort;
import com.appointy.catalog.domain.port.in.CrearCentroPort;
import com.appointy.catalog.domain.port.in.CrearEspecialidadPort;
import com.appointy.catalog.domain.port.in.CrearProfesionalPort;
import com.appointy.catalog.domain.port.in.DefinirHorarioPort;
import com.appointy.catalog.domain.port.in.DesactivarCentroPort;
import com.appointy.catalog.domain.port.in.DesactivarEspecialidadPort;
import com.appointy.catalog.domain.port.in.DesactivarProfesionalPort;
import com.appointy.catalog.domain.port.in.GenerarFranjasPort;
import com.appointy.catalog.domain.port.in.ListarCentrosDeProfesionalPort;
import com.appointy.catalog.domain.port.in.ListarEspecialidadesPort;
import com.appointy.catalog.domain.port.in.ListarHorariosPort;
import com.appointy.catalog.domain.port.in.ListarProfesionalesPorEspecialidadPort;
import com.appointy.catalog.domain.port.in.ModificarHorarioPort;
import com.appointy.catalog.domain.port.in.ValidarAsociacionPort;
import com.appointy.catalog.domain.port.out.AppointmentClientPort;
import com.appointy.catalog.domain.port.out.BloqueoRepositoryPort;
import com.appointy.catalog.domain.port.out.CentroRepositoryPort;
import com.appointy.catalog.domain.port.out.EspecialidadRepositoryPort;
import com.appointy.catalog.domain.port.out.HorarioRepositoryPort;
import com.appointy.catalog.domain.port.out.ParametroRepositoryPort;
import com.appointy.catalog.domain.port.out.ProfesionalRepositoryPort;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    CrearEspecialidadPort crearEspecialidad(EspecialidadRepositoryPort especialidades) {
        return new CrearEspecialidadUseCase(especialidades);
    }

    @Bean
    ActualizarEspecialidadPort actualizarEspecialidad(EspecialidadRepositoryPort especialidades) {
        return new ActualizarEspecialidadUseCase(especialidades);
    }

    @Bean
    DesactivarEspecialidadPort desactivarEspecialidad(EspecialidadRepositoryPort especialidades) {
        return new DesactivarEspecialidadUseCase(especialidades);
    }

    @Bean
    ListarEspecialidadesPort listarEspecialidades(EspecialidadRepositoryPort especialidades) {
        return new ListarEspecialidadesUseCase(especialidades);
    }

    @Bean
    CrearCentroPort crearCentro(CentroRepositoryPort centros) {
        return new CrearCentroUseCase(centros);
    }

    @Bean
    ActualizarCentroPort actualizarCentro(CentroRepositoryPort centros) {
        return new ActualizarCentroUseCase(centros);
    }

    @Bean
    DesactivarCentroPort desactivarCentro(CentroRepositoryPort centros) {
        return new DesactivarCentroUseCase(centros);
    }

    @Bean
    ListarCentrosDeProfesionalPort listarCentrosDeProfesional(ProfesionalRepositoryPort profesionales, CentroRepositoryPort centros) {
        return new ListarCentrosDeProfesionalUseCase(profesionales, centros);
    }

    @Bean
    CrearProfesionalPort crearProfesional(ProfesionalRepositoryPort profesionales) {
        return new CrearProfesionalUseCase(profesionales);
    }

    @Bean
    ActualizarProfesionalPort actualizarProfesional(ProfesionalRepositoryPort profesionales) {
        return new ActualizarProfesionalUseCase(profesionales);
    }

    @Bean
    DesactivarProfesionalPort desactivarProfesional(ProfesionalRepositoryPort profesionales) {
        return new DesactivarProfesionalUseCase(profesionales);
    }

    @Bean
    ListarProfesionalesPorEspecialidadPort listarProfesionalesPorEspecialidad(
            EspecialidadRepositoryPort especialidades, ProfesionalRepositoryPort profesionales) {
        return new ListarProfesionalesPorEspecialidadUseCase(especialidades, profesionales);
    }

    @Bean
    ConsultarProfesionalPort consultarProfesional(ProfesionalRepositoryPort profesionales) {
        return new ConsultarProfesionalUseCase(profesionales);
    }

    @Bean
    AsociarEspecialidadesPort asociarEspecialidades(ProfesionalRepositoryPort profesionales, EspecialidadRepositoryPort especialidades) {
        return new AsociarEspecialidadesUseCase(profesionales, especialidades);
    }

    @Bean
    AsociarCentrosPort asociarCentros(
            ProfesionalRepositoryPort profesionales, CentroRepositoryPort centros, HorarioRepositoryPort horarios, BloqueoRepositoryPort bloqueos) {
        return new AsociarCentrosUseCase(profesionales, centros, horarios, bloqueos);
    }

    @Bean
    PublicadorDeFranjas publicadorDeFranjas(ParametroRepositoryPort parametros, AppointmentClientPort appointment, Clock reloj) {
        return new PublicadorDeFranjas(parametros, appointment, reloj);
    }

    @Bean
    DefinirHorarioPort definirHorario(
            ProfesionalRepositoryPort profesionales,
            CentroRepositoryPort centros,
            HorarioRepositoryPort horarios,
            ParametroRepositoryPort parametros,
            PublicadorDeFranjas publicador) {
        return new DefinirHorarioUseCase(profesionales, centros, horarios, parametros, publicador);
    }

    @Bean
    ModificarHorarioPort modificarHorario(
            ProfesionalRepositoryPort profesionales,
            CentroRepositoryPort centros,
            HorarioRepositoryPort horarios,
            ParametroRepositoryPort parametros,
            PublicadorDeFranjas publicador) {
        return new ModificarHorarioUseCase(profesionales, centros, horarios, parametros, publicador);
    }

    @Bean
    GenerarFranjasPort generarFranjas(HorarioRepositoryPort horarios, PublicadorDeFranjas publicador) {
        return new GenerarFranjasUseCase(horarios, publicador);
    }

    @Bean
    ListarHorariosPort listarHorarios(ProfesionalRepositoryPort profesionales, HorarioRepositoryPort horarios) {
        return new ListarHorariosUseCase(profesionales, horarios);
    }

    @Bean
    ConsultarParametrosPort consultarParametros(ParametroRepositoryPort parametros) {
        return new ConsultarParametrosUseCase(parametros);
    }

    @Bean
    ConfigurarParametrosPort configurarParametros(ParametroRepositoryPort parametros) {
        return new ConfigurarParametrosUseCase(parametros);
    }

    @Bean
    BloquearFranjasPort bloquearFranjas(
            ProfesionalRepositoryPort profesionales,
            CentroRepositoryPort centros,
            BloqueoRepositoryPort bloqueos,
            AppointmentClientPort appointment,
            Clock reloj) {
        return new BloquearFranjasUseCase(profesionales, centros, bloqueos, appointment, reloj);
    }

    @Bean
    ValidarAsociacionPort validarAsociacion(ProfesionalRepositoryPort profesionales) {
        return new ValidarAsociacionUseCase(profesionales);
    }
}
