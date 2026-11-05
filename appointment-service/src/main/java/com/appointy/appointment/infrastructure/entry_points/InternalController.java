package com.appointy.appointment.infrastructure.entry_points;

import com.appointy.appointment.domain.model.ResultadoGeneracion;
import com.appointy.appointment.domain.usecase.BloquearFranjasUseCase;
import com.appointy.appointment.domain.usecase.GenerarFranjasUseCase;
import com.appointy.appointment.domain.usecase.ValidarRelacionUseCase;
import com.appointy.appointment.infrastructure.entry_points.dto.BloquearFranjasRequest;
import com.appointy.appointment.infrastructure.entry_points.dto.GenerarFranjasRequest;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Llamadas de servicio a servicio (RNF-04): catalog genera y bloquea franjas (CU-25, CU-26); clinical valida relacion (CU-27). */
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final GenerarFranjasUseCase generar;
    private final BloquearFranjasUseCase bloquear;
    private final ValidarRelacionUseCase relacion;

    public InternalController(GenerarFranjasUseCase generar, BloquearFranjasUseCase bloquear, ValidarRelacionUseCase relacion) {
        this.generar = generar;
        this.bloquear = bloquear;
        this.relacion = relacion;
    }

    @PostMapping("/franjas/generar")
    public Map<String, Integer> generar(@Valid @RequestBody GenerarFranjasRequest s) {
        ResultadoGeneracion r = generar.ejecutar(s.profesionalId().toString(), s.centroId().toString(), s.aDominio());
        return Map.of("creadas", r.creadas(), "existentes", r.existentes());
    }

    @PostMapping("/franjas/bloquear")
    public Map<String, Integer> bloquear(@Valid @RequestBody BloquearFranjasRequest s) {
        int bloqueadas = bloquear.ejecutar(s.profesionalId().toString(), s.centroId().toString(), s.fechaInicio().toString(), s.fechaFin().toString());
        return Map.of("bloqueadas", bloqueadas);
    }

    @GetMapping("/relaciones")
    public Map<String, Boolean> relacion(@RequestParam UUID profesionalId, @RequestParam UUID pacienteId) {
        return Map.of("existe", relacion.ejecutar(profesionalId.toString(), pacienteId.toString()));
    }
}
