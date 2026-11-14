package com.appointy.clinicalhistory.infrastructure.entry_points;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.model.Diagnostico;
import com.appointy.clinicalhistory.domain.model.EntradaClinica;
import com.appointy.clinicalhistory.domain.model.PaginaEntradas;
import com.appointy.clinicalhistory.domain.model.ResumenHistoria;
import com.appointy.clinicalhistory.domain.usecase.*;
import com.appointy.clinicalhistory.infrastructure.security.UsuarioAutenticado;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class HistoriaClinicaControllerTest {

    static final String UID = "11111111-1111-1111-1111-111111111111";
    static final String CITA = "22222222-2222-2222-2222-222222222222";
    static final String ENTRADA = "33333333-3333-3333-3333-333333333333";

    final RegistrarAtencionUseCase registrar = mock(RegistrarAtencionUseCase.class);
    final ConsultarHistoriaPacienteUseCase historiaDePaciente = mock(ConsultarHistoriaPacienteUseCase.class);
    final ConsultarMiHistoriaUseCase miHistoria = mock(ConsultarMiHistoriaUseCase.class);
    final CorregirEntradaUseCase corregir = mock(CorregirEntradaUseCase.class);
    final BuscarDiagnosticosUseCase diagnosticos = mock(BuscarDiagnosticosUseCase.class);
    final ConsultarResumenPacienteUseCase resumenPaciente = mock(ConsultarResumenPacienteUseCase.class);
    final ConsultarMiResumenUseCase miResumen = mock(ConsultarMiResumenUseCase.class);
    final ListarVersionesEntradaUseCase versiones = mock(ListarVersionesEntradaUseCase.class);
    final ListarAtencionesUseCase atenciones = mock(ListarAtencionesUseCase.class);
    MockMvc mvc;

    static EntradaClinica entrada() {
        return new EntradaClinica(ENTRADA, UID, "prof1", CITA, "motivo", "Gripe", "notas", null, null, 1000);
    }

    @BeforeEach
    void preparar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new UsuarioAutenticado(UID, "PROFESIONAL"), null, List.of()));
        mvc = MockMvcBuilders.standaloneSetup(new HistoriaClinicaController(registrar, historiaDePaciente, miHistoria, corregir, diagnosticos,
                        resumenPaciente, miResumen, versiones, atenciones))
                .setControllerAdvice(new GlobalExceptionHandler()).setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper().findAndRegisterModules())).build();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registraYCorrigeUnaAtencion() throws Exception {
        when(registrar.ejecutar(any())).thenReturn(entrada());
        when(corregir.ejecutar(any())).thenReturn(entrada());
        String cuerpo = "{\"citaId\":\"" + CITA + "\",\"motivo\":\"m\",\"diagnostico\":\"d\"}";
        mvc.perform(post("/api/v1/entradas").contentType("application/json").content(cuerpo)).andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(ENTRADA));
        mvc.perform(post("/api/v1/entradas/" + ENTRADA + "/correcciones").contentType("application/json").content("{\"motivo\":\"m\",\"diagnostico\":\"d\"}"))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/entradas").contentType("application/json").content("{\"motivo\":\"m\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void consultasDeHistoria() throws Exception {
        when(historiaDePaciente.ejecutar(eq(new Actor(UID, "PROFESIONAL")), eq(UID))).thenReturn(new EntradaClinica[] {entrada()});
        when(miHistoria.ejecutar(any())).thenReturn(new EntradaClinica[] {entrada(), entrada()});
        mvc.perform(get("/api/v1/pacientes/" + UID + "/historia")).andExpect(status().isOk()).andExpect(jsonPath("$[0].diagnostico").value("Gripe"));
        mvc.perform(get("/api/v1/historia/mia")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void listadoPaginadoVersionesResumenesYDiagnosticos() throws Exception {
        when(atenciones.ejecutar(any(), eq(2), eq(5))).thenReturn(new PaginaEntradas(new EntradaClinica[] {entrada()}, 2, 5, 11, 3));
        when(versiones.ejecutar(any(), eq(ENTRADA))).thenReturn(new EntradaClinica[] {entrada()});
        when(resumenPaciente.ejecutar(any(), eq(UID))).thenReturn(new ResumenHistoria(2, 1, 5000, "Gripe"));
        when(miResumen.ejecutar(any())).thenReturn(new ResumenHistoria(0, 0, 0, null));
        when(diagnosticos.ejecutar(any(), eq("gripe"))).thenReturn(new Diagnostico[] {new Diagnostico("J11", "Influenza")});
        mvc.perform(get("/api/v1/atenciones?page=2&size=5")).andExpect(status().isOk()).andExpect(jsonPath("$.totalPaginas").value(3))
                .andExpect(jsonPath("$.contenido[0].id").value(ENTRADA));
        mvc.perform(get("/api/v1/entradas/" + ENTRADA + "/versiones")).andExpect(status().isOk()).andExpect(jsonPath("$[0].citaId").value(CITA));
        mvc.perform(get("/api/v1/pacientes/" + UID + "/resumen")).andExpect(status().isOk()).andExpect(jsonPath("$.atenciones").value(2))
                .andExpect(jsonPath("$.ultimaAtencionEn").exists());
        mvc.perform(get("/api/v1/historia/mia/resumen")).andExpect(status().isOk()).andExpect(jsonPath("$.ultimaAtencionEn").doesNotExist());
        mvc.perform(get("/api/v1/diagnosticos?q=gripe")).andExpect(status().isOk()).andExpect(jsonPath("$[0].codigo").value("J11"));
        mvc.perform(get("/api/v1/diagnosticos")).andExpect(status().isBadRequest());
    }
}
