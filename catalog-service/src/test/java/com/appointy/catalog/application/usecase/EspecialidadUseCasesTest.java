package com.appointy.catalog.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.port.in.ActualizarEspecialidadPort;
import com.appointy.catalog.domain.port.in.CrearEspecialidadPort;
import com.appointy.catalog.domain.port.out.EspecialidadRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EspecialidadUseCasesTest {

    @Mock EspecialidadRepositoryPort especialidades;

    final Especialidad cardiologia = new Especialidad("e1", "Cardiologia", "Corazon", true);

    @Test
    void creaUnaEspecialidadActivaConElNombreRecortado() {
        when(especialidades.buscarPorNombre("Pediatria")).thenReturn(null);
        when(especialidades.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Especialidad e = new CrearEspecialidadUseCase(especialidades).ejecutar(new CrearEspecialidadPort.Comando("  Pediatria ", "Ninos"));

        assertEquals("Pediatria", e.nombre());
        assertTrue(e.activo());
        assertEquals(36, e.id().length());
    }

    @Test
    void noCreaUnNombreRepetidoNiVacio() {
        when(especialidades.buscarPorNombre("Cardiologia")).thenReturn(cardiologia);
        CrearEspecialidadUseCase useCase = new CrearEspecialidadUseCase(especialidades);

        assertEquals("ESPECIALIDAD_YA_EXISTE", assertThrows(ConflictoException.class,
                () -> useCase.ejecutar(new CrearEspecialidadPort.Comando("Cardiologia", null))).codigo());
        assertThrows(ValidacionException.class, () -> useCase.ejecutar(new CrearEspecialidadPort.Comando("  ", null)));
        verify(especialidades, never()).guardar(any());
    }

    @Test
    void actualizaConservandoElMismoNombreSinConflictoConsigoMisma() {
        when(especialidades.buscarPorId("e1")).thenReturn(cardiologia);
        when(especialidades.buscarPorNombre("Cardiologia")).thenReturn(cardiologia);
        when(especialidades.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Especialidad e = new ActualizarEspecialidadUseCase(especialidades)
                .ejecutar(new ActualizarEspecialidadPort.Comando("e1", "Cardiologia", "Nueva descripcion"));

        assertEquals("Nueva descripcion", e.descripcion());
    }

    @Test
    void noActualizaAUnNombreDeOtraEspecialidadNiUnaInexistente() {
        when(especialidades.buscarPorId("e2")).thenReturn(new Especialidad("e2", "Pediatria", null, true));
        when(especialidades.buscarPorNombre("Cardiologia")).thenReturn(cardiologia);
        when(especialidades.buscarPorId("nada")).thenReturn(null);
        ActualizarEspecialidadUseCase useCase = new ActualizarEspecialidadUseCase(especialidades);

        assertThrows(ConflictoException.class, () -> useCase.ejecutar(new ActualizarEspecialidadPort.Comando("e2", "Cardiologia", null)));
        assertThrows(NoEncontradoException.class, () -> useCase.ejecutar(new ActualizarEspecialidadPort.Comando("nada", "X", null)));
    }

    @Test
    void desactivarEsLogicoEIdempotente() {
        when(especialidades.buscarPorId("e1")).thenReturn(cardiologia);
        when(especialidades.guardar(any())).thenAnswer(i -> i.getArgument(0));
        new DesactivarEspecialidadUseCase(especialidades).ejecutar("e1");
        verify(especialidades).guardar(new Especialidad("e1", "Cardiologia", "Corazon", false));

        when(especialidades.buscarPorId("e3")).thenReturn(new Especialidad("e3", "Vieja", null, false));
        new DesactivarEspecialidadUseCase(especialidades).ejecutar("e3");
        verify(especialidades, never()).guardar(new Especialidad("e3", "Vieja", null, false));
        assertFalse(cardiologia.desactivar().activo());
    }
}
