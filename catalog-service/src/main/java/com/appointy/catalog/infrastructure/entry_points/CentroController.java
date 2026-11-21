package com.appointy.catalog.infrastructure.entry_points;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.appointy.catalog.domain.usecase.CentroUseCase;
import com.appointy.catalog.infrastructure.entry_points.dto.CentroRequest;
import com.appointy.catalog.infrastructure.entry_points.dto.CentroResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
@Tag(name = "Centros")
@RestController
@RequestMapping("/api/v1/centros")
@PreAuthorize("hasRole('ADMINISTRATIVO')")
public class CentroController {

    private final CentroUseCase centroUseCase;

    public CentroController(CentroUseCase centroUseCase) {
        this.centroUseCase = centroUseCase;
    }

    @Operation(summary = "Listar centros por nombre; los inactivos solo con incluirInactivos=true")
    @GetMapping
    public List<CentroResponse> listar(@RequestParam(defaultValue = "false") boolean incluirInactivos) {
        return Arrays.stream(centroUseCase.listarCentros(incluirInactivos)).map(CentroResponse::de).toList();
    }

    @Operation(summary = "Crear un centro")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CentroResponse crear(@Valid @RequestBody CentroRequest s) {
        return CentroResponse.de(centroUseCase.crearCentro(new CentroUseCase.CrearCentroComando(s.nombre(), s.direccion(), s.ciudad(), s.telefono())));
    }

    @Operation(summary = "Actualizar un centro")
    @PutMapping("/{id}")
    public CentroResponse actualizar(@PathVariable UUID id, @Valid @RequestBody CentroRequest s) {
        return CentroResponse.de(centroUseCase.actualizarCentro(new CentroUseCase.ActualizarCentroComando(id.toString(), s.nombre(), s.direccion(), s.ciudad(), s.telefono())));
    }

    /** Borrado logico: el centro queda inactivo, no se elimina. */
    @Operation(summary = "Desactivar un centro (borrado lógico)")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable UUID id) {
        centroUseCase.desactivarCentro(id.toString());
    }
}
