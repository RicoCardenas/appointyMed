package com.appointy.notification.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.notification.domain.model.Notificacion;
import com.appointy.notification.domain.usecase.NotificacionUseCase;
import com.appointy.notification.infrastructure.entry_points.dto.NotificarCitaRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** CU-33. Solo servicio a servicio (llave de servicio); lo llama appointment tras el commit. */
@Tag(name = "Interno (servicio a servicio)")
@RestController
@RequestMapping("/internal")
public class InternalNotificacionController {

    private final NotificacionUseCase notificacionUseCase;

    public InternalNotificacionController(NotificacionUseCase notificacionUseCase) {
        this.notificacionUseCase = notificacionUseCase;
    }

    @Operation(summary = "Notificar un hecho de una cita (appointment)")
    @PostMapping("/notificaciones")
    @ResponseStatus(HttpStatus.CREATED)
    public Resultado notificar(@Valid @RequestBody NotificarCitaRequest solicitud) {
        Notificacion n = notificacionUseCase.notificarCita(solicitud.aSolicitud());
        return new Resultado(n.id(), n.estado().name());
    }

    record Resultado(String id, String estado) {}
}
