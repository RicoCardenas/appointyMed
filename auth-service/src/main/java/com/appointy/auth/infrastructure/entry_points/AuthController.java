package com.appointy.auth.infrastructure.entry_points;

import com.appointy.auth.domain.port.in.CerrarSesionPort;
import com.appointy.auth.domain.port.in.IniciarSesionPort;
import com.appointy.auth.domain.port.in.RegistrarPacientePort;
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

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegistrarPacientePort registrarPaciente;
    private final IniciarSesionPort iniciarSesion;
    private final CerrarSesionPort cerrarSesion;

    public AuthController(RegistrarPacientePort registrarPaciente, IniciarSesionPort iniciarSesion, CerrarSesionPort cerrarSesion) {
        this.registrarPaciente = registrarPaciente;
        this.iniciarSesion = iniciarSesion;
        this.cerrarSesion = cerrarSesion;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteResponse registrar(@Valid @RequestBody RegistroPacienteRequest solicitud) {
        return PacienteResponse.de(registrarPaciente.ejecutar(solicitud.aComando()));
    }

    @PostMapping("/login")
    public LoginResponse iniciarSesion(@Valid @RequestBody LoginRequest solicitud) {
        return LoginResponse.de(iniciarSesion.ejecutar(solicitud.aComando()));
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cerrarSesion(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        cerrarSesion.ejecutar(new CerrarSesionPort.Comando(usuario.jti(), usuario.expiraEn()));
    }
}
