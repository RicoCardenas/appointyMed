package com.appointymed.appointment.application.service;

import com.appointymed.appointment.application.dto.*;
import com.appointymed.appointment.application.mapper.DoctorMapper;
import com.appointymed.appointment.domain.model.Doctor;
import com.appointymed.appointment.domain.model.DoctorSchedule;
import com.appointymed.appointment.domain.model.Specialty;
import com.appointymed.appointment.domain.model.TimeSlot;
import com.appointymed.appointment.domain.model.DomainId;
import com.appointymed.appointment.domain.model.DomainList;
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
import com.appointymed.appointment.application.mapper.DomainValueMapper;

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
                DomainId.of(request.userId().toString()),
                request.medicalLicense(),
                DomainId.of(request.specialtyId().toString()),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.consultationRoom()
        );
        Doctor doctor = registerDoctorUseCase.execute(command);
        var specialty = specialtyRepository.findById(doctor.getSpecialtyId());
        String specialtyName = specialty.isPresent() ? specialty.get().getName() : "Especialidad General";
        return doctorMapper.toResponse(doctor, specialtyName);
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors(UUID specialtyId) {
        DomainList<Doctor> doctors = getAllDoctorsUseCase.execute(DomainValueMapper.toDomainId(specialtyId));
        List<DoctorResponse> responses = new ArrayList<>();
        for (int index = 0; index < doctors.size(); index++) {
            Doctor doc = doctors.get(index);
            var specialty = specialtyRepository.findById(doc.getSpecialtyId());
            String specialtyName = specialty.isPresent() ? specialty.get().getName() : "N/A";
            DomainList<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doc.getId());
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
        Doctor doc = getDoctorByIdUseCase.execute(DomainId.of(id.toString()));
        DomainList<DoctorSchedule> schedules = doctorScheduleRepository.findByDoctorId(doc.getId());
        var specialty = specialtyRepository.findById(doc.getSpecialtyId());
        String specialtyName = specialty.isPresent() ? specialty.get().getName() : "N/A";

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
        Doctor doc = getDoctorByIdUseCase.executeByUserId(DomainId.of(userId.toString()));
        return getDoctorById(UUID.fromString(doc.getId().value()));
    }

    public List<ScheduleResponse> updateDoctorSchedules(UUID doctorId, DoctorScheduleRequest request) {
        DomainList<UpdateDoctorScheduleUseCase.DayScheduleItem> items = new DomainList<>();
        for (var schedule : request.schedules()) items.add(new UpdateDoctorScheduleUseCase.DayScheduleItem(
                schedule.dayOfWeek(), DomainValueMapper.toDomainTime(schedule.startTime()), DomainValueMapper.toDomainTime(schedule.endTime()), schedule.slotDurationMinutes(), schedule.active()));
        DomainList<DoctorSchedule> schedules = updateDoctorScheduleUseCase.execute(DomainId.of(doctorId.toString()), items);
        List<ScheduleResponse> result = new ArrayList<>();
        for (int index = 0; index < schedules.size(); index++) result.add(doctorMapper.toScheduleResponse(schedules.get(index)));
        return result;
    }

    @Transactional(readOnly = true)
    public List<AvailableSlotResponse> getDoctorAvailability(UUID doctorId, LocalDate date) {
        DomainList<TimeSlot> slots = getDoctorAvailabilityUseCase.execute(DomainId.of(doctorId.toString()), DomainValueMapper.toDomainDate(date));
        List<AvailableSlotResponse> result = new ArrayList<>();
        for (int index = 0; index < slots.size(); index++) {
            TimeSlot slot = slots.get(index);
            result.add(new AvailableSlotResponse(DomainValueMapper.toLocalDateTime(slot.startDateTime()), DomainValueMapper.toLocalDateTime(slot.endDateTime()), slot.available()));
        }
        return result;
    }
}
