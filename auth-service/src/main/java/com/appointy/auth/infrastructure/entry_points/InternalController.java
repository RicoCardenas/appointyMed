package com.appointy.auth.infrastructure.entry_points;

import com.appointy.auth.domain.usecase.ConsultarContactoUseCase;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Solo servicio a servicio (llave de servicio). Devuelve el correo, nunca claves ni datos del perfil. */
@RestController
@RequestMapping("/internal")
public class InternalController {

    private final ConsultarContactoUseCase contacto;

    public InternalController(ConsultarContactoUseCase contacto) {
        this.contacto = contacto;
    }

    @GetMapping("/usuarios/{id}/contacto")
    public Contacto contacto(@PathVariable UUID id) {
        return new Contacto(contacto.ejecutar(id.toString()));
    }

    record Contacto(String correo) {}
}
