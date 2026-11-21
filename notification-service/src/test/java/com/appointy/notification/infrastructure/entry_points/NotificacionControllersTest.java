package com.appointy.notification.infrastructure.entry_points;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.appointy.notification.domain.usecase.ConsultaNotificacionUseCase;
import com.appointy.notification.domain.usecase.NotificacionUseCase;
import com.appointy.notification.domain.exception.NoEncontradoException;
import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.Estadisticas;
import com.appointy.notification.domain.model.FiltroNotificaciones;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.model.Pagina;
import com.appointy.notification.domain.model.ResultadoLote;
import com.appointy.notification.domain.model.SolicitudCita;
import com.appointy.notification.domain.model.TipoNotificacion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class NotificacionControllersTest {

    static final String ID = "11111111-1111-1111-1111-111111111111";
    static final String CITA = "22222222-2222-2222-2222-222222222222";
    final ConsultaNotificacionUseCase consultaNotificacionUseCase = mock(ConsultaNotificacionUseCase.class);
    final NotificacionUseCase notificacionUseCase = mock(NotificacionUseCase.class);
    MockMvc mvc;

    static Notificacion n(EstadoNotificacion estado) {
        return new Notificacion(ID, CITA, TipoNotificacion.CONFIRMACION, "paciente@correo.co", "Cita confirmada", "cuerpo", estado, 1, null, 1000, 2000, 500);
    }

    @BeforeEach
    void preparar() {
        mvc = MockMvcBuilders.standaloneSetup(
                        new NotificacionController(consultaNotificacionUseCase, notificacionUseCase), new InternalNotificacionController(notificacionUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper().findAndRegisterModules())).build();
    }

    @Test
    void listaPaginadaConFiltros() throws Exception {
        when(consultaNotificacionUseCase.listarNotificaciones(any(), eq(1), eq(5))).thenReturn(new Pagina(new Notificacion[] {n(EstadoNotificacion.ENVIADA)}, 1, 5, 6, 2));
        mvc.perform(get("/api/v1/notificaciones?page=1&size=5&estado=ENVIADA&tipo=CONFIRMACION&citaId=" + CITA))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(6)).andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.contenido[0].destinatario").value("p***@correo.co")).andExpect(jsonPath("$.contenido[0].cuerpo").doesNotExist());
        ArgumentCaptor<FiltroNotificaciones> filtro = ArgumentCaptor.forClass(FiltroNotificaciones.class);
        verify(consultaNotificacionUseCase).listarNotificaciones(filtro.capture(), eq(1), eq(5));
        org.junit.jupiter.api.Assertions.assertEquals(EstadoNotificacion.ENVIADA, filtro.getValue().estado());
        org.junit.jupiter.api.Assertions.assertEquals(CITA, filtro.getValue().citaId());
    }

    @Test
    void listaSinFiltrosUsaLosValoresPorDefecto() throws Exception {
        when(consultaNotificacionUseCase.listarNotificaciones(any(), eq(0), eq(20))).thenReturn(new Pagina(new Notificacion[0], 0, 20, 0, 0));
        mvc.perform(get("/api/v1/notificaciones")).andExpect(status().isOk()).andExpect(jsonPath("$.contenido").isEmpty());
    }

    @Test
    void estadisticasYHistorialPorCita() throws Exception {
        when(consultaNotificacionUseCase.consultarEstadisticas()).thenReturn(new Estadisticas(1, 2, 3, 4, 5, 6, 7, 8));
        when(consultaNotificacionUseCase.listarNotificacionesDeCita(CITA)).thenReturn(new Notificacion[] {n(EstadoNotificacion.ENVIADA)});
        mvc.perform(get("/api/v1/notificaciones/estadisticas")).andExpect(status().isOk()).andExpect(jsonPath("$.recordatorios").value(8));
        mvc.perform(get("/api/v1/notificaciones/cita/" + CITA)).andExpect(status().isOk()).andExpect(jsonPath("$[0].estado").value("ENVIADA"));
    }

    @Test
    void reenvioYJobsManuales() throws Exception {
        when(notificacionUseCase.reenviarNotificacion(ID)).thenReturn(n(EstadoNotificacion.ENVIADA));
        when(notificacionUseCase.enviarRecordatorios()).thenReturn(new ResultadoLote(2, 1, 1));
        when(notificacionUseCase.reintentarFallidas()).thenReturn(new ResultadoLote(0, 0, 0));
        mvc.perform(post("/api/v1/notificaciones/" + ID + "/reenviar")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value(ID));
        mvc.perform(post("/api/v1/notificaciones/recordatorios/ejecutar")).andExpect(status().isOk()).andExpect(jsonPath("$.fallidas").value(1));
        mvc.perform(post("/api/v1/notificaciones/reintentos/ejecutar")).andExpect(status().isOk()).andExpect(jsonPath("$.procesadas").value(0));
    }

    @Test
    void erroresDeDominioYParametrosInvalidos() throws Exception {
        when(notificacionUseCase.reenviarNotificacion(ID)).thenThrow(new NoEncontradoException("NOTIFICACION_NO_ENCONTRADA", "no existe"));
        mvc.perform(post("/api/v1/notificaciones/" + ID + "/reenviar")).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/notificaciones/cita/no-es-uuid")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("PARAMETRO_INVALIDO"));
    }

    @Test
    void notificarCitaInterno() throws Exception {
        when(notificacionUseCase.notificarCita(any(SolicitudCita.class))).thenReturn(n(EstadoNotificacion.ENVIADA));
        String cuerpo = "{\"tipo\":\"CONFIRMADA\",\"citaId\":\"" + CITA + "\",\"pacienteId\":\"" + ID + "\",\"fecha\":\"2026-11-20\",\"horaInicio\":\"08:00\"}";
        mvc.perform(post("/internal/notificaciones").contentType("application/json").content(cuerpo))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.estado").value("ENVIADA"));
        mvc.perform(post("/internal/notificaciones").contentType("application/json").content("{\"tipo\":\"CONFIRMADA\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("VALIDACION_ENTRADA"));
    }
}
