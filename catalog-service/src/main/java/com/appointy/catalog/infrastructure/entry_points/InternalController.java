package com.appointy.catalog.infrastructure.entry_points;

import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.port.in.ConsultarParametrosPort;
import com.appointy.catalog.domain.port.in.ValidarAsociacionPort;
import com.appointy.catalog.infrastructure.entry_points.dto.AsociacionResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consultas de servicio a servicio, protegidas con la llave de API (RNF-04): no las usan los usuarios finales. */
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final ValidarAsociacionPort validarAsociacion;
    private final ConsultarParametrosPort consultarParametros;

    public InternalController(ValidarAsociacionPort validarAsociacion, ConsultarParametrosPort consultarParametros) {
        this.validarAsociacion = validarAsociacion;
        this.consultarParametros = consultarParametros;
    }

    @GetMapping("/profesionales/{id}/asociacion")
    public AsociacionResponse asociacion(@PathVariable UUID id, @RequestParam UUID especialidadId, @RequestParam UUID centroId) {
        return AsociacionResponse.de(validarAsociacion.ejecutar(id.toString(), especialidadId.toString(), centroId.toString()));
    }

    @GetMapping("/parametros")
    public Map<String, String> parametros() {
        Map<String, String> valores = new LinkedHashMap<>();
        for (ParametroServicio p : consultarParametros.ejecutar()) {
            valores.put(p.clave(), p.valor());
        }
        return valores;
    }
}
