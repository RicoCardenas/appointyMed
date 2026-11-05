package com.appointy.auth.infrastructure.entry_points;

import com.appointy.auth.domain.port.in.CorregirDatosInmutablesPort;
import com.appointy.auth.domain.port.in.CrearCuentaStaffPort;
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

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class AdminController {

    private final CrearCuentaStaffPort crearCuenta;
    private final CorregirDatosInmutablesPort corregirDatos;

    public AdminController(CrearCuentaStaffPort crearCuenta, CorregirDatosInmutablesPort corregirDatos) {
        this.crearCuenta = crearCuenta;
        this.corregirDatos = corregirDatos;
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public CuentaResponse crearCuenta(@Valid @RequestBody CrearCuentaRequest solicitud) {
        return CuentaResponse.de(crearCuenta.ejecutar(solicitud.aComando()));
    }

    @PatchMapping("/pacientes/{usuarioId}/identidad")
    public PacienteResponse corregirIdentidad(
            @AuthenticationPrincipal UsuarioAutenticado actor,
            @PathVariable UUID usuarioId,
            @Valid @RequestBody CorregirIdentidadRequest solicitud) {
        return PacienteResponse.de(corregirDatos.ejecutar(solicitud.aComando(actor.usuarioId(), usuarioId.toString())));
    }
}
