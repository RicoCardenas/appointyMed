package com.appointy.catalog.infrastructure.entry_points;

import com.appointy.catalog.domain.exception.NoEncontradoException;
import com.appointy.catalog.domain.model.ParametroServicio;
import com.appointy.catalog.domain.model.Profesional;
import com.appointy.catalog.domain.port.in.BuscarProfesionalPorUsuarioPort;
import com.appointy.catalog.domain.port.in.ConsultarParametrosPort;
import com.appointy.catalog.domain.port.in.ValidarAsociacionPort;
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
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final ValidarAsociacionPort validarAsociacion;
    private final ConsultarParametrosPort consultarParametros;
    private final BuscarProfesionalPorUsuarioPort buscarPorUsuario;

    public InternalController(ValidarAsociacionPort validarAsociacion, ConsultarParametrosPort consultarParametros,
            BuscarProfesionalPorUsuarioPort buscarPorUsuario) {
        this.validarAsociacion = validarAsociacion;
        this.consultarParametros = consultarParametros;
        this.buscarPorUsuario = buscarPorUsuario;
    }

    @GetMapping("/profesionales/{id}/asociacion")
    public AsociacionResponse asociacion(@PathVariable UUID id, @RequestParam UUID especialidadId, @RequestParam UUID centroId) {
        return AsociacionResponse.de(validarAsociacion.ejecutar(id.toString(), especialidadId.toString(), centroId.toString()));
    }

    /** 404 si la cuenta no tiene perfil de profesional. */
    @GetMapping("/profesionales/por-usuario/{usuarioId}")
    public ProfesionalInternoResponse porUsuario(@PathVariable UUID usuarioId) {
        Profesional p = buscarPorUsuario.ejecutar(usuarioId.toString());
        if (p == null) {
            throw new NoEncontradoException("PROFESIONAL_NO_ENCONTRADO", "La cuenta no tiene perfil de profesional");
        }
        return ProfesionalInternoResponse.de(p);
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
