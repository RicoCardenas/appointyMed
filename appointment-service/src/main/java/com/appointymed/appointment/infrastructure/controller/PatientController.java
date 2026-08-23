package com.appointymed.appointment.infrastructure.controller;

import com.appointymed.appointment.application.dto.PatientResponse;
import com.appointymed.appointment.application.dto.RegisterPatientRequest;
import com.appointymed.appointment.application.dto.UpdatePatientRequest;
import com.appointymed.appointment.application.service.PatientApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@Tag(name = "Patients", description = "Patient registration and profile management endpoints")
public class PatientController {

    private final PatientApplicationService patientService;

    public PatientController(PatientApplicationService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    @Operation(summary = "Register patient profile")
    public ResponseEntity<PatientResponse> registerPatient(@Valid @RequestBody RegisterPatientRequest request) {
        PatientResponse response = patientService.registerPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update patient profile information")
    public ResponseEntity<PatientResponse> updatePatient(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePatientRequest request) {
        PatientResponse response = patientService.updatePatient(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get patient by ID")
    public ResponseEntity<PatientResponse> getPatientById(@PathVariable UUID id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get patient by auth User ID")
    public ResponseEntity<PatientResponse> getPatientByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(patientService.getPatientByUserId(userId));
    }

    @GetMapping
    @Operation(summary = "List all patients (paginated)")
    public ResponseEntity<List<PatientResponse>> getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(patientService.getAllPatients(page, size));
    }
}
