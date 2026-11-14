package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.rule.ReglasUsuarios;
import com.appointy.auth.domain.usecase.CambiarClaveUseCase;
import java.time.Clock;
import org.springframework.transaction.annotation.Transactional;

public class CambiarClaveUseCaseImpl implements CambiarClaveUseCase {

    private final UsuarioRepositoryGateway usuarios;
    private final CodificadorClaveGateway codificador;
    private final Clock reloj;

    public CambiarClaveUseCaseImpl(UsuarioRepositoryGateway usuarios, CodificadorClaveGateway codificador, Clock reloj) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.reloj = reloj;
    }

    @Override
    @Transactional
    public void ejecutar(Comando comando) {
        ReglasUsuarios.validarCambioDeClave(comando.claveActual(), comando.claveNueva());
        Usuario usuario = usuarios.buscarPorId(comando.usuarioId());
        if (usuario == null) {
            throw new NoEncontradoException("USUARIO_NO_ENCONTRADO", "El usuario no existe");
        }
        if (!codificador.coincide(comando.claveActual(), usuario.claveHash())) {
            throw new ValidacionException("CLAVE_ACTUAL_INCORRECTA", "La clave actual no es correcta");
        }
        usuarios.guardar(usuario.conClaveHash(codificador.codificar(comando.claveNueva()), reloj.millis()));
    }
}
