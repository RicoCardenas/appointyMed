package com.appointy.auth.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.auth.domain.usecase.PerfilUseCase;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Solo servicio a servicio (llave de servicio). Devuelve el correo, nunca claves ni datos del perfil. */
@Tag(name = "Interno (servicio a servicio)")
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final PerfilUseCase perfiles;

    public InternalController(PerfilUseCase perfiles) {
        this.perfiles = perfiles;
    }

    @Operation(summary = "Correo de una cuenta activa (notification)")
    @GetMapping("/usuarios/{id}/contacto")
    public Contacto contacto(@PathVariable UUID id) {
        return new Contacto(perfiles.consultarContacto(id.toString()));
    }

    record Contacto(String correo) {}
}
