package com.appointy.auth.application.usecase;

import com.appointy.auth.domain.exception.CredencialesInvalidasException;
import com.appointy.auth.domain.model.TokenEmitido;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.port.in.IniciarSesionPort;
import com.appointy.auth.domain.port.out.CodificadorClavePort;
import com.appointy.auth.domain.port.out.EmisorTokenPort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import com.appointy.auth.domain.rule.NormalizadorCorreo;
import org.springframework.transaction.annotation.Transactional;

public class IniciarSesionUseCase implements IniciarSesionPort {

    private final UsuarioRepositoryPort usuarios;
    private final CodificadorClavePort codificador;
    private final EmisorTokenPort emisor;
    /** Se compara contra este hash cuando el correo no existe, para que el tiempo no delate si la cuenta existe. */
    private final String hashSenuelo;

    public IniciarSesionUseCase(UsuarioRepositoryPort usuarios, CodificadorClavePort codificador, EmisorTokenPort emisor) {
        this.usuarios = usuarios;
        this.codificador = codificador;
        this.emisor = emisor;
        this.hashSenuelo = codificador.codificar("senuelo-sin-uso");
    }

    @Override
    @Transactional(readOnly = true)
    public Resultado ejecutar(Comando comando) {
        Usuario usuario = usuarios.buscarPorCorreo(NormalizadorCorreo.comparable(comando.correo()));
        String hash = usuario != null ? usuario.claveHash() : hashSenuelo;
        boolean claveCorrecta = comando.clave() != null && codificador.coincide(comando.clave(), hash);
        if (usuario == null || !claveCorrecta || !usuario.activo()) {
            throw new CredencialesInvalidasException();
        }
        TokenEmitido token = emisor.emitir(usuario);
        return new Resultado(token.token(), token.expiraEn(), usuario.id(), usuario.rol());
    }
}
