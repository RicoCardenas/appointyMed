package com.appointy.catalog.domain.usecase;

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
import com.appointy.catalog.domain.usecase.EspecialidadUseCase;
import com.appointy.catalog.domain.gateway.EspecialidadRepositoryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EspecialidadUseCasesTest {

    @Mock EspecialidadRepositoryGateway especialidades;

    final Especialidad cardiologia = new Especialidad("e1", "Cardiologia", "Corazon", true);

    @Test
    void creaUnaEspecialidadActivaConElNombreRecortado() {
        when(especialidades.buscarPorNombre("Pediatria")).thenReturn(null);
        when(especialidades.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Especialidad e = new EspecialidadUseCase(especialidades, Dobles.TX, Dobles.IDS).crearEspecialidad(new EspecialidadUseCase.CrearEspecialidadComando("  Pediatria ", "Ninos"));

        assertEquals("Pediatria", e.nombre());
        assertTrue(e.activo());
        assertEquals(36, e.id().length());
    }

    @Test
    void noCreaUnNombreRepetidoNiVacio() {
        when(especialidades.buscarPorNombre("Cardiologia")).thenReturn(cardiologia);
        EspecialidadUseCase useCase = new EspecialidadUseCase(especialidades, Dobles.TX, Dobles.IDS);

        assertEquals("ESPECIALIDAD_YA_EXISTE", assertThrows(ConflictoException.class,
                () -> useCase.crearEspecialidad(new EspecialidadUseCase.CrearEspecialidadComando("Cardiologia", null))).codigo());
        assertThrows(ValidacionException.class, () -> useCase.crearEspecialidad(new EspecialidadUseCase.CrearEspecialidadComando("  ", null)));
        verify(especialidades, never()).guardar(any());
    }

    @Test
    void actualizaConservandoElMismoNombreSinConflictoConsigoMisma() {
        when(especialidades.buscarPorId("e1")).thenReturn(cardiologia);
        when(especialidades.buscarPorNombre("Cardiologia")).thenReturn(cardiologia);
        when(especialidades.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Especialidad e = new EspecialidadUseCase(especialidades, Dobles.TX, Dobles.IDS)
                .actualizarEspecialidad(new EspecialidadUseCase.ActualizarEspecialidadComando("e1", "Cardiologia", "Nueva descripcion"));

        assertEquals("Nueva descripcion", e.descripcion());
    }

    @Test
    void noActualizaAUnNombreDeOtraEspecialidadNiUnaInexistente() {
        when(especialidades.buscarPorId("e2")).thenReturn(new Especialidad("e2", "Pediatria", null, true));
        when(especialidades.buscarPorNombre("Cardiologia")).thenReturn(cardiologia);
        when(especialidades.buscarPorId("nada")).thenReturn(null);
        EspecialidadUseCase useCase = new EspecialidadUseCase(especialidades, Dobles.TX, Dobles.IDS);

        assertThrows(ConflictoException.class, () -> useCase.actualizarEspecialidad(new EspecialidadUseCase.ActualizarEspecialidadComando("e2", "Cardiologia", null)));
        assertThrows(NoEncontradoException.class, () -> useCase.actualizarEspecialidad(new EspecialidadUseCase.ActualizarEspecialidadComando("nada", "X", null)));
    }

    @Test
    void desactivarEsLogicoEIdempotente() {
        when(especialidades.buscarPorId("e1")).thenReturn(cardiologia);
        when(especialidades.guardar(any())).thenAnswer(i -> i.getArgument(0));
        new EspecialidadUseCase(especialidades, Dobles.TX, Dobles.IDS).desactivarEspecialidad("e1");
        verify(especialidades).guardar(new Especialidad("e1", "Cardiologia", "Corazon", false));

        when(especialidades.buscarPorId("e3")).thenReturn(new Especialidad("e3", "Vieja", null, false));
        new EspecialidadUseCase(especialidades, Dobles.TX, Dobles.IDS).desactivarEspecialidad("e3");
        verify(especialidades, never()).guardar(new Especialidad("e3", "Vieja", null, false));
        assertFalse(cardiologia.desactivar().activo());
    }
}
