package com.appointy.appointment.infrastructure.entry_points;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.appointy.appointment.domain.exception.*;
import com.appointy.appointment.infrastructure.entry_points.dto.RespuestaError;
import com.appointy.appointment.infrastructure.security.JsonErrorWriter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class ManejoDeErroresTest {

    record Cuerpo(@NotBlank String nombre) {}

    @RestController
    static class Prueba {
        @PostMapping("/prueba")
        String crear(@Valid @RequestBody Cuerpo c) {
            return c.nombre();
        }

        @GetMapping("/numero")
        String numero(@RequestParam int n) {
            return "ok";
        }

        @GetMapping("/falla")
        String falla() {
            throw new IllegalStateException("detalle interno");
        }
    }

    final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    MockMvc mvc;

    @BeforeEach
    void preparar() {
        ObjectMapper estricto = new ObjectMapper().findAndRegisterModules().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mvc = MockMvcBuilders.standaloneSetup(new Prueba()).setControllerAdvice(handler)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(estricto)).build();
    }

    static void estado(ResponseEntity<RespuestaError> r, HttpStatus esperado, String codigo) {
        assertEquals(esperado, r.getStatusCode());
        assertEquals(codigo, r.getBody().codigo());
        assertTrue(r.getBody().timestamp() != null);
    }

    @Test
    void lasExcepcionesDeDominioSeTraducenASuEstado() {
        estado(handler.dominio(new ValidacionException("V", "m")), HttpStatus.BAD_REQUEST, "V");
        estado(handler.dominio(new NoEncontradoException("N", "m")), HttpStatus.NOT_FOUND, "N");
        estado(handler.dominio(new ConflictoException("C", "m")), HttpStatus.CONFLICT, "C");
        estado(handler.dominio(new ServicioNoDisponibleException("S", "m")), HttpStatus.SERVICE_UNAVAILABLE, "S");
        estado(handler.dominio(new AccesoProhibidoException("A", "m")), HttpStatus.FORBIDDEN, "A");
        estado(handler.dominio(new ReglaNegocioException("R", "m")), HttpStatus.UNPROCESSABLE_ENTITY, "R");
    }

    @Test
    void seguridadYErroresInesperados() {
        estado(handler.accesoDenegado(new AccessDeniedException("x")), HttpStatus.FORBIDDEN, "ACCESO_DENEGADO");
        estado(handler.noAutenticado(new BadCredentialsException("x")), HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO");
        estado(handler.integridad(new DataIntegrityViolationException("x")), HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO");
        estado(handler.inesperado(new RuntimeException("secreto")), HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO");
    }

    @Test
    void validacionDeEntradaListaLosCampos() throws Exception {
        mvc.perform(post("/prueba").contentType("application/json").content("{\"nombre\":\" \"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("VALIDACION_ENTRADA"))
                .andExpect(jsonPath("$.detalle[0]").value(org.hamcrest.Matchers.startsWith("nombre:")));
    }

    @Test
    void jsonIlegibleYCamposDesconocidos() throws Exception {
        mvc.perform(post("/prueba").contentType("application/json").content("{no es json"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("SOLICITUD_ILEGIBLE"));
        mvc.perform(post("/prueba").contentType("application/json").content("{\"nombre\":\"a\",\"extra\":1}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.codigo").value("CAMPO_NO_PERMITIDO"))
                .andExpect(jsonPath("$.detalle[0]").value("campo: extra"));
    }

    @Test
    void otrosErroresDeSpringConservanLaEstructura() throws Exception {
        mvc.perform(get("/numero?n=abc")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.timestamp").exists());
        mvc.perform(get("/prueba")).andExpect(status().isMethodNotAllowed()).andExpect(jsonPath("$.codigo").value("METHOD_NOT_ALLOWED"));
        mvc.perform(get("/falla")).andExpect(status().isInternalServerError()).andExpect(jsonPath("$.codigo").value("ERROR_INTERNO"))
                .andExpect(jsonPath("$.mensaje").value("Ocurrio un error inesperado"));
    }

    @Test
    void elEscritorDeErroresDeSeguridadUsaLaMismaEstructura() throws Exception {
        MockHttpServletResponse res = new MockHttpServletResponse();
        new JsonErrorWriter(new ObjectMapper().findAndRegisterModules()).escribir(res, 401, "NO_AUTENTICADO", "Se requiere un token valido");
        assertEquals(401, res.getStatus());
        assertTrue(res.getContentAsString().contains("\"codigo\":\"NO_AUTENTICADO\""));
    }
}
