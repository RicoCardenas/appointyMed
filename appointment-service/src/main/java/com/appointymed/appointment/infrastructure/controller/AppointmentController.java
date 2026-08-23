package com.appointymed.appointment.infrastructure.controller;

import com.appointymed.appointment.application.dto.*;
import com.appointymed.appointment.application.service.AppointmentApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@Tag(name = "Appointments", description = "Medical appointments booking, rescheduling, cancellation, and query endpoints")
@PreAuthorize("isAuthenticated()")
public class AppointmentController {

    private final AppointmentApplicationService appointmentService;

    public AppointmentController(AppointmentApplicationService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    @Operation(summary = "Book a new medical appointment")
    public ResponseEntity<AppointmentResponse> bookAppointment(@Valid @RequestBody BookAppointmentRequest request) {
        AppointmentResponse response = appointmentService.bookAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get appointment details by ID")
    public ResponseEntity<AppointmentResponse> getAppointmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.getAppointmentById(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an existing appointment")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody CancelAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(id, request));
    }

    @PostMapping("/{id}/reschedule")
    @Operation(summary = "Reschedule an appointment to a new date and time")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable UUID id,
            @Valid @RequestBody RescheduleAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.rescheduleAppointment(id, request));
    }

    @GetMapping("/patient/{patientId}")
    @Operation(summary = "Get all appointments for a patient")
    public ResponseEntity<List<AppointmentResponse>> getPatientAppointments(@PathVariable UUID patientId) {
        return ResponseEntity.ok(appointmentService.getPatientAppointments(patientId));
    }

    @GetMapping("/patient/{patientId}/upcoming")
    @Operation(summary = "Get upcoming appointments for a patient")
    public ResponseEntity<List<AppointmentResponse>> getUpcomingPatientAppointments(@PathVariable UUID patientId) {
        return ResponseEntity.ok(appointmentService.getUpcomingPatientAppointments(patientId));
    }

    @GetMapping("/patient/{patientId}/history")
    @Operation(summary = "Get appointment history for a patient")
    public ResponseEntity<List<AppointmentResponse>> getPatientAppointmentHistory(@PathVariable UUID patientId) {
        return ResponseEntity.ok(appointmentService.getPatientAppointmentHistory(patientId));
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(summary = "Get doctor appointments (optionally for a specific date)")
    public ResponseEntity<List<AppointmentResponse>> getDoctorAppointments(
            @PathVariable UUID doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorId, date));
    }
}
