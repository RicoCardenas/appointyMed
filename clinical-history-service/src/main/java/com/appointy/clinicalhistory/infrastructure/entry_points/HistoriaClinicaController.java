package com.appointy.clinicalhistory.infrastructure.entry_points;

import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.usecase.ConsultarHistoriaPacienteUseCase;
import com.appointy.clinicalhistory.domain.usecase.ConsultarMiHistoriaUseCase;
import com.appointy.clinicalhistory.domain.usecase.CorregirEntradaUseCase;
import com.appointy.clinicalhistory.domain.usecase.RegistrarAtencionUseCase;
import com.appointy.clinicalhistory.infrastructure.entry_points.dto.CorregirEntradaRequest;
import com.appointy.clinicalhistory.infrastructure.entry_points.dto.EntradaClinicaResponse;
import com.appointy.clinicalhistory.infrastructure.entry_points.dto.RegistrarAtencionRequest;
import com.appointy.clinicalhistory.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import com.appointy.clinicalhistory.domain.usecase.BuscarDiagnosticosUseCase;
import com.appointy.clinicalhistory.domain.usecase.ConsultarMiResumenUseCase;
import com.appointy.clinicalhistory.domain.usecase.ConsultarResumenPacienteUseCase;
import com.appointy.clinicalhistory.domain.usecase.ListarAtencionesUseCase;
import com.appointy.clinicalhistory.domain.usecase.ListarVersionesEntradaUseCase;
import com.appointy.clinicalhistory.infrastructure.entry_points.dto.DiagnosticoResponse;
import com.appointy.clinicalhistory.infrastructure.entry_points.dto.PaginaEntradasResponse;
import com.appointy.clinicalhistory.infrastructure.entry_points.dto.ResumenHistoriaResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * CU-28 a CU-31. No hay {@code @PreAuthorize} por rol: el rol lo exige cada caso de uso para que un acceso rechazado
 * por rol tambien quede en el log de acceso (RF-30). Sin token, la cadena de seguridad responde 401 (no hay usuario que registrar).
 */
@RestController
@RequestMapping("/api/v1")
public class HistoriaClinicaController {

    private final RegistrarAtencionUseCase registrar;
    private final ConsultarHistoriaPacienteUseCase historiaDePaciente;
    private final ConsultarMiHistoriaUseCase miHistoria;
    private final CorregirEntradaUseCase corregir;
    private final BuscarDiagnosticosUseCase diagnosticos;
    private final ConsultarResumenPacienteUseCase resumenPaciente;
    private final ConsultarMiResumenUseCase miResumen;
    private final ListarVersionesEntradaUseCase versiones;
    private final ListarAtencionesUseCase atenciones;

    public HistoriaClinicaController(RegistrarAtencionUseCase registrar, ConsultarHistoriaPacienteUseCase historiaDePaciente,
            ConsultarMiHistoriaUseCase miHistoria, CorregirEntradaUseCase corregir,
            BuscarDiagnosticosUseCase diagnosticos, ConsultarResumenPacienteUseCase resumenPaciente, ConsultarMiResumenUseCase miResumen,
            ListarVersionesEntradaUseCase versiones, ListarAtencionesUseCase atenciones) {
        this.registrar = registrar;
        this.historiaDePaciente = historiaDePaciente;
        this.miHistoria = miHistoria;
        this.corregir = corregir;
        this.diagnosticos = diagnosticos;
        this.resumenPaciente = resumenPaciente;
        this.miResumen = miResumen;
        this.versiones = versiones;
        this.atenciones = atenciones;
    }

    @PostMapping("/entradas")
    @ResponseStatus(HttpStatus.CREATED)
    public EntradaClinicaResponse registrar(@AuthenticationPrincipal UsuarioAutenticado u, @Valid @RequestBody RegistrarAtencionRequest s) {
        return EntradaClinicaResponse.de(registrar.ejecutar(new RegistrarAtencionUseCase.Comando(actor(u), s.citaId().toString(), s.aContenido())));
    }

    @PostMapping("/entradas/{id}/correcciones")
    @ResponseStatus(HttpStatus.CREATED)
    public EntradaClinicaResponse corregir(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id, @Valid @RequestBody CorregirEntradaRequest s) {
        return EntradaClinicaResponse.de(corregir.ejecutar(new CorregirEntradaUseCase.Comando(actor(u), id.toString(), s.aContenido())));
    }

    @GetMapping("/pacientes/{pacienteId}/historia")
    public List<EntradaClinicaResponse> historiaDePaciente(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID pacienteId) {
        return Arrays.stream(historiaDePaciente.ejecutar(actor(u), pacienteId.toString())).map(EntradaClinicaResponse::de).toList();
    }

    @GetMapping("/historia/mia")
    public List<EntradaClinicaResponse> miHistoria(@AuthenticationPrincipal UsuarioAutenticado u) {
        return Arrays.stream(miHistoria.ejecutar(actor(u))).map(EntradaClinicaResponse::de).toList();
    }

    /** GET all paginado (RA-04): las entradas que escribio el profesional autenticado. */
    @GetMapping("/atenciones")
    public PaginaEntradasResponse atenciones(@AuthenticationPrincipal UsuarioAutenticado u, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PaginaEntradasResponse.de(atenciones.ejecutar(actor(u), page, size));
    }

    @GetMapping("/entradas/{id}/versiones")
    public List<EntradaClinicaResponse> versiones(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id) {
        return Arrays.stream(versiones.ejecutar(actor(u), id.toString())).map(EntradaClinicaResponse::de).toList();
    }

    @GetMapping("/pacientes/{pacienteId}/resumen")
    public ResumenHistoriaResponse resumenDePaciente(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID pacienteId) {
        return ResumenHistoriaResponse.de(resumenPaciente.ejecutar(actor(u), pacienteId.toString()));
    }

    @GetMapping("/historia/mia/resumen")
    public ResumenHistoriaResponse miResumen(@AuthenticationPrincipal UsuarioAutenticado u) {
        return ResumenHistoriaResponse.de(miResumen.ejecutar(actor(u)));
    }

    /** Busqueda en el catalogo CIE-10 (API externa, RA-06). Solo PROFESIONAL. */
    @GetMapping("/diagnosticos")
    public List<DiagnosticoResponse> diagnosticos(@AuthenticationPrincipal UsuarioAutenticado u, @RequestParam String q) {
        return Arrays.stream(diagnosticos.ejecutar(actor(u), q)).map(DiagnosticoResponse::de).toList();
    }

    private static Actor actor(UsuarioAutenticado u) {
        return new Actor(u.usuarioId(), u.rol());
    }
}
