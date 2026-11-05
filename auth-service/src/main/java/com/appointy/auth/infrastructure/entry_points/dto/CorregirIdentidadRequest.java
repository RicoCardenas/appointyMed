package com.appointy.auth.infrastructure.entry_points.dto;

import com.appointy.auth.domain.port.in.CorregirDatosInmutablesPort;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CorregirIdentidadRequest(
        @Pattern(regexp = "^[0-9A-Za-z-]{5,20}$", message = "debe tener de 5 a 20 caracteres alfanumericos") String cedula,
        @Size(min = 1, max = 100) String nombres,
        @Size(min = 1, max = 100) String apellidos,
        @Past LocalDate fechaNacimiento) {

    public CorregirDatosInmutablesPort.Comando aComando(UUID actorId, UUID pacienteUsuarioId) {
        return new CorregirDatosInmutablesPort.Comando(actorId, pacienteUsuarioId, cedula, nombres, apellidos, fechaNacimiento);
    }
}
