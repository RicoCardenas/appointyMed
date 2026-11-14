package com.appointy.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.exception.NoEncontradoException;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import org.junit.jupiter.api.Test;

class ConsultarContactoUseCaseTest {

    final UsuarioRepositoryGateway usuarios = mock(UsuarioRepositoryGateway.class);
    final ConsultarContactoUseCaseImpl caso = new ConsultarContactoUseCaseImpl(usuarios);

    @Test
    void devuelveElCorreoDeUnaCuentaActiva() {
        when(usuarios.buscarPorId("u1")).thenReturn(Usuario.nuevo("u1", "a@b.co", "hash", Rol.PACIENTE, 1));
        assertEquals("a@b.co", caso.ejecutar("u1"));
    }

    @Test
    void noExisteOEstaInactivo() {
        assertThrows(NoEncontradoException.class, () -> caso.ejecutar("nadie"));
        Usuario base = Usuario.nuevo("u2", "x@y.co", "hash", Rol.PACIENTE, 1);
        when(usuarios.buscarPorId("u2")).thenReturn(new Usuario(base.id(), base.correo(), base.claveHash(), base.rol(), false, 1, 1, null));
        assertThrows(NoEncontradoException.class, () -> caso.ejecutar("u2"));
    }
}
