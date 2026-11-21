package com.appointy.appointment.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.usecase.CitaUseCase;
import com.appointy.appointment.domain.usecase.ConsultaCitaUseCase;
import com.appointy.appointment.domain.model.EstadisticasCitas;
import com.appointy.appointment.domain.model.EstadoCita;
import com.appointy.appointment.domain.model.FiltroCitas;
import com.appointy.appointment.infrastructure.entry_points.dto.PaginaCitasResponse;
import com.appointy.appointment.infrastructure.entry_points.dto.CancelarCitaRequest;
import com.appointy.appointment.infrastructure.entry_points.dto.CitaResponse;
import com.appointy.appointment.infrastructure.entry_points.dto.HistorialCitaResponse;
import com.appointy.appointment.infrastructure.entry_points.dto.MarcarCitaRequest;
import com.appointy.appointment.infrastructure.entry_points.dto.ReprogramarCitaRequest;
import com.appointy.appointment.infrastructure.entry_points.dto.ReservarCitaRequest;
import com.appointy.appointment.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** CU-18 a CU-24. El rol se exige por endpoint; la propiedad de cada cita la verifica el caso de uso. */
@Tag(name = "Citas")
@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private final CitaUseCase citaUseCase;
    private final ConsultaCitaUseCase consultaCitaUseCase;

    public CitaController(CitaUseCase citaUseCase, ConsultaCitaUseCase consultaCitaUseCase) {
        this.citaUseCase = citaUseCase;
        this.consultaCitaUseCase = consultaCitaUseCase;
    }

    @Operation(summary = "Reservar una cita en una franja disponible (PACIENTE)")
    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponse reservar(@AuthenticationPrincipal UsuarioAutenticado u, @Valid @RequestBody ReservarCitaRequest s) {
        return CitaResponse.de(citaUseCase.reservarCita(new CitaUseCase.ReservarCitaComando(
                actor(u), s.especialidadId().toString(), s.profesionalId().toString(), s.centroId().toString(), s.franjaId().toString())));
    }

    @Operation(summary = "Reprogramar una cita a otra franja (PACIENTE dueño)")
    @PostMapping("/{id}/reprogramar")
    @PreAuthorize("hasRole('PACIENTE')")
    public CitaResponse reprogramar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id, @Valid @RequestBody ReprogramarCitaRequest s) {
        return CitaResponse.de(citaUseCase.reprogramarCita(new CitaUseCase.ReprogramarCitaComando(actor(u), id.toString(), s.franjaId().toString())));
    }

    @Operation(summary = "Cancelar una cita con motivo opcional (PACIENTE dueño o ADMINISTRATIVO)")
    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMINISTRATIVO')")
    public CitaResponse cancelar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelarCitaRequest s) {
        return CitaResponse.de(citaUseCase.cancelarCita(new CitaUseCase.CancelarCitaComando(actor(u), id.toString(), s == null ? null : s.motivo())));
    }

    /** GET all paginado (RA-04). El listado completo es solo para el personal administrativo. */
    @Operation(summary = "Listar todas las citas, paginado y con filtros (ADMINISTRATIVO)")
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public PaginaCitasResponse listar(@AuthenticationPrincipal UsuarioAutenticado u, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size, @RequestParam(required = false) EstadoCita estado,
            @RequestParam(required = false) UUID profesionalId, @RequestParam(required = false) UUID pacienteId) {
        FiltroCitas filtro = new FiltroCitas(estado, profesionalId == null ? null : profesionalId.toString(), pacienteId == null ? null : pacienteId.toString());
        return PaginaCitasResponse.de(consultaCitaUseCase.listarCitas(actor(u), filtro, page, size));
    }

    @Operation(summary = "Conteo de citas por estado (ADMINISTRATIVO)")
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public EstadisticasCitas estadisticas(@AuthenticationPrincipal UsuarioAutenticado u) {
        return consultaCitaUseCase.consultarEstadisticasCitas(actor(u));
    }

    @Operation(summary = "Mis citas, próximas o pasadas (PACIENTE)")
    @GetMapping("/mias")
    @PreAuthorize("hasRole('PACIENTE')")
    public List<CitaResponse> mias(@AuthenticationPrincipal UsuarioAutenticado u, @RequestParam(required = false) String periodo) {
        return Arrays.stream(consultaCitaUseCase.consultarMisCitas(u.usuarioId(), periodo)).map(CitaResponse::de).toList();
    }

    @Operation(summary = "Agenda confirmada del profesional, de una fecha o de hoy en adelante (PROFESIONAL)")
    @GetMapping("/agenda")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public List<CitaResponse> agenda(@AuthenticationPrincipal UsuarioAutenticado u, @RequestParam(required = false) LocalDate fecha) {
        return Arrays.stream(consultaCitaUseCase.consultarAgenda(u.usuarioId(), fecha == null ? null : fecha.toString())).map(CitaResponse::de).toList();
    }

    @Operation(summary = "Consultar una cita (paciente, profesional de la cita o ADMINISTRATIVO)")
    @GetMapping("/{id}")
    public CitaResponse obtener(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id) {
        return CitaResponse.de(consultaCitaUseCase.consultarCita(actor(u), id.toString()));
    }

    @Operation(summary = "Marcar la cita como COMPLETADA o NO_ASISTIDA (PROFESIONAL de la cita)")
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public CitaResponse marcar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id, @Valid @RequestBody MarcarCitaRequest s) {
        return CitaResponse.de(citaUseCase.marcarCitaAtendida(actor(u), id.toString(), s.estado()));
    }

    @Operation(summary = "Historial de cambios de estado de una cita")
    @GetMapping("/{id}/historial")
    public List<HistorialCitaResponse> historial(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id) {
        return Arrays.stream(consultaCitaUseCase.consultarHistorialCita(actor(u), id.toString())).map(HistorialCitaResponse::de).toList();
    }

    private static Actor actor(UsuarioAutenticado u) {
        return new Actor(u.usuarioId(), u.rol());
    }
}
