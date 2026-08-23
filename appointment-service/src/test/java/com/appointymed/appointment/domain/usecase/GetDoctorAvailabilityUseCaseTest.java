package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.model.*;
import com.appointymed.appointment.domain.repository.AppointmentRepository;
import com.appointymed.appointment.domain.repository.DoctorRepository;
import com.appointymed.appointment.domain.repository.DoctorScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDoctorAvailabilityUseCaseTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    private GetDoctorAvailabilityUseCase getDoctorAvailabilityUseCase;

    private UUID doctorId;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        getDoctorAvailabilityUseCase = new GetDoctorAvailabilityUseCase(
                doctorRepository, doctorScheduleRepository, appointmentRepository
        );
        doctorId = UUID.randomUUID();
        doctor = Doctor.builder()
                .id(doctorId)
                .userId(UUID.randomUUID())
                .medicalLicense("LIC-999")
                .firstName("Laura")
                .lastName("Gómez")
                .active(true)
                .build();
    }

    @Test
    void shouldGenerateSlotsAndMarkOccupiedOnes() {
        // Monday date
        LocalDate nextMonday = LocalDate.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY);

        // Schedule: 08:00 to 10:00 (4 slots of 30 min: 08:00, 08:30, 09:00, 09:30)
        DoctorSchedule schedule = DoctorSchedule.builder()
                .id(UUID.randomUUID())
                .doctorId(doctorId)
                .dayOfWeek(1)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .slotDurationMinutes(30)
                .active(true)
                .build();

        // 1 existing appointment at 08:30
        Appointment bookedAppointment = Appointment.builder()
                .id(UUID.randomUUID())
                .doctorId(doctorId)
                .patientId(UUID.randomUUID())
                .specialtyId(UUID.randomUUID())
                .appointmentDateTime(nextMonday.atTime(8, 30))
                .endDateTime(nextMonday.atTime(9, 0))
                .status(AppointmentStatus.SCHEDULED)
                .reason("Consultation")
                .build();

        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, 1)).thenReturn(Optional.of(schedule));
        when(appointmentRepository.findActiveAppointmentsByDoctorAndDate(eq(doctorId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(bookedAppointment));

        List<TimeSlot> slots = getDoctorAvailabilityUseCase.execute(doctorId, nextMonday);

        assertThat(slots).hasSize(4);
        assertThat(slots.get(0).available()).isTrue(); // 08:00
        assertThat(slots.get(1).available()).isFalse(); // 08:30 occupied!
        assertThat(slots.get(2).available()).isTrue(); // 09:00
        assertThat(slots.get(3).available()).isTrue(); // 09:30
    }
}
