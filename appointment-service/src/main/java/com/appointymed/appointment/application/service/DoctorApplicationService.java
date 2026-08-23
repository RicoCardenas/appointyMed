package com.appointymed.appointment.application.service;

import com.appointymed.appointment.application.dto.*;
import com.appointymed.appointment.application.mapper.DoctorMapper;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DoctorSchedule;
import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.model.TimeSlot;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.DoctorScheduleRepository;
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
public class DoctorApplicationService {
    private final RegisterDoctorUseCase registerDoctorUseCase;
    private final GetAllDoctorsUseCase getAllDoctorsUseCase;
    private final GetDoctorByIdUseCase getDoctorByIdUseCase;
    private final UpdateDoctorScheduleUseCase updateDoctorScheduleUseCase;
    private final GetDoctorAvailabilityUseCase getDoctorAvailabilityUseCase;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final SpecialtyRepository specialtyRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    public DoctorApplicationService(RegisterDoctorUseCase registerDoctorUseCase,
                                  GetAllDoctorsUseCase getAllDoctorsUseCase,
                                  GetDoctorByIdUseCase getDoctorByIdUseCase,
                                  UpdateDoctorScheduleUseCase updateDoctorScheduleUseCase,
                                  GetDoctorAvailabilityUseCase getDoctorAvailabilityUseCase,
                                  DoctorScheduleRepository doctorScheduleRepository,
                                  SpecialtyRepository specialtyRepository,
                                  DoctorRepository doctorRepository,
                                  DoctorMapper doctorMapper) {
        this.registerDoctorUseCase = registerDoctorUseCase;
        this.getAllDoctorsUseCase = getAllDoctorsUseCase;
        this.getDoctorByIdUseCase = getDoctorByIdUseCase;
        this.updateDoctorScheduleUseCase = updateDoctorScheduleUseCase;
        this.getDoctorAvailabilityUseCase = getDoctorAvailabilityUseCase;
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.specialtyRepository = specialtyRepository;
        this.doctorRepository = doctorRepository;
        this.doctorMapper = doctorMapper;
    }

    public DoctorResponse registerDoctor(RegisterDoctorRequest request) {
        var command = new RegisterDoctorUseCase.RegisterDoctorCommand(
                request.userId(),
                request.medicalLicense(),
                request.specialtyId(),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.consultationRoom()
        );
        Doctor doctor = registerDoctorUseCase.execute(command);
        String specialtyName = specialtyRepository.findById(doctor.getSpecialtyId())
                .map(Specialty::getName)
                .orElse("Especialidad General");
        return doctorMapper.toResponse(doctor, specialtyName);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors(UUID specialtyId) {
        List<Doctor> doctors = getAllDoctorsUseCase.execute(specialtyId);
        List<DoctorResponse> responses = new ArrayList<>();
        for (Doctor doc : doctors) {
            String specialtyName = specialtyRepository.findById(doc.getSpecialtyId())
                    .map(Specialty::getName)
                    .orElse("N/A");
            List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doc.getId());
            Doctor docWithSchedules = Doctor.builder()
                    .id(doc.getId())
                    .userId(doc.getUserId())
                    .medicalLicense(doc.getMedicalLicense())
                    .specialtyId(doc.getSpecialtyId())
                    .firstName(doc.getFirstName())
                    .lastName(doc.getLastName())
                    .email(doc.getEmail())
                    .phone(doc.getPhone())
                    .consultationRoom(doc.getConsultationRoom())
                    .active(doc.isActive())
                    .schedules(schedules)
                    .createdAt(doc.getCreatedAt())
                    .build();
            responses.add(doctorMapper.toResponse(docWithSchedules, specialtyName));
        }
        return responses;
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID id) {
        Doctor doc = getDoctorByIdUseCase.execute(id);
        List<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doc.getId());
        String specialtyName = specialtyRepository.findById(doc.getSpecialtyId())
                .map(Specialty::getName)
                .orElse("N/A");

        Doctor docWithSchedules = Doctor.builder()
                .id(doc.getId())
                .userId(doc.getUserId())
                .medicalLicense(doc.getMedicalLicense())
                .specialtyId(doc.getSpecialtyId())
                .firstName(doc.getFirstName())
                .lastName(doc.getLastName())
                .email(doc.getEmail())
                .phone(doc.getPhone())
                .consultationRoom(doc.getConsultationRoom())
                .active(doc.isActive())
                .schedules(schedules)
                .createdAt(doc.getCreatedAt())
                .build();

        return doctorMapper.toResponse(docWithSchedules, specialtyName);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorByUserId(UUID userId) {
        Doctor doc = getDoctorByIdUseCase.executeByUserId(userId);
        return getDoctorById(doc.getId());
    }

    public List<ScheduleResponse> updateDoctorSchedules(UUID doctorId, DoctorScheduleRequest request) {
        List<UpdateDoctorScheduleUseCase.DayScheduleItem> items = request.schedules().stream()
                .map(s -> new UpdateDoctorScheduleUseCase.DayScheduleItem(
                        s.dayOfWeek(), s.startTime(), s.endTime(), s.slotDurationMinutes(), s.active()
                )).toList();

        List<DoctorSchedule> schedules = updateDoctorScheduleUseCase.execute(doctorId, items);
        return schedules.stream().map(doctorMapper::toScheduleResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> getDoctorAvailability(UUID doctorId, LocalDate date) {
        List<TimeSlot> slots = getDoctorAvailabilityUseCase.execute(doctorId, date);
        return slots.stream()
                .map(slot -> new AvailableSlotResponse(slot.startDateTime(), slot.endDateTime(), slot.available()))
                .toList();
    }
}
