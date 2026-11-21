package com.appointy.appointment.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.appointy.appointment.domain.exception.NoEncontradoException;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.EstadoCita;
import org.junit.jupiter.api.Test;

class ObtenerCitaServicioUseCaseTest {

    CitaRepositoryGateway citas = mock(CitaRepositoryGateway.class);
    ConsultaCitaUseCase caso = new ConsultaCitaUseCase(null, citas, null, null, null, Dobles.TX);

    @Test
    void devuelveLaCitaSinReglasDePropiedad() {
        Cita cita = new Cita("c1", "pac", "prof", "esp", "centro", "f1", EstadoCita.CONFIRMADA, 1L, 1L, 0L);
        when(citas.buscarPorId("c1")).thenReturn(cita);
        assertEquals(cita, caso.obtenerCitaServicio("c1"));
    }

    @Test
    void siNoExisteLanza404() {
        assertEquals("CITA_NO_ENCONTRADA", assertThrows(NoEncontradoException.class, () -> caso.obtenerCitaServicio("x")).codigo());
    }
}
