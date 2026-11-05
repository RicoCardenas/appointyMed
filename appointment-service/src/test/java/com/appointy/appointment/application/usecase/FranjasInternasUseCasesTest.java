package com.appointy.appointment.application.usecase;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appointy.appointment.domain.exception.ConflictoCitasException;
import com.appointy.appointment.domain.exception.ValidacionException;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.FranjaNueva;
import com.appointy.appointment.domain.model.ResultadoGeneracion;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FranjasInternasUseCasesTest {

    FranjaRepositoryGateway franjas = mock(FranjaRepositoryGateway.class);
    CitaRepositoryGateway citas = mock(CitaRepositoryGateway.class);

    static Franja franja(String id, EstadoFranja estado) {
        return new Franja(id, "prof", "centro", "2030-06-11", 480, 510, estado, 0L);
    }

    @Test
    void generarCuentaLasCreadasYLasQueYaExistian() {
        when(franjas.insertarSiNoExiste(any())).thenReturn(true, false, true);

        ResultadoGeneracion r = new GenerarFranjasUseCaseImpl(franjas).ejecutar("prof", "centro",
                new FranjaNueva[] {new FranjaNueva("2030-06-11", 480, 510), new FranjaNueva("2030-06-11", 510, 540), new FranjaNueva("2030-06-11", 540, 570)});

        assertEquals(2, r.creadas());
        assertEquals(1, r.existentes());
        ArgumentCaptor<Franja> f = ArgumentCaptor.forClass(Franja.class);
        verify(franjas, times(3)).insertarSiNoExiste(f.capture());
        assertEquals(EstadoFranja.DISPONIBLE, f.getAllValues().get(0).estado());
    }

    @Test
    void generarRechazaHorasInvertidasOFueraDelDia() {
        GenerarFranjasUseCaseImpl uc = new GenerarFranjasUseCaseImpl(franjas);
        assertEquals("FRANJA_INVALIDA", assertThrows(ValidacionException.class,
                () -> uc.ejecutar("p", "c", new FranjaNueva[] {new FranjaNueva("2030-06-11", 510, 510)})).codigo());
        assertThrows(ValidacionException.class, () -> uc.ejecutar("p", "c", new FranjaNueva[] {new FranjaNueva("2030-06-11", 480, 1440)}));
        verify(franjas, never()).insertarSiNoExiste(any());
    }

    @Test
    void bloquearSoloCambiaLasDisponiblesYEsIdempotente() {
        when(franjas.bloquearRango("prof", "centro", "2030-06-11", "2030-06-12")).thenReturn(new Franja[] {
            franja("a", EstadoFranja.DISPONIBLE), franja("b", EstadoFranja.BLOQUEADA), franja("c", EstadoFranja.DISPONIBLE)});
        when(citas.idsConfirmadasEnRango("prof", "centro", "2030-06-11", "2030-06-12")).thenReturn(new String[0]);
        when(franjas.guardar(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(2, new BloquearFranjasUseCaseImpl(franjas, citas).ejecutar("prof", "centro", "2030-06-11", "2030-06-12"));

        ArgumentCaptor<Franja> f = ArgumentCaptor.forClass(Franja.class);
        verify(franjas, times(2)).guardar(f.capture());
        f.getAllValues().forEach(x -> assertEquals(EstadoFranja.BLOQUEADA, x.estado()));
    }

    @Test
    void bloquearConCitasConfirmadasListaLasCitasYNoCambiaNada() {
        when(franjas.bloquearRango("prof", "centro", "2030-06-11", "2030-06-12")).thenReturn(new Franja[] {franja("a", EstadoFranja.OCUPADA)});
        when(citas.idsConfirmadasEnRango("prof", "centro", "2030-06-11", "2030-06-12")).thenReturn(new String[] {"cita1", "cita2"});

        ConflictoCitasException ex = assertThrows(ConflictoCitasException.class,
                () -> new BloquearFranjasUseCaseImpl(franjas, citas).ejecutar("prof", "centro", "2030-06-11", "2030-06-12"));

        assertArrayEquals(new String[] {"cita1", "cita2"}, ex.citasAfectadas());
        verify(franjas, never()).guardar(any());
    }

    @Test
    void bloquearRechazaUnRangoInvertido() {
        assertEquals("RANGO_INVALIDO", assertThrows(ValidacionException.class,
                () -> new BloquearFranjasUseCaseImpl(franjas, citas).ejecutar("p", "c", "2030-06-12", "2030-06-11")).codigo());
    }
}
