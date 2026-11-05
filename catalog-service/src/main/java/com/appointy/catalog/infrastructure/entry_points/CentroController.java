package com.appointy.catalog.infrastructure.entry_points;

import com.appointy.catalog.domain.usecase.ActualizarCentroUseCase;
import com.appointy.catalog.domain.usecase.CrearCentroUseCase;
import com.appointy.catalog.domain.usecase.DesactivarCentroUseCase;
import com.appointy.catalog.infrastructure.entry_points.dto.CentroRequest;
import com.appointy.catalog.infrastructure.entry_points.dto.CentroResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** CU-11 para centros: todo es del administrativo; el listado para pacientes (CU-10) cuelga del profesional. */
@RestController
@RequestMapping("/api/v1/centros")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class CentroController {

    private final CrearCentroUseCase crear;
    private final ActualizarCentroUseCase actualizar;
    private final DesactivarCentroUseCase desactivar;

    public CentroController(CrearCentroUseCase crear, ActualizarCentroUseCase actualizar, DesactivarCentroUseCase desactivar) {
        this.crear = crear;
        this.actualizar = actualizar;
        this.desactivar = desactivar;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CentroResponse crear(@Valid @RequestBody CentroRequest s) {
        return CentroResponse.de(crear.ejecutar(new CrearCentroUseCase.Comando(s.nombre(), s.direccion(), s.ciudad(), s.telefono())));
    }

    @PutMapping("/{id}")
    public CentroResponse actualizar(@PathVariable UUID id, @Valid @RequestBody CentroRequest s) {
        return CentroResponse.de(actualizar.ejecutar(new ActualizarCentroUseCase.Comando(id.toString(), s.nombre(), s.direccion(), s.ciudad(), s.telefono())));
    }

    /** Borrado logico: el centro queda inactivo, no se elimina. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable UUID id) {
        desactivar.ejecutar(id.toString());
    }
}
