package com.appointy.auth.application.config;

import com.appointy.auth.application.usecase.ActualizarDatosEditablesUseCase;
import com.appointy.auth.application.usecase.CerrarSesionUseCase;
import com.appointy.auth.application.usecase.ConsultarPerfilUseCase;
import com.appointy.auth.application.usecase.CorregirDatosInmutablesUseCase;
import com.appointy.auth.application.usecase.CrearCuentaStaffUseCase;
import com.appointy.auth.application.usecase.IniciarSesionUseCase;
import com.appointy.auth.application.usecase.LimpiarTokensVencidosUseCase;
import com.appointy.auth.application.usecase.RegistrarPacienteUseCase;
import com.appointy.auth.application.usecase.SembrarAdministradorUseCase;
import com.appointy.auth.domain.port.in.ActualizarDatosEditablesPort;
import com.appointy.auth.domain.port.in.CerrarSesionPort;
import com.appointy.auth.domain.port.in.ConsultarPerfilPort;
import com.appointy.auth.domain.port.in.CorregirDatosInmutablesPort;
import com.appointy.auth.domain.port.in.CrearCuentaStaffPort;
import com.appointy.auth.domain.port.in.IniciarSesionPort;
import com.appointy.auth.domain.port.in.LimpiarTokensVencidosPort;
import com.appointy.auth.domain.port.in.RegistrarPacientePort;
import com.appointy.auth.domain.port.in.SembrarAdministradorPort;
import com.appointy.auth.domain.port.out.CodificadorClavePort;
import com.appointy.auth.domain.port.out.EmisorTokenPort;
import com.appointy.auth.domain.port.out.PacientePerfilRepositoryPort;
import com.appointy.auth.domain.port.out.TokenRevocadoRepositoryPort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    RegistrarPacientePort registrarPaciente(
            UsuarioRepositoryPort usuarios, PacientePerfilRepositoryPort perfiles, CodificadorClavePort codificador, Clock reloj) {
        return new RegistrarPacienteUseCase(usuarios, perfiles, codificador, reloj);
    }

    @Bean
    IniciarSesionPort iniciarSesion(UsuarioRepositoryPort usuarios, CodificadorClavePort codificador, EmisorTokenPort emisor) {
        return new IniciarSesionUseCase(usuarios, codificador, emisor);
    }

    @Bean
    CerrarSesionPort cerrarSesion(TokenRevocadoRepositoryPort revocados) {
        return new CerrarSesionUseCase(revocados);
    }

    @Bean
    ConsultarPerfilPort consultarPerfil(UsuarioRepositoryPort usuarios, PacientePerfilRepositoryPort perfiles) {
        return new ConsultarPerfilUseCase(usuarios, perfiles);
    }

    @Bean
    ActualizarDatosEditablesPort actualizarDatosEditables(
            UsuarioRepositoryPort usuarios, PacientePerfilRepositoryPort perfiles, Clock reloj) {
        return new ActualizarDatosEditablesUseCase(usuarios, perfiles, reloj);
    }

    @Bean
    CrearCuentaStaffPort crearCuentaStaff(UsuarioRepositoryPort usuarios, CodificadorClavePort codificador, Clock reloj) {
        return new CrearCuentaStaffUseCase(usuarios, codificador, reloj);
    }

    @Bean
    CorregirDatosInmutablesPort corregirDatosInmutables(
            UsuarioRepositoryPort usuarios, PacientePerfilRepositoryPort perfiles, Clock reloj) {
        return new CorregirDatosInmutablesUseCase(usuarios, perfiles, reloj);
    }

    @Bean
    SembrarAdministradorPort sembrarAdministrador(UsuarioRepositoryPort usuarios, CrearCuentaStaffPort crearCuentaStaff) {
        return new SembrarAdministradorUseCase(usuarios, crearCuentaStaff);
    }

    @Bean
    LimpiarTokensVencidosPort limpiarTokensVencidos(TokenRevocadoRepositoryPort revocados, Clock reloj) {
        return new LimpiarTokensVencidosUseCase(revocados, reloj);
    }
}
