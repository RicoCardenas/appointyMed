package com.appointy.auth.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.auth.domain.usecase.CuentaUseCase;
import com.appointy.auth.domain.usecase.SesionUseCase;
import com.appointy.auth.infrastructure.entry_points.dto.CambiarClaveRequest;
import com.appointy.auth.infrastructure.entry_points.dto.LoginRequest;
import com.appointy.auth.infrastructure.entry_points.dto.LoginResponse;
import com.appointy.auth.infrastructure.entry_points.dto.PacienteResponse;
import com.appointy.auth.infrastructure.entry_points.dto.RegistroPacienteRequest;
import com.appointy.auth.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticación")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final CuentaUseCase cuentas;
    private final SesionUseCase sesiones;

    public AuthController(CuentaUseCase cuentas, SesionUseCase sesiones) {
        this.cuentas = cuentas;
        this.sesiones = sesiones;
    }

    @Operation(summary = "Registrar un paciente")
    @SecurityRequirements
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteResponse registrar(@Valid @RequestBody RegistroPacienteRequest solicitud) {
        return PacienteResponse.de(cuentas.registrarPaciente(solicitud.aComando()));
    }

    @Operation(summary = "Iniciar sesión y obtener el JWT")
    @SecurityRequirements
    @PostMapping("/login")
    public LoginResponse iniciarSesion(@Valid @RequestBody LoginRequest solicitud) {
        return LoginResponse.de(sesiones.iniciarSesion(solicitud.aComando()));
    }

    @Operation(summary = "Cerrar sesión y revocar el token actual")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cerrarSesion(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        sesiones.cerrarSesion(new SesionUseCase.CerrarSesionComando(usuario.jti(), usuario.expiraEn()));
    }

    @Operation(summary = "Cambiar la clave propia")
    @PostMapping("/cambiar-clave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cambiarClave(@AuthenticationPrincipal UsuarioAutenticado usuario, @Valid @RequestBody CambiarClaveRequest solicitud) {
        cuentas.cambiarClave(new CuentaUseCase.CambiarClaveComando(usuario.usuarioId(), solicitud.claveActual(), solicitud.claveNueva()));
    }
}
