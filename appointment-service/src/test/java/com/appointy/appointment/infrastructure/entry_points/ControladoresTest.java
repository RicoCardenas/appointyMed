package com.appointy.appointment.infrastructure.entry_points;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.CitaConFranja;
import com.appointy.appointment.domain.model.EstadisticasCitas;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.EstadoFranja;
import com.appointy.appointment.domain.model.FiltroCitas;
import com.appointy.appointment.domain.model.Franja;
import com.appointy.appointment.domain.model.HistorialCita;
import com.appointy.appointment.domain.model.PaginaCitasConFranja;
import com.appointy.appointment.domain.model.ResultadoGeneracion;
import com.appointy.appointment.domain.usecase.CitaUseCase;
import com.appointy.appointment.domain.usecase.ConsultaCitaUseCase;
import com.appointy.appointment.domain.usecase.FranjaUseCase;
import com.appointy.appointment.infrastructure.security.UsuarioAutenticado;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/** Los controladores sin contexto de Spring: rutas, codigos HTTP, conversion de parametros y de respuestas. El rol por endpoint se prueba en el e2e. */
class ControladoresTest {

    static final String U = "11111111-1111-1111-1111-111111111111";
    static final String ID = "22222222-2222-2222-2222-222222222222";
    static final String F = "33333333-3333-3333-3333-333333333333";
    final CitaUseCase citaUseCase = mock(CitaUseCase.class);
    final ConsultaCitaUseCase consultaCitaUseCase = mock(ConsultaCitaUseCase.class);
    final FranjaUseCase franjaUseCase = mock(FranjaUseCase.class);
    MockMvc mvc;

    static Franja franja() {
        return new Franja(F, U, U, "2026-11-20", 480, 510, EstadoFranja.OCUPADA, 1L);
    }

    static Cita cita(EstadoCita estado) {
        return new Cita(ID, U, U, U, U, F, estado, 1000, 2000, 1L);
    }

    static CitaConFranja conFranja(EstadoCita estado) {
        return new CitaConFranja(cita(estado), franja());
    }

    @BeforeEach
    void preparar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new UsuarioAutenticado(U, "PACIENTE"), null, List.of()));
        mvc = MockMvcBuilders.standaloneSetup(
                        new CitaController(citaUseCase, consultaCitaUseCase),
                        new DisponibilidadController(franjaUseCase), new InternalController(franjaUseCase, consultaCitaUseCase))
                .setControllerAdvice(new GlobalExceptionHandler()).setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper().findAndRegisterModules())).build();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reservarReprogramarYCancelar() throws Exception {
        when(citaUseCase.reservarCita(any())).thenReturn(conFranja(EstadoCita.CONFIRMADA));
        when(citaUseCase.reprogramarCita(any())).thenReturn(conFranja(EstadoCita.CONFIRMADA));
        when(citaUseCase.cancelarCita(any())).thenReturn(conFranja(EstadoCita.CANCELADA));
        String ids = "\"especialidadId\":\"" + U + "\",\"profesionalId\":\"" + U + "\",\"centroId\":\"" + U + "\",\"franjaId\":\"" + F + "\"";
        mvc.perform(post("/api/v1/citas").contentType("application/json").content("{" + ids + "}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("CONFIRMADA")).andExpect(jsonPath("$.horaInicio").value("08:00"));
        mvc.perform(post("/api/v1/citas/" + ID + "/reprogramar").contentType("application/json").content("{\"franjaId\":\"" + F + "\"}")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/citas/" + ID + "/cancelar").contentType("application/json").content("{\"motivo\":\"viaje\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CANCELADA"));
        mvc.perform(post("/api/v1/citas/" + ID + "/cancelar")).andExpect(status().isOk());
        ArgumentCaptor<CitaUseCase.CancelarCitaComando> comando = ArgumentCaptor.forClass(CitaUseCase.CancelarCitaComando.class);
        verify(citaUseCase, org.mockito.Mockito.times(2)).cancelarCita(comando.capture());
        org.junit.jupiter.api.Assertions.assertEquals("viaje", comando.getAllValues().get(0).motivo());
        org.junit.jupiter.api.Assertions.assertNull(comando.getAllValues().get(1).motivo());
        mvc.perform(post("/api/v1/citas").contentType("application/json").content("{}")).andExpect(status().isBadRequest());
    }

    @Test
    void consultasDelPacienteYDelProfesional() throws Exception {
        when(consultaCitaUseCase.consultarMisCitas(U, "PROXIMAS")).thenReturn(new CitaConFranja[] {conFranja(EstadoCita.CONFIRMADA)});
        when(consultaCitaUseCase.consultarAgenda(U, "2026-11-20")).thenReturn(new CitaConFranja[0]);
        when(consultaCitaUseCase.consultarAgenda(U, null)).thenReturn(new CitaConFranja[] {conFranja(EstadoCita.CONFIRMADA)});
        when(consultaCitaUseCase.consultarCita(eq(new Actor(U, "PACIENTE")), eq(ID))).thenReturn(conFranja(EstadoCita.CONFIRMADA));
        when(citaUseCase.marcarCitaAtendida(any(), eq(ID), eq(EstadoCita.COMPLETADA))).thenReturn(conFranja(EstadoCita.COMPLETADA));
        when(consultaCitaUseCase.consultarHistorialCita(any(), eq(ID))).thenReturn(new HistorialCita[] {
                new HistorialCita("h1", ID, null, EstadoCita.CONFIRMADA, U, "PACIENTE", null, F, "Reserva", 5000)});
        mvc.perform(get("/api/v1/citas/mias?periodo=PROXIMAS")).andExpect(status().isOk()).andExpect(jsonPath("$[0].fecha").value("2026-11-20"));
        mvc.perform(get("/api/v1/citas/agenda?fecha=2026-11-20")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        mvc.perform(get("/api/v1/citas/agenda")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/v1/citas/" + ID)).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(ID));
        mvc.perform(patch("/api/v1/citas/" + ID + "/estado").contentType("application/json").content("{\"estado\":\"COMPLETADA\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("COMPLETADA"));
        mvc.perform(get("/api/v1/citas/" + ID + "/historial")).andExpect(status().isOk()).andExpect(jsonPath("$[0].estadoNuevo").value("CONFIRMADA"));
        mvc.perform(get("/api/v1/citas/no-es-uuid")).andExpect(status().isBadRequest());
    }

    @Test
    void listadoPaginadoYEstadisticasDelAdministrativo() throws Exception {
        when(consultaCitaUseCase.listarCitas(any(), any(FiltroCitas.class), eq(1), eq(5))).thenReturn(
                new PaginaCitasConFranja(new CitaConFranja[] {conFranja(EstadoCita.CONFIRMADA)}, 1, 5, 6, 2));
        when(consultaCitaUseCase.consultarEstadisticasCitas(any())).thenReturn(new EstadisticasCitas(1, 2, 3, 4));
        mvc.perform(get("/api/v1/citas?page=1&size=5&estado=CONFIRMADA&profesionalId=" + U + "&pacienteId=" + U))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(6)).andExpect(jsonPath("$.contenido[0].id").value(ID));
        ArgumentCaptor<FiltroCitas> filtro = ArgumentCaptor.forClass(FiltroCitas.class);
        verify(consultaCitaUseCase).listarCitas(any(), filtro.capture(), eq(1), eq(5));
        org.junit.jupiter.api.Assertions.assertEquals(EstadoCita.CONFIRMADA, filtro.getValue().estado());
        org.junit.jupiter.api.Assertions.assertEquals(U, filtro.getValue().pacienteId());
        mvc.perform(get("/api/v1/citas/estadisticas")).andExpect(status().isOk()).andExpect(jsonPath("$.noAsistidas").value(4));
    }

    @Test
    void disponibilidadPublica() throws Exception {
        when(franjaUseCase.consultarDisponibilidad(U, U, "2026-11-20")).thenReturn(new Franja[] {franja()});
        mvc.perform(get("/api/v1/disponibilidad?profesionalId=" + U + "&centroId=" + U + "&fecha=2026-11-20"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].horaFin").value("08:30")).andExpect(jsonPath("$[0].estado").value("OCUPADA"));
        mvc.perform(get("/api/v1/disponibilidad?profesionalId=" + U)).andExpect(status().isBadRequest());
    }

    @Test
    void llamadasInternas() throws Exception {
        when(franjaUseCase.generarFranjas(eq(U), eq(U), any())).thenReturn(new ResultadoGeneracion(3, 1));
        when(franjaUseCase.bloquearFranjas(U, U, "2026-11-20", "2026-11-21")).thenReturn(2);
        when(consultaCitaUseCase.validarRelacion(U, U)).thenReturn(true);
        when(consultaCitaUseCase.obtenerCitaServicio(ID)).thenReturn(cita(EstadoCita.CONFIRMADA));
        String generarCuerpo = "{\"profesionalId\":\"" + U + "\",\"centroId\":\"" + U + "\",\"franjas\":[{\"fecha\":\"2026-11-20\",\"horaInicio\":\"08:00\",\"horaFin\":\"08:30\"}]}";
        mvc.perform(post("/internal/franjas/generar").contentType("application/json").content(generarCuerpo))
                .andExpect(status().isOk()).andExpect(jsonPath("$.creadas").value(3)).andExpect(jsonPath("$.existentes").value(1));
        mvc.perform(post("/internal/franjas/bloquear").contentType("application/json")
                .content("{\"profesionalId\":\"" + U + "\",\"centroId\":\"" + U + "\",\"fechaInicio\":\"2026-11-20\",\"fechaFin\":\"2026-11-21\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.bloqueadas").value(2));
        mvc.perform(get("/internal/relaciones?profesionalId=" + U + "&pacienteId=" + U)).andExpect(status().isOk()).andExpect(jsonPath("$.existe").value(true));
        mvc.perform(get("/internal/citas/" + ID)).andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("CONFIRMADA"));
        mvc.perform(post("/internal/franjas/generar").contentType("application/json").content("{\"franjas\":[]}")).andExpect(status().isBadRequest());
    }
}
