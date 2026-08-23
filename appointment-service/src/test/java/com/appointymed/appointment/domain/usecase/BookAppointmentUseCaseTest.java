package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.AppointmentConflictException;
import com.appointymed.appointment.domain.model.*;
import com.appointymed.appointment.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookAppointmentUseCaseTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private SpecialtyRepository specialtyRepository;

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;

    @Mock
    private AppointmentEventPublisherPort eventPublisher;

    private BookAppointmentUseCase bookAppointmentUseCase;

    private UUID doctorId;
    private UUID patientId;
    private UUID specialtyId;
    private Doctor doctor;
    private Patient patient;
    private Specialty specialty;
    private DoctorSchedule schedule;

    @BeforeEach
    void setUp() {
        bookAppointmentUseCase = new BookAppointmentUseCase(
                appointmentRepository,
                doctorRepository,
                patientRepository,
                specialtyRepository,
                doctorScheduleRepository,
                eventPublisher
        );

        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        specialtyId = UUID.randomUUID();

        specialty = Specialty.builder()
                .id(specialtyId)
                .name("Cardiología")
                .consultationDurationMinutes(30)
                .active(true)
                .build();

        doctor = Doctor.builder()
                .id(doctorId)
                .userId(UUID.randomUUID())
                .medicalLicense("LIC-12345")
                .specialtyId(specialtyId)
                .firstName("Ana")
                .lastName("Martínez")
                .email("ana@example.com")
                .active(true)
                .build();

        patient = Patient.builder()
                .id(patientId)
                .documentType(DocumentType.CC)
                .documentNumber("12345678")
                .firstName("Carlos")
                .lastName("López")
                .email("carlos@example.com")
                .phone("+573001112233")
                .build();

        // Monday schedule: 08:00 to 12:00
        schedule = DoctorSchedule.builder()
                .id(UUID.randomUUID())
                .doctorId(doctorId)
                .dayOfWeek(1) // Monday
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(12, 0))
                .slotDurationMinutes(30)
                .active(true)
                .build();
    }

    @Test
    void shouldBookAppointmentSuccessfully() {
        // Next Monday at 09:00 AM in the future
        LocalDateTime appointmentTime = LocalDateTime.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY).withHour(9).withMinute(0).withSecond(0).withNano(0);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, 1)).thenReturn(Optional.of(schedule));
        when(appointmentRepository.hasActiveAppointmentConflict(eq(doctorId), any(LocalDateTime.class), any(LocalDateTime.class), isNull())).thenReturn(false);
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        var command = new BookAppointmentUseCase.BookAppointmentCommand(
                patientId, doctorId, specialtyId, appointmentTime, "Routine Checkup", "None"
        );

        Appointment result = bookAppointmentUseCase.execute(command);

        assertThat(result).isNotNull();
        assertThat(result.getDoctorId()).isEqualTo(doctorId);
        assertThat(result.getPatientId()).isEqualTo(patientId);
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(result.getAppointmentDateTime()).isEqualTo(appointmentTime);
        assertThat(result.getEndDateTime()).isEqualTo(appointmentTime.plusMinutes(30));

        verify(appointmentRepository).save(any(Appointment.class));
        verify(eventPublisher).publishAppointmentBooked(any(Appointment.class), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void shouldThrowConflictExceptionWhenSlotIsAlreadyBooked() {
        LocalDateTime appointmentTime = LocalDateTime.now().plusWeeks(1).with(java.time.DayOfWeek.MONDAY).withHour(9).withMinute(0).withSecond(0).withNano(0);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(specialtyRepository.findById(specialtyId)).thenReturn(Optional.of(specialty));
        when(doctorScheduleRepository.findByDoctorIdAndDayOfWeek(doctorId, 1)).thenReturn(Optional.of(schedule));
        when(appointmentRepository.hasActiveAppointmentConflict(eq(doctorId), any(LocalDateTime.class), any(LocalDateTime.class), isNull())).thenReturn(true);

        var command = new BookAppointmentUseCase.BookAppointmentCommand(
                patientId, doctorId, specialtyId, appointmentTime, "Routine Checkup", "None"
        );

        assertThatThrownBy(() -> bookAppointmentUseCase.execute(command))
                .isInstanceOf(AppointmentConflictException.class);

        verify(appointmentRepository, never()).save(any(Appointment.class));
    }
}
