package com.appointy.auth.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.auth.domain.usecase.CuentaUseCase;
import com.appointy.auth.domain.usecase.PerfilUseCase;
import com.appointy.auth.domain.model.EstadisticasUsuarios;
import com.appointy.auth.domain.model.FiltroUsuarios;
import com.appointy.auth.domain.model.Rol;
import com.appointy.auth.infrastructure.entry_points.dto.PaginaUsuariosResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.appointy.auth.infrastructure.entry_points.dto.CorregirIdentidadRequest;
import com.appointy.auth.infrastructure.entry_points.dto.CrearCuentaRequest;
import com.appointy.auth.infrastructure.entry_points.dto.CuentaResponse;
import com.appointy.auth.infrastructure.entry_points.dto.PacienteResponse;
import com.appointy.auth.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Administración de cuentas")
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class AdminController {

    private final CuentaUseCase cuentas;
    private final PerfilUseCase perfiles;

    public AdminController(CuentaUseCase cuentas, PerfilUseCase perfiles) {
        this.cuentas = cuentas;
        this.perfiles = perfiles;
    }

    @Operation(summary = "Crear una cuenta de PROFESIONAL o ADMINISTRATIVO")
    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaResponse crearCuenta(@Valid @RequestBody CrearCuentaRequest solicitud) {
        return CuentaResponse.de(cuentas.crearCuentaStaff(solicitud.aComando()));
    }

    @Operation(summary = "Corregir cédula, nombres o nacimiento de un paciente")
    @PatchMapping("/pacientes/{usuarioId}/identidad")
    public PacienteResponse corregirIdentidad(
            @AuthenticationPrincipal UsuarioAutenticado actor,
            @PathVariable UUID usuarioId,
            @Valid @RequestBody CorregirIdentidadRequest solicitud) {
        return PacienteResponse.de(perfiles.corregirDatosInmutables(solicitud.aComando(actor.usuarioId(), usuarioId.toString())));
    }

    /** GET all paginado (RA-04). */
    @Operation(summary = "Listar cuentas, paginado y con filtros por rol y estado")
    @GetMapping("/usuarios")
    public PaginaUsuariosResponse listar(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Rol rol, @RequestParam(required = false) Boolean activo) {
        return PaginaUsuariosResponse.de(cuentas.listarUsuarios(new FiltroUsuarios(rol, activo), page, size));
    }

    @Operation(summary = "Conteo de cuentas por rol y estado")
    @GetMapping("/usuarios/estadisticas")
    public EstadisticasUsuarios estadisticas() {
        return cuentas.consultarEstadisticasUsuarios();
    }

    @Operation(summary = "Desactivar una cuenta (idempotente; no la propia)")
    @PostMapping("/usuarios/{usuarioId}/desactivar")
    public CuentaResponse desactivar(@AuthenticationPrincipal UsuarioAutenticado actor, @PathVariable UUID usuarioId) {
        return CuentaResponse.de(cuentas.cambiarEstadoCuenta(new CuentaUseCase.CambiarEstadoCuentaComando(actor.usuarioId(), usuarioId.toString(), false)));
    }

    @Operation(summary = "Activar una cuenta (idempotente)")
    @PostMapping("/usuarios/{usuarioId}/activar")
    public CuentaResponse activar(@AuthenticationPrincipal UsuarioAutenticado actor, @PathVariable UUID usuarioId) {
        return CuentaResponse.de(cuentas.cambiarEstadoCuenta(new CuentaUseCase.CambiarEstadoCuentaComando(actor.usuarioId(), usuarioId.toString(), true)));
    }
}
