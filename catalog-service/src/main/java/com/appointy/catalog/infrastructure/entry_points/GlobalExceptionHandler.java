package com.appointy.catalog.infrastructure.entry_points;

import com.appointy.catalog.domain.exception.ConflictoException;
import com.appointy.catalog.domain.exception.DomainException;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.exception.ServicioNoDisponibleException;
import com.appointy.catalog.domain.exception.ValidacionException;
import com.appointy.catalog.infrastructure.entry_points.dto.RespuestaError;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import java.time.Instant;
import java.util.List;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** Toda respuesta de error sale con la estructura de {@link RespuestaError} y sin datos sensibles (RNF-05, RNF-10). */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    ResponseEntity<RespuestaError> dominio(DomainException ex) {
        return responder(estadoDe(ex), ex.codigo(), ex.getMessage(), List.of(ex.detalle()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<RespuestaError> accesoDenegado(AccessDeniedException ex) {
        return responder(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO", "No tienes permiso para esta operacion", List.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<RespuestaError> noAutenticado(AuthenticationException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "NO_AUTENTICADO", "Se requiere un token valido", List.of());
    }

    /** Solo se traducen a 409 las restricciones conocidas; cualquier otra violacion no se disfraza de conflicto. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<RespuestaError> integridad(DataIntegrityViolationException ex) {
        String restriccion = ex.getCause() instanceof ConstraintViolationException cv ? cv.getConstraintName() : null;
        if (restriccion != null) {
            if (restriccion.contains("uq_especialidad_nombre")) {
                return responder(HttpStatus.CONFLICT, "ESPECIALIDAD_YA_EXISTE", "Ya existe una especialidad con ese nombre", List.of());
            }
            if (restriccion.contains("uq_profesional_usuario")) {
                return responder(HttpStatus.CONFLICT, "PROFESIONAL_YA_EXISTE", "Esa cuenta ya tiene un perfil de profesional", List.of());
            }
            if (restriccion.contains("uq_profesional_registro")) {
                return responder(HttpStatus.CONFLICT, "REGISTRO_MEDICO_YA_REGISTRADO", "El registro medico ya esta registrado", List.of());
            }
            if (restriccion.contains("fk_horario_profesional_centro") || restriccion.contains("fk_bloqueo_profesional_centro")) {
                return responder(HttpStatus.CONFLICT, "CENTRO_CON_HORARIOS", "El centro tiene horarios o bloqueos del profesional", List.of());
            }
        }
        LOG.error("Violacion de integridad no prevista (restriccion={})", restriccion, ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrio un error inesperado", List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<RespuestaError> inesperado(Exception ex) {
        LOG.error("Error no controlado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO", "Ocurrio un error inesperado", List.of());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<String> detalle = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .sorted()
                .toList();
        return objeto(HttpStatus.BAD_REQUEST, "VALIDACION_ENTRADA", "Hay datos invalidos en la solicitud", detalle);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        if (ex.getCause() instanceof UnrecognizedPropertyException campo) {
            return objeto(HttpStatus.BAD_REQUEST, "CAMPO_NO_PERMITIDO", "La solicitud incluye un campo que no se puede enviar",
                    List.of("campo: " + campo.getPropertyName()));
        }
        return objeto(HttpStatus.BAD_REQUEST, "SOLICITUD_ILEGIBLE", "El cuerpo de la solicitud no es un JSON valido o tiene valores con formato incorrecto", List.of());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        HttpStatus estado = HttpStatus.resolve(statusCode.value());
        String mensaje = body instanceof ProblemDetail problema && problema.getDetail() != null
                ? problema.getDetail()
                : (estado != null ? estado.getReasonPhrase() : "Error");
        String codigo = estado != null ? estado.name() : "ERROR";
        return new ResponseEntity<>(new RespuestaError(codigo, mensaje, List.of(), Instant.now()), headers, statusCode);
    }

    private static HttpStatus estadoDe(DomainException ex) {
        if (ex instanceof ValidacionException) return HttpStatus.BAD_REQUEST;
        if (ex instanceof ConflictoException) return HttpStatus.CONFLICT;
        if (ex instanceof NoEncontradoException) return HttpStatus.NOT_FOUND;
        if (ex instanceof ServicioNoDisponibleException) return HttpStatus.SERVICE_UNAVAILABLE;
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }

    private static ResponseEntity<RespuestaError> responder(HttpStatus estado, String codigo, String mensaje, List<String> detalle) {
        return ResponseEntity.status(estado).body(new RespuestaError(codigo, mensaje, detalle, Instant.now()));
    }

    private static ResponseEntity<Object> objeto(HttpStatus estado, String codigo, String mensaje, List<String> detalle) {
        return ResponseEntity.status(estado).body(new RespuestaError(codigo, mensaje, detalle, Instant.now()));
    }
}
