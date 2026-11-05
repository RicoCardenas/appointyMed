package com.appointy.auth.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.port.in.CrearCuentaStaffPort.Comando;
import com.appointy.auth.domain.port.out.CodificadorClavePort;
import com.appointy.auth.domain.port.out.UsuarioRepositoryPort;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrearCuentaStaffUseCaseTest {

    @Mock UsuarioRepositoryPort usuarios;
    @Mock CodificadorClavePort codificador;

    CrearCuentaStaffUseCase useCase;

    @BeforeEach
    void iniciar() {
        useCase = new CrearCuentaStaffUseCase(usuarios, codificador, Clock.fixed(Instant.parse("2026-06-01T10:00:00Z"), ZoneOffset.UTC));
    }

    @Test
    void creaCuentaDeProfesionalActiva() {
        when(usuarios.existePorCorreo("doc@correo.com")).thenReturn(false);
        when(codificador.codificar("Clave1234")).thenReturn("hash");
        when(usuarios.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Usuario u = useCase.ejecutar(new Comando("Doc@Correo.com", "Clave1234", Rol.PROFESIONAL));

        assertEquals("doc@correo.com", u.correo());
        assertEquals(Rol.PROFESIONAL, u.rol());
        assertEquals("hash", u.claveHash());
    }

    @Test
    void noCreaCuentasDePacienteDesdeAqui() {
        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> useCase.ejecutar(new Comando("a@b.com", "Clave1234", Rol.PACIENTE)));

        assertEquals("ROL_NO_PERMITIDO", ex.codigo());
        verifyNoInteractions(usuarios);
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(usuarios.existePorCorreo("doc@correo.com")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> useCase.ejecutar(new Comando("doc@correo.com", "Clave1234", Rol.ADMINISTRATIVO)));
    }
}
