package com.appointy.catalog.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.catalog.domain.usecase.ParametroUseCase;
import com.appointy.catalog.infrastructure.entry_points.dto.ParametroResponse;
import com.appointy.catalog.infrastructure.entry_points.dto.ParametrosRequest;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** CU-15. */
@Tag(name = "Parámetros")
@RestController
@RequestMapping("/api/v1/parametros")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class ParametroController {

    private final ParametroUseCase parametroUseCase;

    public ParametroController(ParametroUseCase parametroUseCase) {
        this.parametroUseCase = parametroUseCase;
    }

    @Operation(summary = "Consultar los parámetros del servicio")
    @GetMapping
    public List<ParametroResponse> consultar() {
        return Arrays.stream(parametroUseCase.consultarParametros()).map(ParametroResponse::de).toList();
    }

    @Operation(summary = "Configurar parámetros del servicio")
    @PutMapping
    public List<ParametroResponse> configurar(@Valid @RequestBody ParametrosRequest s) {
        return Arrays.stream(parametroUseCase.configurarParametros(new ParametroUseCase.ConfigurarParametrosComando(s.comoParametros()))).map(ParametroResponse::de).toList();
    }
}
