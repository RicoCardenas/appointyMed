package com.appointy.auth.infrastructure.entry_points;

import com.appointy.auth.domain.port.in.ActualizarDatosEditablesPort;
import com.appointy.auth.domain.port.in.ConsultarPerfilPort;
import com.appointy.auth.infrastructure.entry_points.dto.ActualizarContactoRequest;
import com.appointy.auth.infrastructure.entry_points.dto.PacienteResponse;
import com.appointy.auth.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Perfil del propio paciente: el identificador sale siempre del token, nunca de la URL (RNF-05). */
@RestController
@RequestMapping("/api/v1/pacientes/me")
@PreAuthorize("hasRole('PACIENTE')")
public class PacienteController {

    private final ConsultarPerfilPort consultarPerfil;
    private final ActualizarDatosEditablesPort actualizarDatos;

    public PacienteController(ConsultarPerfilPort consultarPerfil, ActualizarDatosEditablesPort actualizarDatos) {
        this.consultarPerfil = consultarPerfil;
        this.actualizarDatos = actualizarDatos;
    }

    @GetMapping
    public PacienteResponse consultar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return PacienteResponse.de(consultarPerfil.ejecutar(usuario.usuarioId()));
    }

    @PatchMapping
    public PacienteResponse actualizar(
            @AuthenticationPrincipal UsuarioAutenticado usuario, @Valid @RequestBody ActualizarContactoRequest solicitud) {
        return PacienteResponse.de(actualizarDatos.ejecutar(solicitud.aComando(usuario.usuarioId())));
    }
}
