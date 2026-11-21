package com.appointy.catalog.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.catalog.domain.usecase.ProfesionalUseCase;
import com.appointy.catalog.domain.usecase.HorarioUseCase;
import com.appointy.catalog.domain.usecase.CentroUseCase;
import com.appointy.catalog.domain.model.FiltroProfesionales;
import com.appointy.catalog.infrastructure.entry_points.dto.PaginaProfesionalesResponse;
import com.appointy.catalog.infrastructure.entry_points.dto.ActualizarProfesionalRequest;
import com.appointy.catalog.infrastructure.entry_points.dto.CentroResponse;
import com.appointy.catalog.infrastructure.entry_points.dto.CrearProfesionalRequest;
import com.appointy.catalog.infrastructure.entry_points.dto.DefinirHorarioRequest;
import com.appointy.catalog.infrastructure.entry_points.dto.DetalleProfesionalResponse;
import com.appointy.catalog.infrastructure.entry_points.dto.HorarioResponse;
import com.appointy.catalog.infrastructure.entry_points.dto.IdsRequest;
import com.appointy.catalog.infrastructure.entry_points.dto.ProfesionalResponse;
import com.appointy.catalog.infrastructure.entry_points.dto.ResultadoHorarioResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profesionales")
@RestController
@RequestMapping("/api/v1/profesionales")
public class ProfesionalController {

    private final ProfesionalUseCase profesionalUseCase;
    private final CentroUseCase centroUseCase;
    private final HorarioUseCase horarioUseCase;

    public ProfesionalController(ProfesionalUseCase profesionalUseCase, CentroUseCase centroUseCase, HorarioUseCase horarioUseCase) {
        this.profesionalUseCase = profesionalUseCase;
        this.centroUseCase = centroUseCase;
        this.horarioUseCase = horarioUseCase;
    }

    /** GET all paginado (RA-04): `texto` busca en nombres, apellidos y registro medico. */
    @Operation(summary = "Listar profesionales, paginado, con búsqueda por texto")
    @GetMapping
    public PaginaProfesionalesResponse listar(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String texto, @RequestParam(required = false) Boolean activo) {
        return PaginaProfesionalesResponse.de(profesionalUseCase.listarProfesionales(new FiltroProfesionales(texto, activo), page, size));
    }

    @Operation(summary = "Crear un profesional")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ProfesionalResponse crear(@Valid @RequestBody CrearProfesionalRequest s) {
        return ProfesionalResponse.de(
                profesionalUseCase.crearProfesional(new ProfesionalUseCase.CrearProfesionalComando(s.usuarioId().toString(), s.nombres(), s.apellidos(), s.registroMedico())));
    }

    @Operation(summary = "Actualizar un profesional")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ProfesionalResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarProfesionalRequest s) {
        return ProfesionalResponse.de(
                profesionalUseCase.actualizarProfesional(new ProfesionalUseCase.ActualizarProfesionalComando(id.toString(), s.nombres(), s.apellidos(), s.registroMedico())));
    }

    /** Borrado logico: el profesional queda inactivo, no se elimina. */
    @Operation(summary = "Desactivar un profesional (borrado lógico)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public void desactivar(@PathVariable UUID id) {
        profesionalUseCase.desactivarProfesional(id.toString());
    }

    @Operation(summary = "Consultar un profesional con sus especialidades y centros")
    @GetMapping("/{id}")
    public DetalleProfesionalResponse consultar(@PathVariable UUID id) {
        return DetalleProfesionalResponse.de(profesionalUseCase.consultarProfesional(id.toString()));
    }

    /** CU-10: el paciente elige centro cuando el profesional atiende en mas de uno. */
    @Operation(summary = "Centros activos donde atiende un profesional")
    @GetMapping("/{id}/centros")
    public List<CentroResponse> centros(@PathVariable UUID id) {
        return Arrays.stream(centroUseCase.listarCentrosDeProfesional(id.toString())).map(CentroResponse::de).toList();
    }

    /** CU-12. */
    @Operation(summary = "Reemplazar las especialidades de un profesional")
    @PutMapping("/{id}/especialidades")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public DetalleProfesionalResponse asociarEspecialidades(@PathVariable UUID id, @Valid @RequestBody IdsRequest s) {
        return DetalleProfesionalResponse.de(profesionalUseCase.asociarEspecialidades(new ProfesionalUseCase.AsociarEspecialidadesComando(id.toString(), s.comoTexto())));
    }

    /** CU-12. */
    @Operation(summary = "Reemplazar los centros de un profesional")
    @PutMapping("/{id}/centros")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public DetalleProfesionalResponse asociarCentros(@PathVariable UUID id, @Valid @RequestBody IdsRequest s) {
        return DetalleProfesionalResponse.de(profesionalUseCase.asociarCentros(new ProfesionalUseCase.AsociarCentrosComando(id.toString(), s.comoTexto())));
    }

    @Operation(summary = "Horarios de un profesional")
    @GetMapping("/{id}/horarios")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'PROFESIONAL')")
    public List<HorarioResponse> horarios(@PathVariable UUID id) {
        return Arrays.stream(horarioUseCase.listarHorarios(id.toString())).map(HorarioResponse::de).toList();
    }

    /** CU-13: el horario siempre se guarda; la respuesta dice si appointment alcanzo a generar las franjas. */
    @Operation(summary = "Definir un horario semanal y generar sus franjas")
    @PostMapping("/{id}/horarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ResultadoHorarioResponse definirHorario(@PathVariable UUID id, @Valid @RequestBody DefinirHorarioRequest s) {
        return ResultadoHorarioResponse.de(horarioUseCase.definirHorario(s.aComando(id.toString())));
    }
}
