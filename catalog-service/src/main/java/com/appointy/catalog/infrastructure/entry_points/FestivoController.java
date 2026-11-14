package com.appointy.catalog.infrastructure.entry_points;

import com.appointy.catalog.domain.usecase.ConsultarFestivosUseCase;
import com.appointy.catalog.infrastructure.entry_points.dto.FestivoResponse;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Festivos del pais segun la API externa (RA-06). Cualquier usuario autenticado. */
@RestController
@RequestMapping("/api/v1/festivos")
public class FestivoController {

    private final ConsultarFestivosUseCase consultar;

    public FestivoController(ConsultarFestivosUseCase consultar) {
        this.consultar = consultar;
    }

    @GetMapping
    public List<FestivoResponse> festivos(@RequestParam int anio) {
        return Arrays.stream(consultar.ejecutar(anio)).map(FestivoResponse::de).toList();
    }
}
