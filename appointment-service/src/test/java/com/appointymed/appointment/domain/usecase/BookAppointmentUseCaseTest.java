package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.exception.AppointmentConflictException;
import com.appointymed.appointment.domain.model.*;
import com.appointymed.appointment.domain.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookAppointmentUseCaseTest {
    @Mock AppointmentRepository appointments;
    @Mock DoctorRepository doctors;
    @Mock PatientRepository patients;
    @Mock SpecialtyRepository specialties;
    @Mock DoctorScheduleRepository schedules;
    @Mock AppointmentEventPublisherPort events;

    private DomainId doctorId;
    private DomainId patientId;
    private DomainId specialtyId;
    private DomainDateTime appointmentTime;
    private BookAppointmentUseCase useCase;

    @BeforeEach
    void setUp() {
        doctorId = DomainId.of("11111111-1111-1111-1111-111111111111");
        patientId = DomainId.of("22222222-2222-2222-2222-222222222222");
        specialtyId = DomainId.of("33333333-3333-3333-3333-333333333333");
        long today = System.currentTimeMillis() / 86400000L;
        DomainDate date = DomainDate.ofEpochDay(today + 7);
        appointmentTime = date.atTime(DomainTime.ofMinuteOfDay(9 * 60));
        DomainClock clock = () -> DomainDateTime.ofEpochMillis(System.currentTimeMillis());
        useCase = new BookAppointmentUseCase(appointments, doctors, patients, specialties, schedules, events, clock);

        Doctor doctor = Doctor.builder().id(doctorId).userId(DomainId.of("44444444-4444-4444-4444-444444444444"))
                .specialtyId(specialtyId).medicalLicense("LIC-123").firstName("Ana").lastName("Martinez").email("ana@example.com").active(true).build();
        Patient patient = Patient.builder().id(patientId).documentNumber("123").firstName("Carlos").lastName("Lopez").email("carlos@example.com").phone("+123").build();
        Specialty specialty = Specialty.builder().id(specialtyId).name("Cardiology").consultationDurationMinutes(30).active(true).build();
        DoctorSchedule schedule = DoctorSchedule.builder().doctorId(doctorId).dayOfWeek(date.getDayOfWeek()).startTime(DomainTime.ofMinuteOfDay(8 * 60)).endTime(DomainTime.ofMinuteOfDay(12 * 60)).slotDurationMinutes(30).active(true).build();
        when(doctors.findById(doctorId)).thenReturn(DomainOptional.of(doctor));
        when(patients.findById(patientId)).thenReturn(DomainOptional.of(patient));
        when(specialties.findById(specialtyId)).thenReturn(DomainOptional.of(specialty));
        when(schedules.findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek())).thenReturn(DomainOptional.of(schedule));
    }

    @Test
    void booksAppointment() {
        when(appointments.hasActiveAppointmentConflict(eq(doctorId), any(), any(), isNull())).thenReturn(false);
        when(appointments.save(any(Appointment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Appointment result = useCase.execute(new BookAppointmentUseCase.BookAppointmentCommand(patientId, doctorId, specialtyId, appointmentTime, "Routine", ""));
        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(result.getAppointmentDateTime()).isEqualTo(appointmentTime);
        assertThat(result.getEndDateTime()).isEqualTo(appointmentTime.plusMinutes(30));
        verify(events).publishAppointmentBooked(any(), anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void rejectsConflict() {
        when(appointments.hasActiveAppointmentConflict(eq(doctorId), any(), any(), isNull())).thenReturn(true);
        assertThatThrownBy(() -> useCase.execute(new BookAppointmentUseCase.BookAppointmentCommand(patientId, doctorId, specialtyId, appointmentTime, "Routine", "")))
                .isInstanceOf(AppointmentConflictException.class);
        verify(appointments, never()).save(any());
    }
}
