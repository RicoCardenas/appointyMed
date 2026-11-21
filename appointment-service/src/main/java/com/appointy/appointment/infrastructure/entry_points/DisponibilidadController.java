package com.appointy.appointment.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.appointment.domain.usecase.FranjaUseCase;
import com.appointy.appointment.infrastructure.entry_points.dto.FranjaResponse;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** CU-17: cualquier usuario autenticado consulta las franjas DISPONIBLES y futuras. */
@Tag(name = "Disponibilidad")
@RestController
@RequestMapping("/api/v1/disponibilidad")
public class DisponibilidadController {

    private final FranjaUseCase franjaUseCase;

    public DisponibilidadController(FranjaUseCase franjaUseCase) {
        this.franjaUseCase = franjaUseCase;
    }

    @Operation(summary = "Franjas disponibles de un profesional en un centro y fecha")
    @GetMapping
    public List<FranjaResponse> disponibilidad(@RequestParam UUID profesionalId, @RequestParam UUID centroId, @RequestParam LocalDate fecha) {
        return Arrays.stream(franjaUseCase.consultarDisponibilidad(profesionalId.toString(), centroId.toString(), fecha.toString())).map(FranjaResponse::de).toList();
    }
}
