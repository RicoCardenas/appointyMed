package com.appointy.notification.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.Estadisticas;
import com.appointy.notification.domain.model.FiltroNotificaciones;
import com.appointy.notification.domain.model.ResultadoLote;
import com.appointy.notification.domain.model.TipoNotificacion;
import com.appointy.notification.domain.usecase.ConsultaNotificacionUseCase;
import com.appointy.notification.domain.usecase.NotificacionUseCase;
import com.appointy.notification.infrastructure.entry_points.dto.NotificacionResponse;
import com.appointy.notification.infrastructure.entry_points.dto.PaginaResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Gestion de notificaciones por el personal administrativo. */
@Tag(name = "Notificaciones")
@RestController
@RequestMapping("/api/v1/notificaciones")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class NotificacionController {

    private final ConsultaNotificacionUseCase consultaNotificacionUseCase;
    private final NotificacionUseCase notificacionUseCase;

    public NotificacionController(ConsultaNotificacionUseCase consultaNotificacionUseCase, NotificacionUseCase notificacionUseCase) {
        this.consultaNotificacionUseCase = consultaNotificacionUseCase;
        this.notificacionUseCase = notificacionUseCase;
    }

    /** GET all paginado (RA-04). */
    @Operation(summary = "Listar notificaciones, paginado y con filtros (ADMINISTRATIVO)")
    @GetMapping
    public PaginaResponse listar(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) EstadoNotificacion estado, @RequestParam(required = false) TipoNotificacion tipo,
            @RequestParam(required = false) UUID citaId) {
        FiltroNotificaciones filtro = new FiltroNotificaciones(estado, tipo, citaId == null ? null : citaId.toString());
        return PaginaResponse.de(consultaNotificacionUseCase.listarNotificaciones(filtro, page, size));
    }

    @Operation(summary = "Conteo por estado y tipo (ADMINISTRATIVO)")
    @GetMapping("/estadisticas")
    public Estadisticas estadisticas() {
        return consultaNotificacionUseCase.consultarEstadisticas();
    }

    @Operation(summary = "Notificaciones de una cita (ADMINISTRATIVO)")
    @GetMapping("/cita/{citaId}")
    public List<NotificacionResponse> deCita(@PathVariable UUID citaId) {
        return Arrays.stream(consultaNotificacionUseCase.listarNotificacionesDeCita(citaId.toString())).map(NotificacionResponse::de).toList();
    }

    @Operation(summary = "Reenviar una notificación pendiente o fallida (ADMINISTRATIVO)")
    @PostMapping("/{id}/reenviar")
    public NotificacionResponse reenviar(@PathVariable UUID id) {
        return NotificacionResponse.de(notificacionUseCase.reenviarNotificacion(id.toString()));
    }

    @Operation(summary = "Ejecutar ahora el envío de recordatorios vencidos (ADMINISTRATIVO)")
    @PostMapping("/recordatorios/ejecutar")
    public ResultadoLote ejecutarRecordatorios() {
        return notificacionUseCase.enviarRecordatorios();
    }

    @Operation(summary = "Ejecutar ahora el reintento de fallidas (ADMINISTRATIVO)")
    @PostMapping("/reintentos/ejecutar")
    public ResultadoLote ejecutarReintentos() {
        return notificacionUseCase.reintentarFallidas();
    }
}
