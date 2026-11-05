package com.appointy.catalog.infrastructure.entry_points;

import com.appointy.catalog.domain.port.in.BloquearFranjasPort;
import com.appointy.catalog.infrastructure.entry_points.dto.BloqueoRequest;
import com.appointy.catalog.infrastructure.entry_points.dto.BloqueoResponse;
import com.appointy.catalog.infrastructure.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** CU-16: 409 con la lista de citas afectadas si hay citas confirmadas; 503 si appointment no responde (no se guarda nada). */
@RestController
@RequestMapping("/api/v1/bloqueos")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class BloqueoController {

    private final BloquearFranjasPort bloquear;

    public BloqueoController(BloquearFranjasPort bloquear) {
        this.bloquear = bloquear;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BloqueoResponse bloquear(@AuthenticationPrincipal UsuarioAutenticado actor, @Valid @RequestBody BloqueoRequest s) {
        return BloqueoResponse.de(bloquear.ejecutar(s.aComando(actor.usuarioId())));
    }
}
