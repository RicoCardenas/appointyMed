package com.appointy.catalog.infrastructure.entry_points;

import com.appointy.catalog.domain.usecase.ConfigurarParametrosUseCase;
import com.appointy.catalog.domain.usecase.ConsultarParametrosUseCase;
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
@RestController
@RequestMapping("/api/v1/parametros")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class ParametroController {

    private final ConsultarParametrosUseCase consultar;
    private final ConfigurarParametrosUseCase configurar;

    public ParametroController(ConsultarParametrosUseCase consultar, ConfigurarParametrosUseCase configurar) {
        this.consultar = consultar;
        this.configurar = configurar;
    }

    @GetMapping
    public List<ParametroResponse> consultar() {
        return Arrays.stream(consultar.ejecutar()).map(ParametroResponse::de).toList();
    }

    @PutMapping
    public List<ParametroResponse> configurar(@Valid @RequestBody ParametrosRequest s) {
        return Arrays.stream(configurar.ejecutar(new ConfigurarParametrosUseCase.Comando(s.comoParametros()))).map(ParametroResponse::de).toList();
    }
}
