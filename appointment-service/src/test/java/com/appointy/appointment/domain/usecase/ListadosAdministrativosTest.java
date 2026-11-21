package com.appointy.appointment.domain.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.appointy.appointment.domain.exception.AccesoProhibidoException;
import com.appointy.appointment.domain.exception.NoEncontradoException;
import com.appointy.appointment.domain.exception.ValidacionException;
import com.appointy.appointment.domain.gateway.CatalogClientGateway;
import com.appointy.appointment.domain.gateway.CitaRepositoryGateway;
import com.appointy.appointment.domain.gateway.FranjaRepositoryGateway;
import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadisticasCitas;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.FiltroCitas;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.PaginaCitas;
import com.appointy.appointment.domain.model.PaginaCitasConFranja;
import com.appointy.appointment.domain.model.ProfesionalDeUsuario;
import org.junit.jupiter.api.Test;

class ListadosAdministrativosTest {

    static final Actor ADMIN = new Actor("a1", "ADMINISTRATIVO");
    static final Actor PACIENTE = new Actor("p1", "PACIENTE");
    static final Actor PROFESIONAL = new Actor("u-prof", "PROFESIONAL");

    final CitaRepositoryGateway citas = mock(CitaRepositoryGateway.class);
    final FranjaRepositoryGateway franjas = mock(FranjaRepositoryGateway.class);
    final CatalogClientGateway catalog = mock(CatalogClientGateway.class);

    static Cita cita(String id, String paciente, String profesional) {
        return new Cita(id, paciente, profesional, "e1", "c1", "f1", EstadoCita.CONFIRMADA, 1, 2, 1L);
    }

    static Franja franja() {
        return new Franja("f1", "prof1", "c1", "2026-11-20", 480, 510, EstadoFranja.OCUPADA, 1L);
    }

    @Test
    void elAdministrativoListaConFranjasYPaginacion() {
        when(citas.listar(new FiltroCitas(null, null, null), 0, 20)).thenReturn(new PaginaCitas(new Cita[] {cita("c-1", "p1", "prof1")}, 0, 20, 1, 1));
        when(franjas.buscarPorIds(new String[] {"f1"})).thenReturn(new Franja[] {franja()});
        PaginaCitasConFranja p = new ConsultaCitaUseCase(null, citas, franjas, null, null, Dobles.TX).listarCitas(ADMIN, new FiltroCitas(null, null, null), 0, 20);
        assertEquals(1, p.totalElementos());
        assertEquals("2026-11-20", p.contenido()[0].franja().fecha());
    }

    @Test
    void soloAdministrativoYPaginasValidas() {
        ConsultaCitaUseCase caso = new ConsultaCitaUseCase(null, citas, franjas, null, null, Dobles.TX);
        assertThrows(AccesoProhibidoException.class, () -> caso.listarCitas(PACIENTE, null, 0, 20));
        assertThrows(ValidacionException.class, () -> caso.listarCitas(ADMIN, null, -1, 20));
        assertThrows(ValidacionException.class, () -> caso.listarCitas(ADMIN, null, 0, 0));
        assertThrows(ValidacionException.class, () -> caso.listarCitas(ADMIN, null, 0, 101));
    }

    @Test
    void estadisticasSoloParaAdministrativo() {
        when(citas.estadisticas()).thenReturn(new EstadisticasCitas(1, 2, 3, 4));
        ConsultaCitaUseCase caso = new ConsultaCitaUseCase(null, citas, null, null, null, Dobles.TX);
        assertEquals(3, caso.consultarEstadisticasCitas(ADMIN).completadas());
        assertThrows(AccesoProhibidoException.class, () -> caso.consultarEstadisticasCitas(PROFESIONAL));
    }

    @Test
    void consultarUnaCitaRespetaQuienPuedeVerla() {
        when(citas.buscarPorId("c-1")).thenReturn(cita("c-1", "p1", "prof1"));
        when(franjas.buscarPorId("f1")).thenReturn(franja());
        when(catalog.buscarProfesionalDeUsuario("u-prof")).thenReturn(new ProfesionalDeUsuario("prof1", true));
        when(catalog.buscarProfesionalDeUsuario("u-otro")).thenReturn(new ProfesionalDeUsuario("prof9", true));
        ConsultaCitaUseCase caso = new ConsultaCitaUseCase(catalog, citas, franjas, null, null, Dobles.TX);
        CitaConFranja propia = caso.consultarCita(PACIENTE, "c-1");
        assertEquals("c-1", propia.cita().id());
        assertEquals("c-1", caso.consultarCita(ADMIN, "c-1").cita().id());
        assertEquals("c-1", caso.consultarCita(PROFESIONAL, "c-1").cita().id());
        assertThrows(AccesoProhibidoException.class, () -> caso.consultarCita(new Actor("p2", "PACIENTE"), "c-1"));
        assertThrows(AccesoProhibidoException.class, () -> caso.consultarCita(new Actor("u-otro", "PROFESIONAL"), "c-1"));
        assertThrows(NoEncontradoException.class, () -> caso.consultarCita(PACIENTE, "nada"));
    }
}
