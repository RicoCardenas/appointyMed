package com.appointy.appointment.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.appointment.domain.model.Cita;
import com.appointy.appointment.domain.model.ResultadoGeneracion;
import com.appointy.appointment.domain.usecase.FranjaUseCase;
import com.appointy.appointment.domain.usecase.ConsultaCitaUseCase;
import com.appointy.appointment.infrastructure.entry_points.dto.BloquearFranjasRequest;
import com.appointy.appointment.infrastructure.entry_points.dto.GenerarFranjasRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Llamadas de servicio a servicio (RNF-04): catalog genera y bloquea franjas (CU-25, CU-26); clinical valida relacion (CU-27). */
@Tag(name = "Interno (servicio a servicio)")
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final FranjaUseCase franjaUseCase;
    private final ConsultaCitaUseCase consultaCitaUseCase;

    public InternalController(FranjaUseCase franjaUseCase, ConsultaCitaUseCase consultaCitaUseCase) {
        this.franjaUseCase = franjaUseCase;
        this.consultaCitaUseCase = consultaCitaUseCase;
    }

    @Operation(summary = "Generar franjas (catalog)")
    @PostMapping("/franjas/generar")
    public Map<String, Integer> generar(@Valid @RequestBody GenerarFranjasRequest s) {
        ResultadoGeneracion r = franjaUseCase.generarFranjas(s.profesionalId().toString(), s.centroId().toString(), s.aDominio());
        return Map.of("creadas", r.creadas(), "existentes", r.existentes());
    }

    @Operation(summary = "Bloquear franjas (catalog)")
    @PostMapping("/franjas/bloquear")
    public Map<String, Integer> bloquear(@Valid @RequestBody BloquearFranjasRequest s) {
        int bloqueadas = franjaUseCase.bloquearFranjas(s.profesionalId().toString(), s.centroId().toString(), s.fechaInicio().toString(), s.fechaFin().toString());
        return Map.of("bloqueadas", bloqueadas);
    }

    @Operation(summary = "Existe relación profesional-paciente (clinical)")
    @GetMapping("/relaciones")
    public Map<String, Boolean> relacion(@RequestParam UUID profesionalId, @RequestParam UUID pacienteId) {
        return Map.of("existe", consultaCitaUseCase.validarRelacion(profesionalId.toString(), pacienteId.toString()));
    }

    /** CU-28: clinical comprueba el profesional y el estado de la cita. Solo ids y estado, sin datos personales. */
    @Operation(summary = "Datos mínimos de una cita (clinical)")
    @GetMapping("/citas/{id}")
    public Map<String, String> cita(@PathVariable UUID id) {
        Cita c = consultaCitaUseCase.obtenerCitaServicio(id.toString());
        return Map.of("citaId", c.id(), "pacienteId", c.pacienteId(), "profesionalId", c.profesionalId(), "estado", c.estado().name());
    }
}
