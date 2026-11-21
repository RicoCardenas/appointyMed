package com.appointy.clinicalhistory.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.clinicalhistory.domain.model.Actor;
import com.appointy.clinicalhistory.domain.usecase.HistoriaUseCase;
import com.appointy.clinicalhistory.domain.usecase.EntradaClinicaUseCase;
import com.appointy.clinicalhistory.infrastructure.entry_points.dto.CorregirEntradaRequest;
import com.appointy.clinicalhistory.infrastructure.entry_points.dto.EntradaClinicaResponse;
import com.appointy.clinicalhistory.infrastructure.entry_points.dto.RegistrarAtencionRequest;
import com.appointy.clinicalhistory.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
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
@Tag(name = "Historia clínica")
@RestController
@RequestMapping("/api/v1")
public class HistoriaClinicaController {

    private final EntradaClinicaUseCase entradaClinicaUseCase;
    private final HistoriaUseCase historiaUseCase;

    public HistoriaClinicaController(EntradaClinicaUseCase entradaClinicaUseCase, HistoriaUseCase historiaUseCase) {
        this.entradaClinicaUseCase = entradaClinicaUseCase;
        this.historiaUseCase = historiaUseCase;
    }

    @Operation(summary = "Registrar la atención de una cita (PROFESIONAL de la cita)")
    @PostMapping("/entradas")
    @ResponseStatus(HttpStatus.CREATED)
    public EntradaClinicaResponse registrar(@AuthenticationPrincipal UsuarioAutenticado u, @Valid @RequestBody RegistrarAtencionRequest s) {
        return EntradaClinicaResponse.de(entradaClinicaUseCase.registrarAtencion(new EntradaClinicaUseCase.RegistrarAtencionComando(actor(u), s.citaId().toString(), s.aContenido())));
    }

    @Operation(summary = "Corregir una entrada: crea una nueva que apunta a la anterior")
    @PostMapping("/entradas/{id}/correcciones")
    @ResponseStatus(HttpStatus.CREATED)
    public EntradaClinicaResponse corregir(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id, @Valid @RequestBody CorregirEntradaRequest s) {
        return EntradaClinicaResponse.de(entradaClinicaUseCase.corregirEntrada(new EntradaClinicaUseCase.CorregirEntradaComando(actor(u), id.toString(), s.aContenido())));
    }

    @Operation(summary = "Historia completa de un paciente (PROFESIONAL con relación)")
    @GetMapping("/pacientes/{pacienteId}/historia")
    public List<EntradaClinicaResponse> historiaDePaciente(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID pacienteId) {
        return Arrays.stream(historiaUseCase.consultarHistoriaPaciente(actor(u), pacienteId.toString())).map(EntradaClinicaResponse::de).toList();
    }

    @Operation(summary = "Mi historia clínica (PACIENTE)")
    @GetMapping("/historia/mia")
    public List<EntradaClinicaResponse> miHistoria(@AuthenticationPrincipal UsuarioAutenticado u) {
        return Arrays.stream(historiaUseCase.consultarMiHistoria(actor(u))).map(EntradaClinicaResponse::de).toList();
    }

    /** GET all paginado (RA-04): las entradas que escribio el profesional autenticado. */
    @Operation(summary = "Entradas que escribí, paginado (PROFESIONAL)")
    @GetMapping("/atenciones")
    public PaginaEntradasResponse atenciones(@AuthenticationPrincipal UsuarioAutenticado u, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PaginaEntradasResponse.de(historiaUseCase.listarAtenciones(actor(u), page, size));
    }

    @Operation(summary = "La entrada original de una atención y sus correcciones")
    @GetMapping("/entradas/{id}/versiones")
    public List<EntradaClinicaResponse> versiones(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID id) {
        return Arrays.stream(entradaClinicaUseCase.listarVersionesEntrada(actor(u), id.toString())).map(EntradaClinicaResponse::de).toList();
    }

    @Operation(summary = "Resumen de la historia de un paciente (PROFESIONAL con relación)")
    @GetMapping("/pacientes/{pacienteId}/resumen")
    public ResumenHistoriaResponse resumenDePaciente(@AuthenticationPrincipal UsuarioAutenticado u, @PathVariable UUID pacienteId) {
        return ResumenHistoriaResponse.de(historiaUseCase.consultarResumenPaciente(actor(u), pacienteId.toString()));
    }

    @Operation(summary = "Resumen de mi historia (PACIENTE)")
    @GetMapping("/historia/mia/resumen")
    public ResumenHistoriaResponse miResumen(@AuthenticationPrincipal UsuarioAutenticado u) {
        return ResumenHistoriaResponse.de(historiaUseCase.consultarMiResumen(actor(u)));
    }

    /** Busqueda en el catalogo CIE-10 (API externa, RA-06). Solo PROFESIONAL. */
    @Operation(summary = "Buscar diagnósticos CIE-10 en la API externa NLM (PROFESIONAL)")
    @GetMapping("/diagnosticos")
    public List<DiagnosticoResponse> diagnosticos(@AuthenticationPrincipal UsuarioAutenticado u, @RequestParam String q) {
        return Arrays.stream(historiaUseCase.buscarDiagnosticos(actor(u), q)).map(DiagnosticoResponse::de).toList();
    }

    private static Actor actor(UsuarioAutenticado u) {
        return new Actor(u.usuarioId(), u.rol());
    }
}
