package com.appointy.auth.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.auth.domain.usecase.PerfilUseCase;
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
@Tag(name = "Perfil del paciente")
@RestController
@RequestMapping("/api/v1/pacientes/me")
@PreAuthorize("hasRole('PACIENTE')")
public class PacienteController {

    private final PerfilUseCase perfiles;

    public PacienteController(PerfilUseCase perfiles) {
        this.perfiles = perfiles;
    }

    @Operation(summary = "Consultar el perfil propio")
    @GetMapping
    public PacienteResponse consultar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return PacienteResponse.de(perfiles.consultarPerfil(usuario.usuarioId()));
    }

    @Operation(summary = "Actualizar correo, teléfono, dirección o ciudad")
    @PatchMapping
    public PacienteResponse actualizar(
            @AuthenticationPrincipal UsuarioAutenticado usuario, @Valid @RequestBody ActualizarContactoRequest solicitud) {
        return PacienteResponse.de(perfiles.actualizarDatosEditables(solicitud.aComando(usuario.usuarioId())));
    }
}
