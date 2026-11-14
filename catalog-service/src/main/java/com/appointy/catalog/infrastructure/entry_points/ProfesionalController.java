package com.appointy.catalog.infrastructure.entry_points;

import com.appointy.catalog.domain.usecase.ActualizarProfesionalUseCase;
import com.appointy.catalog.domain.usecase.AsociarCentrosUseCase;
import com.appointy.catalog.domain.usecase.AsociarEspecialidadesUseCase;
import com.appointy.catalog.domain.usecase.ConsultarProfesionalUseCase;
import com.appointy.catalog.domain.usecase.CrearProfesionalUseCase;
import com.appointy.catalog.domain.usecase.DefinirHorarioUseCase;
import com.appointy.catalog.domain.usecase.DesactivarProfesionalUseCase;
import com.appointy.catalog.domain.usecase.ListarCentrosDeProfesionalUseCase;
import com.appointy.catalog.domain.model.FiltroProfesionales;
import com.appointy.catalog.domain.usecase.ListarHorariosUseCase;
import com.appointy.catalog.domain.usecase.ListarProfesionalesUseCase;
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

@RestController
@RequestMapping("/api/v1/profesionales")
public class ProfesionalController {

    private final CrearProfesionalUseCase crear;
    private final ActualizarProfesionalUseCase actualizar;
    private final DesactivarProfesionalUseCase desactivar;
    private final ConsultarProfesionalUseCase consultar;
    private final ListarCentrosDeProfesionalUseCase listarCentros;
    private final AsociarEspecialidadesUseCase asociarEspecialidades;
    private final AsociarCentrosUseCase asociarCentros;
    private final DefinirHorarioUseCase definirHorario;
    private final ListarHorariosUseCase listarHorarios;
    private final ListarProfesionalesUseCase listar;

    public ProfesionalController(
            CrearProfesionalUseCase crear,
            ActualizarProfesionalUseCase actualizar,
            DesactivarProfesionalUseCase desactivar,
            ConsultarProfesionalUseCase consultar,
            ListarCentrosDeProfesionalUseCase listarCentros,
            AsociarEspecialidadesUseCase asociarEspecialidades,
            AsociarCentrosUseCase asociarCentros,
            DefinirHorarioUseCase definirHorario,
            ListarHorariosUseCase listarHorarios,
            ListarProfesionalesUseCase listar) {
        this.crear = crear;
        this.actualizar = actualizar;
        this.desactivar = desactivar;
        this.consultar = consultar;
        this.listarCentros = listarCentros;
        this.asociarEspecialidades = asociarEspecialidades;
        this.asociarCentros = asociarCentros;
        this.definirHorario = definirHorario;
        this.listarHorarios = listarHorarios;
        this.listar = listar;
    }

    /** GET all paginado (RA-04): `texto` busca en nombres, apellidos y registro medico. */
    @GetMapping
    public PaginaProfesionalesResponse listar(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String texto, @RequestParam(required = false) Boolean activo) {
        return PaginaProfesionalesResponse.de(listar.ejecutar(new FiltroProfesionales(texto, activo), page, size));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ProfesionalResponse crear(@Valid @RequestBody CrearProfesionalRequest s) {
        return ProfesionalResponse.de(
                crear.ejecutar(new CrearProfesionalUseCase.Comando(s.usuarioId().toString(), s.nombres(), s.apellidos(), s.registroMedico())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ProfesionalResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarProfesionalRequest s) {
        return ProfesionalResponse.de(
                actualizar.ejecutar(new ActualizarProfesionalUseCase.Comando(id.toString(), s.nombres(), s.apellidos(), s.registroMedico())));
    }

    /** Borrado logico: el profesional queda inactivo, no se elimina. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public void desactivar(@PathVariable UUID id) {
        desactivar.ejecutar(id.toString());
    }

    @GetMapping("/{id}")
    public DetalleProfesionalResponse consultar(@PathVariable UUID id) {
        return DetalleProfesionalResponse.de(consultar.ejecutar(id.toString()));
    }

    /** CU-10: el paciente elige centro cuando el profesional atiende en mas de uno. */
    @GetMapping("/{id}/centros")
    public List<CentroResponse> centros(@PathVariable UUID id) {
        return Arrays.stream(listarCentros.ejecutar(id.toString())).map(CentroResponse::de).toList();
    }

    /** CU-12. */
    @PutMapping("/{id}/especialidades")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public DetalleProfesionalResponse asociarEspecialidades(@PathVariable UUID id, @Valid @RequestBody IdsRequest s) {
        return DetalleProfesionalResponse.de(asociarEspecialidades.ejecutar(new AsociarEspecialidadesUseCase.Comando(id.toString(), s.comoTexto())));
    }

    /** CU-12. */
    @PutMapping("/{id}/centros")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public DetalleProfesionalResponse asociarCentros(@PathVariable UUID id, @Valid @RequestBody IdsRequest s) {
        return DetalleProfesionalResponse.de(asociarCentros.ejecutar(new AsociarCentrosUseCase.Comando(id.toString(), s.comoTexto())));
    }

    @GetMapping("/{id}/horarios")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'PROFESIONAL')")
    public List<HorarioResponse> horarios(@PathVariable UUID id) {
        return Arrays.stream(listarHorarios.ejecutar(id.toString())).map(HorarioResponse::de).toList();
    }

    /** CU-13: el horario siempre se guarda; la respuesta dice si appointment alcanzo a generar las franjas. */
    @PostMapping("/{id}/horarios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ResultadoHorarioResponse definirHorario(@PathVariable UUID id, @Valid @RequestBody DefinirHorarioRequest s) {
        return ResultadoHorarioResponse.de(definirHorario.ejecutar(s.aComando(id.toString())));
    }
}
