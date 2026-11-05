package com.appointy.catalog.infrastructure.entry_points;

import com.appointy.catalog.domain.port.in.ActualizarProfesionalPort;
import com.appointy.catalog.domain.port.in.AsociarCentrosPort;
import com.appointy.catalog.domain.port.in.AsociarEspecialidadesPort;
import com.appointy.catalog.domain.port.in.ConsultarProfesionalPort;
import com.appointy.catalog.domain.port.in.CrearProfesionalPort;
import com.appointy.catalog.domain.port.in.DefinirHorarioPort;
import com.appointy.catalog.domain.port.in.DesactivarProfesionalPort;
import com.appointy.catalog.domain.port.in.ListarCentrosDeProfesionalPort;
import com.appointy.catalog.domain.port.in.ListarHorariosPort;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profesionales")
public class ProfesionalController {

    private final CrearProfesionalPort crear;
    private final ActualizarProfesionalPort actualizar;
    private final DesactivarProfesionalPort desactivar;
    private final ConsultarProfesionalPort consultar;
    private final ListarCentrosDeProfesionalPort listarCentros;
    private final AsociarEspecialidadesPort asociarEspecialidades;
    private final AsociarCentrosPort asociarCentros;
    private final DefinirHorarioPort definirHorario;
    private final ListarHorariosPort listarHorarios;

    public ProfesionalController(
            CrearProfesionalPort crear,
            ActualizarProfesionalPort actualizar,
            DesactivarProfesionalPort desactivar,
            ConsultarProfesionalPort consultar,
            ListarCentrosDeProfesionalPort listarCentros,
            AsociarEspecialidadesPort asociarEspecialidades,
            AsociarCentrosPort asociarCentros,
            DefinirHorarioPort definirHorario,
            ListarHorariosPort listarHorarios) {
        this.crear = crear;
        this.actualizar = actualizar;
        this.desactivar = desactivar;
        this.consultar = consultar;
        this.listarCentros = listarCentros;
        this.asociarEspecialidades = asociarEspecialidades;
        this.asociarCentros = asociarCentros;
        this.definirHorario = definirHorario;
        this.listarHorarios = listarHorarios;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ProfesionalResponse crear(@Valid @RequestBody CrearProfesionalRequest s) {
        return ProfesionalResponse.de(
                crear.ejecutar(new CrearProfesionalPort.Comando(s.usuarioId().toString(), s.nombres(), s.apellidos(), s.registroMedico())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public ProfesionalResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ActualizarProfesionalRequest s) {
        return ProfesionalResponse.de(
                actualizar.ejecutar(new ActualizarProfesionalPort.Comando(id.toString(), s.nombres(), s.apellidos(), s.registroMedico())));
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
        return DetalleProfesionalResponse.de(asociarEspecialidades.ejecutar(new AsociarEspecialidadesPort.Comando(id.toString(), s.comoTexto())));
    }

    /** CU-12. */
    @PutMapping("/{id}/centros")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public DetalleProfesionalResponse asociarCentros(@PathVariable UUID id, @Valid @RequestBody IdsRequest s) {
        return DetalleProfesionalResponse.de(asociarCentros.ejecutar(new AsociarCentrosPort.Comando(id.toString(), s.comoTexto())));
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
