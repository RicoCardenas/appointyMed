package com.appointy.auth.application.config;

import com.appointy.auth.application.usecase.ActualizarDatosEditablesUseCaseImpl;
import com.appointy.auth.application.usecase.CerrarSesionUseCaseImpl;
import com.appointy.auth.application.usecase.ConsultarContactoUseCaseImpl;
import com.appointy.auth.application.usecase.CambiarClaveUseCaseImpl;
import com.appointy.auth.application.usecase.CambiarEstadoCuentaUseCaseImpl;
import com.appointy.auth.application.usecase.ConsultarEstadisticasUsuariosUseCaseImpl;
import com.appointy.auth.application.usecase.ListarUsuariosUseCaseImpl;
import com.appointy.auth.application.usecase.ConsultarPerfilUseCaseImpl;
import com.appointy.auth.application.usecase.CorregirDatosInmutablesUseCaseImpl;
import com.appointy.auth.application.usecase.CrearCuentaStaffUseCaseImpl;
import com.appointy.auth.application.usecase.IniciarSesionUseCaseImpl;
import com.appointy.auth.application.usecase.LimpiarTokensVencidosUseCaseImpl;
import com.appointy.auth.application.usecase.RegistrarPacienteUseCaseImpl;
import com.appointy.auth.application.usecase.SembrarAdministradorUseCaseImpl;
import com.appointy.auth.domain.usecase.ActualizarDatosEditablesUseCase;
import com.appointy.auth.domain.usecase.CerrarSesionUseCase;
import com.appointy.auth.domain.usecase.ConsultarContactoUseCase;
import com.appointy.auth.domain.usecase.CambiarClaveUseCase;
import com.appointy.auth.domain.usecase.CambiarEstadoCuentaUseCase;
import com.appointy.auth.domain.usecase.ConsultarEstadisticasUsuariosUseCase;
import com.appointy.auth.domain.usecase.ListarUsuariosUseCase;
import com.appointy.auth.domain.usecase.ConsultarPerfilUseCase;
import com.appointy.auth.domain.usecase.CorregirDatosInmutablesUseCase;
import com.appointy.auth.domain.usecase.CrearCuentaStaffUseCase;
import com.appointy.auth.domain.usecase.IniciarSesionUseCase;
import com.appointy.auth.domain.usecase.LimpiarTokensVencidosUseCase;
import com.appointy.auth.domain.usecase.RegistrarPacienteUseCase;
import com.appointy.auth.domain.usecase.SembrarAdministradorUseCase;
import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import com.appointy.auth.domain.gateway.EmisorTokenGateway;
import com.appointy.auth.domain.gateway.PacientePerfilRepositoryGateway;
import com.appointy.auth.domain.gateway.TokenRevocadoRepositoryGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    RegistrarPacienteUseCase registrarPaciente(
            UsuarioRepositoryGateway usuarios, PacientePerfilRepositoryGateway perfiles, CodificadorClaveGateway codificador, Clock reloj) {
        return new RegistrarPacienteUseCaseImpl(usuarios, perfiles, codificador, reloj);
    }

    @Bean
    IniciarSesionUseCase iniciarSesion(UsuarioRepositoryGateway usuarios, CodificadorClaveGateway codificador, EmisorTokenGateway emisor) {
        return new IniciarSesionUseCaseImpl(usuarios, codificador, emisor);
    }

    @Bean
    CerrarSesionUseCase cerrarSesion(TokenRevocadoRepositoryGateway revocados) {
        return new CerrarSesionUseCaseImpl(revocados);
    }

    @Bean
    ConsultarPerfilUseCase consultarPerfil(UsuarioRepositoryGateway usuarios, PacientePerfilRepositoryGateway perfiles) {
        return new ConsultarPerfilUseCaseImpl(usuarios, perfiles);
    }

    @Bean
    ActualizarDatosEditablesUseCase actualizarDatosEditables(
            UsuarioRepositoryGateway usuarios, PacientePerfilRepositoryGateway perfiles, Clock reloj) {
        return new ActualizarDatosEditablesUseCaseImpl(usuarios, perfiles, reloj);
    }

    @Bean
    CrearCuentaStaffUseCase crearCuentaStaff(UsuarioRepositoryGateway usuarios, CodificadorClaveGateway codificador, Clock reloj) {
        return new CrearCuentaStaffUseCaseImpl(usuarios, codificador, reloj);
    }

    @Bean
    CorregirDatosInmutablesUseCase corregirDatosInmutables(
            UsuarioRepositoryGateway usuarios, PacientePerfilRepositoryGateway perfiles, Clock reloj) {
        return new CorregirDatosInmutablesUseCaseImpl(usuarios, perfiles, reloj);
    }

    @Bean
    SembrarAdministradorUseCase sembrarAdministrador(UsuarioRepositoryGateway usuarios, CrearCuentaStaffUseCase crearCuentaStaff) {
        return new SembrarAdministradorUseCaseImpl(usuarios, crearCuentaStaff);
    }

    @Bean
    LimpiarTokensVencidosUseCase limpiarTokensVencidos(TokenRevocadoRepositoryGateway revocados, Clock reloj) {
        return new LimpiarTokensVencidosUseCaseImpl(revocados, reloj);
    }

    @Bean
    ConsultarContactoUseCase consultarContacto(UsuarioRepositoryGateway usuarios) {
        return new ConsultarContactoUseCaseImpl(usuarios);
    }

    @Bean
    ListarUsuariosUseCase listarUsuarios(UsuarioRepositoryGateway usuarios) {
        return new ListarUsuariosUseCaseImpl(usuarios);
    }

    @Bean
    ConsultarEstadisticasUsuariosUseCase consultarEstadisticasUsuarios(UsuarioRepositoryGateway usuarios) {
        return new ConsultarEstadisticasUsuariosUseCaseImpl(usuarios);
    }

    @Bean
    CambiarEstadoCuentaUseCase cambiarEstadoCuenta(UsuarioRepositoryGateway usuarios, Clock reloj) {
        return new CambiarEstadoCuentaUseCaseImpl(usuarios, reloj);
    }

    @Bean
    CambiarClaveUseCase cambiarClave(UsuarioRepositoryGateway usuarios, CodificadorClaveGateway codificador, Clock reloj) {
        return new CambiarClaveUseCaseImpl(usuarios, codificador, reloj);
    }
}
