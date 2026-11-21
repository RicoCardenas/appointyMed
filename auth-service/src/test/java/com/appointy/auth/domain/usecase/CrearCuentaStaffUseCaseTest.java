package com.appointy.auth.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.appointy.auth.domain.exception.ConflictoException;
import com.appointy.auth.domain.exception.ValidacionException;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.usecase.CuentaUseCase.CrearCuentaStaffComando;
import com.appointy.auth.domain.gateway.CodificadorClaveGateway;
import com.appointy.auth.domain.gateway.UsuarioRepositoryGateway;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CrearCuentaStaffUseCaseTest {

    @Mock UsuarioRepositoryGateway usuarios;
    @Mock CodificadorClaveGateway codificador;

    CuentaUseCase useCase;

    @BeforeEach
    void iniciar() {
        useCase = new CuentaUseCase(usuarios, null, codificador, Dobles.reloj("2026-06-01T10:00:00Z"), Dobles.IDS, Dobles.TX);
    }

    @Test
    void creaCuentaDeProfesionalActiva() {
        when(usuarios.existePorCorreo("doc@correo.com")).thenReturn(false);
        when(codificador.codificar("Clave1234")).thenReturn("hash");
        when(usuarios.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Usuario u = useCase.crearCuentaStaff(new CrearCuentaStaffComando("Doc@Correo.com", "Clave1234", Rol.PROFESIONAL));

        assertEquals("doc@correo.com", u.correo());
        assertEquals(Rol.PROFESIONAL, u.rol());
        assertEquals("hash", u.claveHash());
    }

    @Test
    void noCreaCuentasDePacienteDesdeAqui() {
        ValidacionException ex = assertThrows(ValidacionException.class,
                () -> useCase.crearCuentaStaff(new CrearCuentaStaffComando("a@b.com", "Clave1234", Rol.PACIENTE)));

        assertEquals("ROL_NO_PERMITIDO", ex.codigo());
        verifyNoInteractions(usuarios);
    }

    @Test
    void rechazaCorreoDuplicado() {
        when(usuarios.existePorCorreo("doc@correo.com")).thenReturn(true);

        assertThrows(ConflictoException.class,
                () -> useCase.crearCuentaStaff(new CrearCuentaStaffComando("doc@correo.com", "Clave1234", Rol.ADMINISTRATIVO)));
    }
}
