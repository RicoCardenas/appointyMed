package com.appointy.appointment.infrastructure.entry_points;

import com.appointy.appointment.domain.port.in.ConsultarDisponibilidadPort;
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
@RestController
@RequestMapping("/api/v1/disponibilidad")
public class DisponibilidadController {

    private final ConsultarDisponibilidadPort consultar;

    public DisponibilidadController(ConsultarDisponibilidadPort consultar) {
        this.consultar = consultar;
    }

    @GetMapping
    public List<FranjaResponse> disponibilidad(@RequestParam UUID profesionalId, @RequestParam UUID centroId, @RequestParam LocalDate fecha) {
        return Arrays.stream(consultar.ejecutar(profesionalId.toString(), centroId.toString(), fecha.toString())).map(FranjaResponse::de).toList();
    }
}
