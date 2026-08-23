package com.appointymed.appointment.application.service;

import com.appointymed.appointment.application.dto.*;
import com.appointymed.appointment.application.mapper.AppointmentMapper;
import com.appointymed.appointment.domain.exception.ResourceNotFoundException;
import com.appointymed.appointment.domain.model.Appointment;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.Patient;
import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.PatientRepository;
import com.appointymed.appointment.domain.repository.SpecialtyRepository;
import com.appointymed.appointment.domain.usecase.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AppointmentApplicationService {
    private final BookAppointmentUseCase bookAppointmentUseCase;
    private final CancelAppointmentUseCase cancelAppointmentUseCase;
    private final RescheduleAppointmentUseCase rescheduleAppointmentUseCase;
    private final GetPatientAppointmentsUseCase getPatientAppointmentsUseCase;
    private final GetDoctorAppointmentsUseCase getDoctorAppointmentsUseCase;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final SpecialtyRepository specialtyRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentApplicationService(BookAppointmentUseCase bookAppointmentUseCase,
                                       CancelAppointmentUseCase cancelAppointmentUseCase,
                                       RescheduleAppointmentUseCase rescheduleAppointmentUseCase,
                                       GetPatientAppointmentsUseCase getPatientAppointmentsUseCase,
                                       GetDoctorAppointmentsUseCase getDoctorAppointmentsUseCase,
                                       AppointmentRepository appointmentRepository,
                                       DoctorRepository doctorRepository,
                                       PatientRepository patientRepository,
                                       SpecialtyRepository specialtyRepository,
                                       AppointmentMapper appointmentMapper) {
        this.bookAppointmentUseCase = bookAppointmentUseCase;
        this.cancelAppointmentUseCase = cancelAppointmentUseCase;
        this.rescheduleAppointmentUseCase = rescheduleAppointmentUseCase;
        this.getPatientAppointmentsUseCase = getPatientAppointmentsUseCase;
        this.getDoctorAppointmentsUseCase = getDoctorAppointmentsUseCase;
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.specialtyRepository = specialtyRepository;
        this.appointmentMapper = appointmentMapper;
    }

    public AppointmentResponse bookAppointment(BookAppointmentRequest request) {
        var command = new BookAppointmentUseCase.BookAppointmentCommand(
                request.patientId(),
                request.doctorId(),
                request.specialtyId(),
                request.appointmentDateTime(),
                request.reason(),
                request.notes()
        );
        Appointment appointment = bookAppointmentUseCase.execute(command);
        return mapToResponse(appointment);
    }

    public AppointmentResponse cancelAppointment(UUID appointmentId, CancelAppointmentRequest request) {
        Appointment appointment = cancelAppointmentUseCase.execute(appointmentId, request.cancellationReason());
        return mapToResponse(appointment);
    }

    public AppointmentResponse rescheduleAppointment(UUID appointmentId, RescheduleAppointmentRequest request) {
        Appointment appointment = rescheduleAppointmentUseCase.execute(appointmentId, request.newAppointmentDateTime());
        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        return mapToResponse(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointments(UUID patientId) {
        List<Appointment> list = getPatientAppointmentsUseCase.executeAll(patientId);
        return mapListToResponse(list);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getUpcomingPatientAppointments(UUID patientId) {
        List<Appointment> list = getPatientAppointmentsUseCase.executeUpcoming(patientId);
        return mapListToResponse(list);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getPatientAppointmentHistory(UUID patientId) {
        List<Appointment> list = getPatientAppointmentsUseCase.executeHistory(patientId);
        return mapListToResponse(list);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> getDoctorAppointments(UUID doctorId, LocalDate date) {
        List<Appointment> list = getDoctorAppointmentsUseCase.execute(doctorId, date);
        return mapListToResponse(list);
    }

    private AppointmentResponse mapToResponse(Appointment a) {
        String doctorName = doctorRepository.findById(a.getDoctorId()).map(Doctor::getFullName).orElse("Dr. N/A");
        Patient patient = patientRepository.findById(a.getPatientId()).orElse(null);
        String patientName = patient != null ? patient.getFullName() : "N/A";
        String patientPhone = patient != null ? patient.getPhone() : "N/A";
        String specialtyName = specialtyRepository.findById(a.getSpecialtyId()).map(Specialty::getName).orElse("N/A");
        return appointmentMapper.toResponse(a, doctorName, patientName, patientPhone, specialtyName);
    }

    private List<AppointmentResponse> mapListToResponse(List<Appointment> list) {
        List<AppointmentResponse> result = new ArrayList<>();
        for (Appointment a : list) {
            result.add(mapToResponse(a));
        }
        return result;
    }
}
