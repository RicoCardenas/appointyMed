package com.appointy.auth.infrastructure.entry_points;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.appointy.auth.domain.exception.CredencialesInvalidasException;
import com.appointy.auth.domain.model.EstadisticasUsuarios;
import com.appointy.auth.domain.model.FiltroUsuarios;
import com.appointy.auth.domain.model.PacienteConCuenta;
import com.appointy.auth.domain.model.PacientePerfil;
import com.appointy.auth.domain.model.PaginaUsuarios;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.domain.model.Usuario;
import com.appointy.auth.domain.usecase.*;
import com.appointy.auth.infrastructure.security.UsuarioAutenticado;
import com.fasterxml.jackson.databind.DeserializationFeature;
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

class ControladoresTest {

    static final String U = "11111111-1111-1111-1111-111111111111";
    static final String V = "22222222-2222-2222-2222-222222222222";

    final RegistrarPacienteUseCase registrar = mock(RegistrarPacienteUseCase.class);
    final IniciarSesionUseCase login = mock(IniciarSesionUseCase.class);
    final CerrarSesionUseCase logout = mock(CerrarSesionUseCase.class);
    final CambiarClaveUseCase cambiarClave = mock(CambiarClaveUseCase.class);
    final ConsultarPerfilUseCase perfil = mock(ConsultarPerfilUseCase.class);
    final ActualizarDatosEditablesUseCase actualizar = mock(ActualizarDatosEditablesUseCase.class);
    final CrearCuentaStaffUseCase crearCuenta = mock(CrearCuentaStaffUseCase.class);
    final CorregirDatosInmutablesUseCase corregir = mock(CorregirDatosInmutablesUseCase.class);
    final ListarUsuariosUseCase listar = mock(ListarUsuariosUseCase.class);
    final ConsultarEstadisticasUsuariosUseCase estadisticas = mock(ConsultarEstadisticasUsuariosUseCase.class);
    final CambiarEstadoCuentaUseCase cambiarEstado = mock(CambiarEstadoCuentaUseCase.class);
    final ConsultarContactoUseCase contacto = mock(ConsultarContactoUseCase.class);
    MockMvc mvc;

    static Usuario usuario(boolean activo) {
        return new Usuario(U, "ana@correo.co", "hash", Rol.PROFESIONAL, activo, 1000, 2000, 0L);
    }

    static PacienteConCuenta paciente() {
        return new PacienteConCuenta(new Usuario(U, "ana@correo.co", "hash", Rol.PACIENTE, true, 1, 1, 0L),
                new PacientePerfil(U, "12345", "Ana", "Perez", "1990-05-01", "3001234567", "Calle 1", "Bogota"));
    }

    @BeforeEach
    void preparar() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(new UsuarioAutenticado(U, Rol.ADMINISTRATIVO, "jti-1", 9000), null, List.of()));
        ObjectMapper estricto = new ObjectMapper().findAndRegisterModules().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mvc = MockMvcBuilders.standaloneSetup(new AuthController(registrar, login, logout, cambiarClave), new PacienteController(perfil, actualizar),
                        new AdminController(crearCuenta, corregir, listar, estadisticas, cambiarEstado), new InternalController(contacto))
                .setControllerAdvice(new GlobalExceptionHandler()).setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(estricto)).build();
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void registroLoginYLogout() throws Exception {
        when(registrar.ejecutar(any())).thenReturn(paciente());
        when(login.ejecutar(any())).thenReturn(new IniciarSesionUseCase.Resultado("tok", 5000, U, Rol.PACIENTE));
        String registro = "{\"correo\":\"ana@correo.co\",\"clave\":\"Clave1234\",\"cedula\":\"12345\",\"nombres\":\"Ana\",\"apellidos\":\"Perez\",\"fechaNacimiento\":\"1990-05-01\"}";
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content(registro))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.cedula").value("12345")).andExpect(jsonPath("$.ciudad").value("Bogota"));
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content("{\"correo\":\"a@b.co\",\"clave\":\"x\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").value("tok")).andExpect(jsonPath("$.tokenType").value("Bearer"));
        mvc.perform(post("/api/v1/auth/logout")).andExpect(status().isNoContent());
        ArgumentCaptor<CerrarSesionUseCase.Comando> comando = ArgumentCaptor.forClass(CerrarSesionUseCase.Comando.class);
        verify(logout).ejecutar(comando.capture());
        org.junit.jupiter.api.Assertions.assertEquals("jti-1", comando.getValue().jti());
        mvc.perform(post("/api/v1/auth/register").contentType("application/json").content("{\"correo\":\"x\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void credencialesInvalidasSon401() throws Exception {
        when(login.ejecutar(any())).thenThrow(new CredencialesInvalidasException());
        mvc.perform(post("/api/v1/auth/login").contentType("application/json").content("{\"correo\":\"a@b.co\",\"clave\":\"x\"}"))
                .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.codigo").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void cambiarLaClaveDelUsuarioAutenticado() throws Exception {
        mvc.perform(post("/api/v1/auth/cambiar-clave").contentType("application/json").content("{\"claveActual\":\"Actual123\",\"claveNueva\":\"Nueva456\"}"))
                .andExpect(status().isNoContent());
        ArgumentCaptor<CambiarClaveUseCase.Comando> comando = ArgumentCaptor.forClass(CambiarClaveUseCase.Comando.class);
        verify(cambiarClave).ejecutar(comando.capture());
        org.junit.jupiter.api.Assertions.assertEquals(U, comando.getValue().usuarioId());
        mvc.perform(post("/api/v1/auth/cambiar-clave").contentType("application/json").content("{\"claveActual\":\"\"}")).andExpect(status().isBadRequest());
    }

    @Test
    void perfilDelPaciente() throws Exception {
        when(perfil.ejecutar(U)).thenReturn(paciente());
        when(actualizar.ejecutar(any())).thenReturn(paciente());
        mvc.perform(get("/api/v1/pacientes/me")).andExpect(status().isOk()).andExpect(jsonPath("$.nombres").value("Ana"));
        mvc.perform(patch("/api/v1/pacientes/me").contentType("application/json").content("{\"telefono\":\"3001234567\"}")).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/pacientes/me").contentType("application/json").content("{\"cedula\":\"999\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("CAMPO_NO_PERMITIDO"));
    }

    @Test
    void administracionDeCuentas() throws Exception {
        when(crearCuenta.ejecutar(any())).thenReturn(usuario(true));
        when(corregir.ejecutar(any())).thenReturn(paciente());
        mvc.perform(post("/api/v1/admin/usuarios").contentType("application/json").content("{\"correo\":\"doc@correo.co\",\"clave\":\"Clave1234\",\"rol\":\"PROFESIONAL\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.rol").value("PROFESIONAL"));
        mvc.perform(patch("/api/v1/admin/pacientes/" + V + "/identidad").contentType("application/json").content("{\"nombres\":\"Ana Maria\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void listadoPaginadoEstadisticasYCambioDeEstado() throws Exception {
        when(listar.ejecutar(any(FiltroUsuarios.class), eq(1), eq(5))).thenReturn(new PaginaUsuarios(new Usuario[] {usuario(true)}, 1, 5, 6, 2));
        when(estadisticas.ejecutar()).thenReturn(new EstadisticasUsuarios(5, 2, 1, 7, 1));
        when(cambiarEstado.ejecutar(any())).thenReturn(usuario(false));
        mvc.perform(get("/api/v1/admin/usuarios?page=1&size=5&rol=PROFESIONAL&activo=true"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.totalElementos").value(6)).andExpect(jsonPath("$.contenido[0].correo").value("ana@correo.co"));
        ArgumentCaptor<FiltroUsuarios> filtro = ArgumentCaptor.forClass(FiltroUsuarios.class);
        verify(listar).ejecutar(filtro.capture(), eq(1), eq(5));
        org.junit.jupiter.api.Assertions.assertEquals(Rol.PROFESIONAL, filtro.getValue().rol());
        org.junit.jupiter.api.Assertions.assertEquals(Boolean.TRUE, filtro.getValue().activo());
        mvc.perform(get("/api/v1/admin/usuarios/estadisticas")).andExpect(status().isOk()).andExpect(jsonPath("$.inactivos").value(1));
        mvc.perform(post("/api/v1/admin/usuarios/" + V + "/desactivar")).andExpect(status().isOk()).andExpect(jsonPath("$.activo").value(false));
        mvc.perform(post("/api/v1/admin/usuarios/" + V + "/activar")).andExpect(status().isOk());
        mvc.perform(post("/api/v1/admin/usuarios/no-uuid/activar")).andExpect(status().isBadRequest());
    }

    @Test
    void contactoInterno() throws Exception {
        when(contacto.ejecutar(V)).thenReturn("ana@correo.co");
        mvc.perform(get("/internal/usuarios/" + V + "/contacto")).andExpect(status().isOk()).andExpect(jsonPath("$.correo").value("ana@correo.co"));
    }
}
