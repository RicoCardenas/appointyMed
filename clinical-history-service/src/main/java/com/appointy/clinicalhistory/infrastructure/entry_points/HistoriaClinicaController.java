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
import java.util.UUID;
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

    public HistoriaClinicaController(RegistrarAtencionUseCase registrar, ConsultarHistoriaPacienteUseCase historiaDePaciente,
            ConsultarMiHistoriaUseCase miHistoria, CorregirEntradaUseCase corregir) {
        this.registrar = registrar;
        this.historiaDePaciente = historiaDePaciente;
        this.miHistoria = miHistoria;
        this.corregir = corregir;
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

    private static Actor actor(UsuarioAutenticado u) {
        return new Actor(u.usuarioId(), u.rol());
    }
}
