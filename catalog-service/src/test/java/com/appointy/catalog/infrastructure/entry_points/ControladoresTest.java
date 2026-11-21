package com.appointy.catalog.infrastructure.entry_points;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.model.Bloqueo;
import com.appointy.catalog.domain.model.Centro;
import com.appointy.catalog.domain.model.DetalleProfesional;
import com.appointy.catalog.domain.model.Especialidad;
import com.appointy.catalog.domain.model.Festivo;
import com.appointy.catalog.domain.model.FiltroProfesionales;
import com.appointy.catalog.domain.model.HorarioAtencion;
import com.appointy.catalog.domain.model.PaginaProfesionales;
import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.model.ResultadoHorario;
import com.appointy.catalog.domain.usecase.CentroUseCase;
import com.appointy.catalog.domain.usecase.EspecialidadUseCase;
import com.appointy.catalog.domain.usecase.HorarioUseCase;
import com.appointy.catalog.domain.usecase.ParametroUseCase;
import com.appointy.catalog.domain.usecase.ProfesionalUseCase;
import com.appointy.catalog.infrastructure.security.UsuarioAutenticado;
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

/** Controladores sin contexto de Spring: rutas, codigos HTTP, validacion y conversion. El rol por endpoint se prueba en el e2e. */
class ControladoresTest {

    static final String U = "11111111-1111-1111-1111-111111111111";
    static final String V = "22222222-2222-2222-2222-222222222222";
    final ProfesionalUseCase profesionalUseCase = mock(ProfesionalUseCase.class);
    final CentroUseCase centroUseCase = mock(CentroUseCase.class);
    final HorarioUseCase horarioUseCase = mock(HorarioUseCase.class);
    final EspecialidadUseCase especialidadUseCase = mock(EspecialidadUseCase.class);
    final ParametroUseCase parametroUseCase = mock(ParametroUseCase.class);
    MockMvc mvc;
    String rol = "ADMINISTRATIVO";

    static Profesional profesional() {
        return new Profesional(U, V, "Ana", "Perez", "RM-1", true);
    }

    static Centro centro() {
        return new Centro(U, "Sede", "Calle 1", "Bogota", "3001234567", true);
    }

    static HorarioAtencion horario() {
        return new HorarioAtencion(U, V, U, 1, 480, 540, 30, true);
    }

    static ResultadoHorario resultado() {
        return new ResultadoHorario(horario(), true, 4, 0, "Franjas generadas");
    }

    static DetalleProfesional detalle() {
        return new DetalleProfesional(profesional(), new String[] {V}, new String[] {U});
    }

    @BeforeEach
    void preparar() {
        autenticar("ADMINISTRATIVO");
        mvc = MockMvcBuilders.standaloneSetup(
                        new ProfesionalController(profesionalUseCase, centroUseCase, horarioUseCase),
                        new CentroController(centroUseCase),
                        new EspecialidadController(especialidadUseCase, profesionalUseCase),
                        new HorarioController(horarioUseCase), new BloqueoController(horarioUseCase),
                        new ParametroController(parametroUseCase), new FestivoController(parametroUseCase),
                        new InternalController(profesionalUseCase, parametroUseCase))
                .setControllerAdvice(new GlobalExceptionHandler()).setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper().findAndRegisterModules())).build();
    }

    void autenticar(String rolNuevo) {
        rol = rolNuevo;
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(new UsuarioAutenticado(U, rolNuevo), null, List.of()));
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void profesionalesCrudYAsociaciones() throws Exception {
        when(profesionalUseCase.crearProfesional(any())).thenReturn(profesional());
        when(profesionalUseCase.actualizarProfesional(any())).thenReturn(profesional());
        when(profesionalUseCase.consultarProfesional(U)).thenReturn(detalle());
        when(profesionalUseCase.asociarEspecialidades(any())).thenReturn(detalle());
        when(profesionalUseCase.asociarCentros(any())).thenReturn(detalle());
        when(centroUseCase.listarCentrosDeProfesional(U)).thenReturn(new Centro[] {centro()});
        String cuerpo = "{\"nombres\":\"Ana\",\"apellidos\":\"Perez\",\"registroMedico\":\"RM-1\"}";
        mvc.perform(post("/api/v1/profesionales").contentType("application/json").content("{\"usuarioId\":\"" + V + "\"," + cuerpo.substring(1)))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.registroMedico").value("RM-1"));
        mvc.perform(put("/api/v1/profesionales/" + U).contentType("application/json").content(cuerpo)).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/profesionales/" + U)).andExpect(status().isNoContent());
        verify(profesionalUseCase).desactivarProfesional(U);
        mvc.perform(get("/api/v1/profesionales/" + U)).andExpect(status().isOk()).andExpect(jsonPath("$.especialidadIds[0]").value(V));
        mvc.perform(get("/api/v1/profesionales/" + U + "/centros")).andExpect(status().isOk()).andExpect(jsonPath("$[0].ciudad").value("Bogota"));
        mvc.perform(put("/api/v1/profesionales/" + U + "/especialidades").contentType("application/json").content("{\"ids\":[\"" + V + "\"]}")).andExpect(status().isOk());
        mvc.perform(put("/api/v1/profesionales/" + U + "/centros").contentType("application/json").content("{\"ids\":[]}")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/profesionales").contentType("application/json").content("{}")).andExpect(status().isBadRequest());
    }

    @Test
    void listadoPaginadoDeProfesionales() throws Exception {
        when(profesionalUseCase.listarProfesionales(any(), eq(1), eq(5))).thenReturn(new PaginaProfesionales(new Profesional[] {profesional()}, 1, 5, 6, 2));
        mvc.perform(get("/api/v1/profesionales?page=1&size=5&texto=ana&activo=true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalPaginas").value(2)).andExpect(jsonPath("$.contenido[0].nombres").value("Ana"));
        ArgumentCaptor<FiltroProfesionales> filtro = ArgumentCaptor.forClass(FiltroProfesionales.class);
        verify(profesionalUseCase).listarProfesionales(filtro.capture(), eq(1), eq(5));
        org.junit.jupiter.api.Assertions.assertEquals("ana", filtro.getValue().texto());
        org.junit.jupiter.api.Assertions.assertEquals(Boolean.TRUE, filtro.getValue().activo());
    }

    @Test
    void horariosDeUnProfesional() throws Exception {
        when(horarioUseCase.listarHorarios(U)).thenReturn(new HorarioAtencion[] {horario()});
        when(horarioUseCase.definirHorario(any())).thenReturn(resultado());
        mvc.perform(get("/api/v1/profesionales/" + U + "/horarios")).andExpect(status().isOk()).andExpect(jsonPath("$[0].horaInicio").value("08:00"));
        mvc.perform(post("/api/v1/profesionales/" + U + "/horarios").contentType("application/json")
                .content("{\"centroId\":\"" + U + "\",\"diaSemana\":1,\"horaInicio\":\"08:00\",\"horaFin\":\"09:00\",\"duracionFranjaMin\":30}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.franjasCreadas").value(4));
        mvc.perform(post("/api/v1/profesionales/" + U + "/horarios").contentType("application/json")
                .content("{\"centroId\":\"" + U + "\",\"diaSemana\":9,\"horaInicio\":\"8am\",\"horaFin\":\"09:00\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void centros() throws Exception {
        when(centroUseCase.crearCentro(any())).thenReturn(centro());
        when(centroUseCase.actualizarCentro(any())).thenReturn(centro());
        String cuerpo = "{\"nombre\":\"Sede\",\"direccion\":\"Calle 1\",\"ciudad\":\"Bogota\",\"telefono\":\"3001234567\"}";
        mvc.perform(post("/api/v1/centros").contentType("application/json").content(cuerpo)).andExpect(status().isCreated()).andExpect(jsonPath("$.nombre").value("Sede"));
        mvc.perform(put("/api/v1/centros/" + U).contentType("application/json").content(cuerpo)).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/centros/" + U)).andExpect(status().isNoContent());
        verify(centroUseCase).desactivarCentro(U);
        mvc.perform(post("/api/v1/centros").contentType("application/json").content("{\"nombre\":\"\"}")).andExpect(status().isBadRequest());
        when(centroUseCase.listarCentros(false)).thenReturn(new Centro[] {centro()});
        when(centroUseCase.listarCentros(true)).thenReturn(new Centro[] {centro(), centro()});
        mvc.perform(get("/api/v1/centros")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        mvc.perform(get("/api/v1/centros?incluirInactivos=true")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void especialidades() throws Exception {
        when(especialidadUseCase.listarEspecialidades(false)).thenReturn(new Especialidad[] {new Especialidad(U, "Cardiologia", null, true)});
        when(especialidadUseCase.listarEspecialidades(true)).thenReturn(new Especialidad[0]);
        when(profesionalUseCase.listarProfesionalesPorEspecialidad(U)).thenReturn(new Profesional[] {profesional()});
        when(especialidadUseCase.crearEspecialidad(any())).thenReturn(new Especialidad(U, "Cardiologia", null, true));
        when(especialidadUseCase.actualizarEspecialidad(any())).thenReturn(new Especialidad(U, "Cardiologia", "x", true));
        mvc.perform(get("/api/v1/especialidades")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Cardiologia"));
        mvc.perform(get("/api/v1/especialidades/" + U + "/profesionales")).andExpect(status().isOk()).andExpect(jsonPath("$[0].apellidos").value("Perez"));
        mvc.perform(post("/api/v1/especialidades").contentType("application/json").content("{\"nombre\":\"Cardiologia\"}")).andExpect(status().isCreated());
        mvc.perform(put("/api/v1/especialidades/" + U).contentType("application/json").content("{\"nombre\":\"Cardiologia\",\"descripcion\":\"x\"}")).andExpect(status().isOk());
        mvc.perform(delete("/api/v1/especialidades/" + U)).andExpect(status().isNoContent());
        mvc.perform(get("/api/v1/especialidades?incluirInactivas=true")).andExpect(status().isOk());
    }

    @Test
    void soloElAdministrativoVeLasEspecialidadesInactivas() throws Exception {
        autenticar("PACIENTE");
        // sin proxies de seguridad en este montaje, el controlador lanza AccessDeniedException y el manejador la traduce a 403
        mvc.perform(get("/api/v1/especialidades?incluirInactivas=true")).andExpect(status().isForbidden()).andExpect(jsonPath("$.codigo").value("ACCESO_DENEGADO"));
    }

    @Test
    void horariosBloqueosYParametros() throws Exception {
        when(horarioUseCase.modificarHorario(any())).thenReturn(resultado());
        when(horarioUseCase.generarFranjas(U)).thenReturn(resultado());
        when(horarioUseCase.bloquearFranjas(any())).thenReturn(new Bloqueo(U, V, U, "2026-11-20", "2026-11-21", "Congreso", U, 1000));
        when(parametroUseCase.consultarParametros()).thenReturn(new ParametroServicio[] {new ParametroServicio("horizonte_max_dias", "60", "d")});
        when(parametroUseCase.configurarParametros(any())).thenReturn(new ParametroServicio[] {new ParametroServicio("horizonte_max_dias", "30", "d")});
        mvc.perform(put("/api/v1/horarios/" + U).contentType("application/json")
                .content("{\"centroId\":\"" + U + "\",\"diaSemana\":2,\"horaInicio\":\"08:00\",\"horaFin\":\"09:00\",\"activo\":true}")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/horarios/" + U + "/franjas")).andExpect(status().isOk()).andExpect(jsonPath("$.franjasGeneradas").value(true));
        mvc.perform(post("/api/v1/bloqueos").contentType("application/json")
                .content("{\"profesionalId\":\"" + V + "\",\"centroId\":\"" + U + "\",\"fechaInicio\":\"2026-11-20\",\"fechaFin\":\"2026-11-21\",\"motivo\":\"Congreso\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.motivo").value("Congreso"));
        mvc.perform(get("/api/v1/parametros")).andExpect(status().isOk()).andExpect(jsonPath("$[0].clave").value("horizonte_max_dias"));
        mvc.perform(put("/api/v1/parametros").contentType("application/json").content("{\"parametros\":{\"horizonte_max_dias\":\"30\"}}")).andExpect(status().isOk());
        mvc.perform(put("/api/v1/parametros").contentType("application/json").content("{\"parametros\":{}}")).andExpect(status().isBadRequest());
    }

    @Test
    void conflictoDeBloqueoYAppointmentCaido() throws Exception {
        when(horarioUseCase.bloquearFranjas(any())).thenThrow(new ConflictoException("CITAS_AFECTADAS", "hay citas"));
        when(horarioUseCase.generarFranjas(U)).thenThrow(new ServicioNoDisponibleException("APPOINTMENT_NO_DISPONIBLE", "caido"));
        mvc.perform(post("/api/v1/bloqueos").contentType("application/json")
                .content("{\"profesionalId\":\"" + V + "\",\"centroId\":\"" + U + "\",\"fechaInicio\":\"2026-11-20\",\"fechaFin\":\"2026-11-21\",\"motivo\":\"x\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/v1/horarios/" + U + "/franjas")).andExpect(status().isServiceUnavailable());
    }

    @Test
    void festivosDeLaApiExterna() throws Exception {
        when(parametroUseCase.consultarFestivos(2026)).thenReturn(new Festivo[] {new Festivo("2026-01-01", "Anio Nuevo")});
        when(parametroUseCase.consultarFestivos(2027)).thenThrow(new ServicioNoDisponibleException("FESTIVOS_NO_DISPONIBLES", "x"));
        mvc.perform(get("/api/v1/festivos?anio=2026")).andExpect(status().isOk()).andExpect(jsonPath("$[0].nombre").value("Anio Nuevo"));
        mvc.perform(get("/api/v1/festivos?anio=2027")).andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.codigo").value("FESTIVOS_NO_DISPONIBLES"));
        mvc.perform(get("/api/v1/festivos")).andExpect(status().isBadRequest());
    }

    @Test
    void consultasInternas() throws Exception {
        when(profesionalUseCase.validarAsociacion(U, V, U)).thenReturn(new ProfesionalUseCase.ValidarAsociacionResultado(true, true));
        when(profesionalUseCase.buscarProfesionalPorUsuario(V)).thenReturn(profesional());
        when(parametroUseCase.consultarParametros()).thenReturn(new ParametroServicio[] {new ParametroServicio("k", "v", null)});
        mvc.perform(get("/internal/profesionales/" + U + "/asociacion?especialidadId=" + V + "&centroId=" + U))
                .andExpect(status().isOk()).andExpect(jsonPath("$.asociado").value(true));
        mvc.perform(get("/internal/profesionales/por-usuario/" + V)).andExpect(status().isOk()).andExpect(jsonPath("$.profesionalId").value(U));
        mvc.perform(get("/internal/profesionales/por-usuario/" + U)).andExpect(status().isNotFound()).andExpect(jsonPath("$.codigo").value("PROFESIONAL_NO_ENCONTRADO"));
        mvc.perform(get("/internal/parametros")).andExpect(status().isOk()).andExpect(jsonPath("$.k").value("v"));
    }
}
