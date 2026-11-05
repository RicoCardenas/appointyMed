package com.appointy.catalog.infrastructure.entry_points;

import com.appointy.catalog.domain.port.in.ConfigurarParametrosPort;
import com.appointy.catalog.domain.port.in.ConsultarParametrosPort;
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

    private final ConsultarParametrosPort consultar;
    private final ConfigurarParametrosPort configurar;

    public ParametroController(ConsultarParametrosPort consultar, ConfigurarParametrosPort configurar) {
        this.consultar = consultar;
        this.configurar = configurar;
    }

    @GetMapping
    public List<ParametroResponse> consultar() {
        return Arrays.stream(consultar.ejecutar()).map(ParametroResponse::de).toList();
    }

    @PutMapping
    public List<ParametroResponse> configurar(@Valid @RequestBody ParametrosRequest s) {
        return Arrays.stream(configurar.ejecutar(new ConfigurarParametrosPort.Comando(s.comoParametros()))).map(ParametroResponse::de).toList();
    }
}
