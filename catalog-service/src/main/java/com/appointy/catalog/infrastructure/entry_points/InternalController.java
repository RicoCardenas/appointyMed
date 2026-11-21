package com.appointy.catalog.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.usecase.ProfesionalUseCase;
import com.appointy.catalog.domain.usecase.ParametroUseCase;
import com.appointy.catalog.infrastructure.entry_points.dto.AsociacionResponse;
import com.appointy.catalog.infrastructure.entry_points.dto.ProfesionalInternoResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consultas de servicio a servicio, protegidas con la llave de API (RNF-04): no las usan los usuarios finales. */
@Tag(name = "Interno (servicio a servicio)")
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final ProfesionalUseCase profesionalUseCase;
    private final ParametroUseCase parametroUseCase;

    public InternalController(ProfesionalUseCase profesionalUseCase, ParametroUseCase parametroUseCase) {
        this.profesionalUseCase = profesionalUseCase;
        this.parametroUseCase = parametroUseCase;
    }

    @Operation(summary = "Validar asociación profesional-especialidad-centro (appointment)")
    @GetMapping("/profesionales/{id}/asociacion")
    public AsociacionResponse asociacion(@PathVariable UUID id, @RequestParam UUID especialidadId, @RequestParam UUID centroId) {
        return AsociacionResponse.de(profesionalUseCase.validarAsociacion(id.toString(), especialidadId.toString(), centroId.toString()));
    }

    /** 404 si la cuenta no tiene perfil de profesional. */
    @Operation(summary = "Profesional de una cuenta (appointment, clinical)")
    @GetMapping("/profesionales/por-usuario/{usuarioId}")
    public ProfesionalInternoResponse porUsuario(@PathVariable UUID usuarioId) {
        Profesional p = profesionalUseCase.buscarProfesionalPorUsuario(usuarioId.toString());
        if (p == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "La cuenta no tiene perfil de profesional");
        }
        return ProfesionalInternoResponse.de(p);
    }

    @Operation(summary = "Parámetros del servicio (appointment)")
    @GetMapping("/parametros")
    public Map<String, String> parametros() {
        Map<String, String> valores = new LinkedHashMap<>();
        for (ParametroServicio p : parametroUseCase.consultarParametros()) {
            valores.put(p.clave(), p.valor());
        }
        return valores;
    }
}
