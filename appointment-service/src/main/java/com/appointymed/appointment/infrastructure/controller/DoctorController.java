package com.appointymed.appointment.infrastructure.controller;

import com.appointymed.appointment.application.dto.*;
import com.appointymed.appointment.application.service.DoctorApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/doctors")
@Tag(name = "Doctors", description = "Doctor registration, schedules, and availability endpoints")
public class DoctorController {

    private final DoctorApplicationService doctorService;

    public DoctorController(DoctorApplicationService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    @Operation(summary = "Register new doctor")
    public ResponseEntity<DoctorResponse> registerDoctor(@Valid @RequestBody RegisterDoctorRequest request) {
        DoctorResponse response = doctorService.registerDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List doctors (optionally filtered by specialty)")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors(@RequestParam(required = false) UUID specialtyId) {
        return ResponseEntity.ok(doctorService.getAllDoctors(specialtyId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get doctor profile by ID")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable UUID id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get doctor profile by auth User ID")
    public ResponseEntity<DoctorResponse> getDoctorByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(doctorService.getDoctorByUserId(userId));
    }

    @PutMapping("/{id}/schedules")
    @Operation(summary = "Configure weekly working schedule for doctor")
    public ResponseEntity<List<ScheduleResponse>> updateDoctorSchedules(
            @PathVariable UUID id,
            @Valid @RequestBody DoctorScheduleRequest request) {
        return ResponseEntity.ok(doctorService.updateDoctorSchedules(id, request));
    }

    @GetMapping("/{id}/availability")
    @Operation(summary = "Query available appointment time slots for doctor on a specific date")
    public ResponseEntity<List<AvailableSlotResponse>> getDoctorAvailability(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(doctorService.getDoctorAvailability(id, date));
    }
}
