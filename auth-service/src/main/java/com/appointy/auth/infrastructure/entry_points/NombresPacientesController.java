package com.appointy.auth.infrastructure.entry_points;

import com.appointy.auth.domain.model.NombrePaciente;
import com.appointy.auth.domain.usecase.PerfilUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Nombres de pacientes por id, para profesionales y administrativos. Devuelve solo nombres y apellidos; los ids que un profesional
 * conoce salen de sus propias citas y atenciones.
 */
@Tag(name = "Nombres de pacientes")
@RestController
@RequestMapping("/api/v1/pacientes/nombres")
@PreAuthorize("hasAnyRole('PROFESIONAL', 'ADMINISTRATIVO')")
public class NombresPacientesController {

    private final PerfilUseCase perfiles;

    public NombresPacientesController(PerfilUseCase perfiles) {
        this.perfiles = perfiles;
    }

    @Operation(summary = "Nombres de hasta 50 pacientes (PROFESIONAL o ADMINISTRATIVO)")
    @PostMapping
    public List<Nombre> nombres(@Valid @RequestBody Solicitud solicitud) {
        NombrePaciente[] r = perfiles.consultarNombresPacientes(solicitud.ids().stream().map(UUID::toString).toArray(String[]::new));
        return Arrays.stream(r).map(n -> new Nombre(n.usuarioId(), n.nombres(), n.apellidos())).toList();
    }

    public record Solicitud(@NotEmpty @Size(max = 50) List<UUID> ids) {}

    public record Nombre(String usuarioId, String nombres, String apellidos) {}
}
