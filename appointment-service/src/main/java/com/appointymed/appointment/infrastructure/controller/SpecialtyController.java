package com.appointymed.appointment.infrastructure.controller;

import com.appointymed.appointment.application.dto.CreateSpecialtyRequest;
import com.appointymed.appointment.application.dto.SpecialtyResponse;
import com.appointymed.appointment.application.service.SpecialtyApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/specialties")
@Tag(name = "Specialties", description = "Medical specialties management endpoints")
@PreAuthorize("isAuthenticated()")
public class SpecialtyController {

    private final SpecialtyApplicationService specialtyService;

    public SpecialtyController(SpecialtyApplicationService specialtyService) {
        this.specialtyService = specialtyService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Create medical specialty")
    public ResponseEntity<SpecialtyResponse> createSpecialty(@Valid @RequestBody CreateSpecialtyRequest request) {
        SpecialtyResponse response = specialtyService.createSpecialty(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all active medical specialties")
    public ResponseEntity<List<SpecialtyResponse>> getAllSpecialties() {
        return ResponseEntity.ok(specialtyService.getAllSpecialties());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get medical specialty by ID")
    public ResponseEntity<SpecialtyResponse> getSpecialtyById(@PathVariable UUID id) {
        return ResponseEntity.ok(specialtyService.getSpecialtyById(id));
    }
}
