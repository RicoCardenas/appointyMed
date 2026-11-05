package com.appointy.catalog.infrastructure.entry_points;

import com.appointy.catalog.domain.port.in.ActualizarEspecialidadPort;
import com.appointy.catalog.domain.port.in.CrearEspecialidadPort;
import com.appointy.catalog.domain.port.in.DesactivarEspecialidadPort;
import com.appointy.catalog.domain.port.in.ListarEspecialidadesPort;
import com.appointy.catalog.domain.port.in.ListarProfesionalesPorEspecialidadPort;
import com.appointy.catalog.infrastructure.entry_points.dto.EspecialidadRequest;
import com.appointy.catalog.infrastructure.entry_points.dto.EspecialidadResponse;
import com.appointy.catalog.infrastructure.entry_points.dto.ProfesionalResponse;
import com.appointy.catalog.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/especialidades")
public class EspecialidadController {

    private final ListarEspecialidadesPort listar;
    private final CrearEspecialidadPort crear;
    private final ActualizarEspecialidadPort actualizar;
    private final DesactivarEspecialidadPort desactivar;
    private final ListarProfesionalesPorEspecialidadPort listarProfesionales;

    public EspecialidadController(
            ListarEspecialidadesPort listar,
            CrearEspecialidadPort crear,
            ActualizarEspecialidadPort actualizar,
            DesactivarEspecialidadPort desactivar,
            ListarProfesionalesPorEspecialidadPort listarProfesionales) {
        this.listar = listar;
        this.crear = crear;
        this.actualizar = actualizar;
        this.desactivar = desactivar;
        this.listarProfesionales = listarProfesionales;
    }

    /** CU-08. Solo un administrativo puede pedir tambien las inactivas. */
    @GetMapping
    public List<EspecialidadResponse> listar(
            @AuthenticationPrincipal UsuarioAutenticado usuario, @RequestParam(defaultValue = "false") boolean incluirInactivas) {
        if (incluirInactivas && !"ADMINISTRATIVO".equals(usuario.rol())) {
            throw new AccessDeniedException("Solo un administrativo ve las especialidades inactivas");
        }
        return Arrays.stream(listar.ejecutar(incluirInactivas)).map(EspecialidadResponse::de).toList();
    }

    /** CU-09. */
    @GetMapping("/{id}/profesionales")
    public List<ProfesionalResponse> profesionales(@PathVariable UUID id) {
        return Arrays.stream(listarProfesionales.ejecutar(id.toString())).map(ProfesionalResponse::de).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public EspecialidadResponse crear(@Valid @RequestBody EspecialidadRequest solicitud) {
        return EspecialidadResponse.de(crear.ejecutar(new CrearEspecialidadPort.Comando(solicitud.nombre(), solicitud.descripcion())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public EspecialidadResponse actualizar(@PathVariable UUID id, @Valid @RequestBody EspecialidadRequest solicitud) {
        return EspecialidadResponse.de(
                actualizar.ejecutar(new ActualizarEspecialidadPort.Comando(id.toString(), solicitud.nombre(), solicitud.descripcion())));
    }

    /** Borrado logico: la especialidad queda inactiva, no se elimina. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMINISTRATIVO')")
    public void desactivar(@PathVariable UUID id) {
        desactivar.ejecutar(id.toString());
    }
}
