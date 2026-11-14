package com.appointy.notification.infrastructure.entry_points;

import com.appointy.notification.domain.model.EstadoNotificacion;
import com.appointy.notification.domain.model.Estadisticas;
import com.appointy.notification.domain.model.FiltroNotificaciones;
import com.appointy.notification.domain.model.ResultadoLote;
import com.appointy.notification.domain.model.TipoNotificacion;
import com.appointy.notification.domain.usecase.ConsultarEstadisticasUseCase;
import com.appointy.notification.domain.usecase.EnviarRecordatoriosUseCase;
import com.appointy.notification.domain.usecase.ListarNotificacionesDeCitaUseCase;
import com.appointy.notification.domain.usecase.ListarNotificacionesUseCase;
import com.appointy.notification.domain.usecase.ReenviarNotificacionUseCase;
import com.appointy.notification.domain.usecase.ReintentarFallidasUseCase;
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
@RestController
@RequestMapping("/api/v1/notificaciones")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class NotificacionController {

    private final ListarNotificacionesUseCase listar;
    private final ListarNotificacionesDeCitaUseCase deCita;
    private final ReenviarNotificacionUseCase reenviar;
    private final ConsultarEstadisticasUseCase estadisticas;
    private final EnviarRecordatoriosUseCase recordatorios;
    private final ReintentarFallidasUseCase reintentos;

    public NotificacionController(ListarNotificacionesUseCase listar, ListarNotificacionesDeCitaUseCase deCita,
            ReenviarNotificacionUseCase reenviar, ConsultarEstadisticasUseCase estadisticas, EnviarRecordatoriosUseCase recordatorios,
            ReintentarFallidasUseCase reintentos) {
        this.listar = listar;
        this.deCita = deCita;
        this.reenviar = reenviar;
        this.estadisticas = estadisticas;
        this.recordatorios = recordatorios;
        this.reintentos = reintentos;
    }

    /** GET all paginado (RA-04). */
    @GetMapping
    public PaginaResponse listar(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) EstadoNotificacion estado, @RequestParam(required = false) TipoNotificacion tipo,
            @RequestParam(required = false) UUID citaId) {
        FiltroNotificaciones filtro = new FiltroNotificaciones(estado, tipo, citaId == null ? null : citaId.toString());
        return PaginaResponse.de(listar.ejecutar(filtro, page, size));
    }

    @GetMapping("/estadisticas")
    public Estadisticas estadisticas() {
        return estadisticas.ejecutar();
    }

    @GetMapping("/cita/{citaId}")
    public List<NotificacionResponse> deCita(@PathVariable UUID citaId) {
        return Arrays.stream(deCita.ejecutar(citaId.toString())).map(NotificacionResponse::de).toList();
    }

    @PostMapping("/{id}/reenviar")
    public NotificacionResponse reenviar(@PathVariable UUID id) {
        return NotificacionResponse.de(reenviar.ejecutar(id.toString()));
    }

    @PostMapping("/recordatorios/ejecutar")
    public ResultadoLote ejecutarRecordatorios() {
        return recordatorios.ejecutar();
    }

    @PostMapping("/reintentos/ejecutar")
    public ResultadoLote ejecutarReintentos() {
        return reintentos.ejecutar();
    }
}
