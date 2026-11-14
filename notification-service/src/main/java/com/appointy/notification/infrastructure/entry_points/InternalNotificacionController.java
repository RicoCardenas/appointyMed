package com.appointy.notification.infrastructure.entry_points;

import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.usecase.NotificarCitaUseCase;
import com.appointy.notification.infrastructure.entry_points.dto.NotificarCitaRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** CU-33. Solo servicio a servicio (llave de servicio); lo llama appointment tras el commit. */
@RestController
@RequestMapping("/internal")
public class InternalNotificacionController {

    private final NotificarCitaUseCase notificar;

    public InternalNotificacionController(NotificarCitaUseCase notificar) {
        this.notificar = notificar;
    }

    @PostMapping("/notificaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public Resultado notificar(@Valid @RequestBody NotificarCitaRequest solicitud) {
        Notificacion n = notificar.ejecutar(solicitud.aSolicitud());
        return new Resultado(n.id(), n.estado().name());
    }

    record Resultado(String id, String estado) {}
}
