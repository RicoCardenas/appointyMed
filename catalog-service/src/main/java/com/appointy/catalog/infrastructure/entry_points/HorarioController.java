package com.appointy.catalog.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.catalog.domain.usecase.GenerarFranjasUseCase;
import com.appointy.catalog.domain.usecase.ModificarHorarioUseCase;
import com.appointy.catalog.infrastructure.entry_points.dto.ModificarHorarioRequest;
import com.appointy.catalog.infrastructure.entry_points.dto.ResultadoHorarioResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Horarios")
@RestController
@RequestMapping("/api/v1/horarios")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class HorarioController {

    private final ModificarHorarioUseCase modificar;
    private final GenerarFranjasUseCase generarFranjas;

    public HorarioController(ModificarHorarioUseCase modificar, GenerarFranjasUseCase generarFranjas) {
        this.modificar = modificar;
        this.generarFranjas = generarFranjas;
    }

    /** CU-14. */
    @Operation(summary = "Modificar un horario y regenerar sus franjas")
    @PutMapping("/{id}")
    public ResultadoHorarioResponse modificar(@PathVariable UUID id, @Valid @RequestBody ModificarHorarioRequest s) {
        return ResultadoHorarioResponse.de(modificar.ejecutar(s.aComando(id.toString())));
    }

    /** Reintento idempotente de la generacion de franjas; 503 si appointment sigue sin responder. */
    @Operation(summary = "Reintentar la generación de franjas de un horario (idempotente)")
    @PostMapping("/{id}/franjas")
    public ResultadoHorarioResponse generarFranjas(@PathVariable UUID id) {
        return ResultadoHorarioResponse.de(generarFranjas.ejecutar(id.toString()));
    }
}
