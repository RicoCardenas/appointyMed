package com.appointy.appointment.infrastructure.entry_points;

import com.appointy.appointment.domain.model.Actor;
import com.appointy.appointment.domain.port.in.CancelarCitaPort;
import com.appointy.appointment.domain.port.in.ConsultarAgendaPort;
import com.appointy.appointment.domain.port.in.ConsultarCitaPort;
import com.appointy.appointment.domain.port.in.ConsultarHistorialCitaPort;
import com.appointy.appointment.domain.port.in.ConsultarMisCitasPort;
import com.appointy.appointment.domain.port.in.MarcarCitaAtendidaPort;
import com.appointy.appointment.domain.port.in.ReprogramarCitaPort;
import com.appointy.appointment.domain.port.in.ReservarCitaPort;
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
@RestController
@RequestMapping("/api/v1/citas")
public class CitaController {

    private final ReservarCitaPort reservar;
    private final ReprogramarCitaPort reprogramar;
    private final CancelarCitaPort cancelar;
    private final ConsultarMisCitasPort misCitas;
    private final ConsultarAgendaPort agenda;
    private final ConsultarCitaPort consultar;
    private final MarcarCitaAtendidaPort marcar;
    private final ConsultarHistorialCitaPort historial;

    public CitaController(ReservarCitaPort reservar, ReprogramarCitaPort reprogramar, CancelarCitaPort cancelar, ConsultarMisCitasPort misCitas,
            ConsultarAgendaPort agenda, ConsultarCitaPort consultar, MarcarCitaAtendidaPort marcar, ConsultarHistorialCitaPort historial) {
        this.reservar = reservar;
        this.reprogramar = reprogramar;
        this.cancelar = cancelar;
        this.misCitas = misCitas;
        this.agenda = agenda;
        this.consultar = consultar;
        this.marcar = marcar;
        this.historial = historial;
    }

    @PostMapping
    @PreAuthorize("hasRole('PACIENTE')")
    @ResponseStatus(HttpStatus.CREATED)
    public CitaResponse reservar(@AuthenticationPrincipal UsuarioAutenticado u, @Valid @RequestBody ReservarCitaRequest s) {
        return CitaResponse.de(reservar.ejecutar(new ReservarCitaPort.Comando(
                actor(u), s.especialidadId().toString(), s.profesionalId().toString(), s.centroId().toString(), s.franjaId().toString())));
    }

    @PostMapping("/{id}/reprogramar")
    @PreAuthorize("hasRole('PACIENTE')")
    public CitaResponse reprogramar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id, @Valid @RequestBody ReprogramarCitaRequest s) {
        return CitaResponse.de(reprogramar.ejecutar(new ReprogramarCitaPort.Comando(actor(u), id.toString(), s.franjaId().toString())));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('PACIENTE', 'ADMINISTRATIVO')")
    public CitaResponse cancelar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id,
            @Valid @RequestBody(required = false) CancelarCitaRequest s) {
        return CitaResponse.de(cancelar.ejecutar(new CancelarCitaPort.Comando(actor(u), id.toString(), s == null ? null : s.motivo())));
    }

    @GetMapping("/mias")
    @PreAuthorize("hasRole('PACIENTE')")
    public List<CitaResponse> mias(@AuthenticationPrincipal UsuarioAutenticado u, @RequestParam(required = false) String periodo) {
        return Arrays.stream(misCitas.ejecutar(u.usuarioId(), periodo)).map(CitaResponse::de).toList();
    }

    @GetMapping("/agenda")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public List<CitaResponse> agenda(@AuthenticationPrincipal UsuarioAutenticado u, @RequestParam(required = false) LocalDate fecha) {
        return Arrays.stream(agenda.ejecutar(u.usuarioId(), fecha == null ? null : fecha.toString())).map(CitaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public CitaResponse obtener(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id) {
        return CitaResponse.de(consultar.ejecutar(actor(u), id.toString()));
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('PROFESIONAL')")
    public CitaResponse marcar(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id, @Valid @RequestBody MarcarCitaRequest s) {
        return CitaResponse.de(marcar.ejecutar(actor(u), id.toString(), s.estado()));
    }

    @GetMapping("/{id}/historial")
    public List<HistorialCitaResponse> historial(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id) {
        return Arrays.stream(historial.ejecutar(actor(u), id.toString())).map(HistorialCitaResponse::de).toList();
    }

    private static Actor actor(UsuarioAutenticado u) {
        return new Actor(u.usuarioId(), u.rol());
    }
}
