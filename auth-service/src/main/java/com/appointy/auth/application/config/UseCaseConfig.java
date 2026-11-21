package com.appointy.auth.application.config;

import com.appointy.auth.domain.usecase.CuentaUseCase;
import com.appointy.auth.domain.usecase.PerfilUseCase;
import com.appointy.auth.domain.usecase.SesionUseCase;
import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import com.appointy.auth.domain.gateway.EmisorTokenGateway;
import com.appointy.auth.domain.gateway.GeneradorIdGateway;
import com.appointy.auth.domain.gateway.PacientePerfilRepositoryGateway;
import com.appointy.auth.domain.gateway.RegistroGateway;
import com.appointy.auth.domain.gateway.RelojGateway;
import com.appointy.auth.domain.gateway.TokenRevocadoRepositoryGateway;
import com.appointy.auth.domain.gateway.TransactionGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Registro de los casos de uso: el dominio no conoce Spring, aqui se le entregan sus puertos de salida. */
@Configuration
public class UseCaseConfig {

    @Bean
    SesionUseCase sesionUseCase(UsuarioRepositoryGateway usuarios, CodificadorClaveGateway codificador, EmisorTokenGateway emisor,
            TokenRevocadoRepositoryGateway revocados, RelojGateway reloj, TransactionGateway transacciones) {
        return new SesionUseCase(usuarios, codificador, emisor, revocados, reloj, transacciones);
    }

    @Bean
    CuentaUseCase cuentaUseCase(UsuarioRepositoryGateway usuarios, PacientePerfilRepositoryGateway perfiles,
            CodificadorClaveGateway codificador, RelojGateway reloj, GeneradorIdGateway ids, TransactionGateway transacciones) {
        return new CuentaUseCase(usuarios, perfiles, codificador, reloj, ids, transacciones);
    }

    @Bean
    PerfilUseCase perfilUseCase(UsuarioRepositoryGateway usuarios, PacientePerfilRepositoryGateway perfiles, RelojGateway reloj,
            TransactionGateway transacciones, RegistroGateway registro) {
        return new PerfilUseCase(usuarios, perfiles, reloj, transacciones, registro);
    }
}
