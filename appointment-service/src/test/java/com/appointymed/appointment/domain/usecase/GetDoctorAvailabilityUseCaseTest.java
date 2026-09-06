package com.appointymed.appointment.domain.usecase;

import com.appointymed.appointment.domain.model.*;
import com.appointymed.appointment.domain.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetDoctorAvailabilityUseCaseTest {
    @Mock DoctorRepository doctors;
    @Mock DoctorScheduleRepository schedules;
    @Mock AppointmentRepository appointments;

    @Test
    void generatesSlotsAndMarksOccupiedOne() {
        DomainId doctorId = DomainId.of("11111111-1111-1111-1111-111111111111");
        long today = System.currentTimeMillis() / 86400000L;
        DomainDate date = DomainDate.ofEpochDay(today + 7);
        Doctor doctor = Doctor.builder().id(doctorId).userId(DomainId.of("22222222-2222-2222-2222-222222222222")).medicalLicense("LIC").firstName("Laura").lastName("Gomez").active(true).build();
        DoctorSchedule schedule = DoctorSchedule.builder().doctorId(doctorId).dayOfWeek(date.getDayOfWeek()).startTime(DomainTime.ofMinuteOfDay(8 * 60)).endTime(DomainTime.ofMinuteOfDay(10 * 60)).slotDurationMinutes(30).active(true).build();
        Appointment booked = Appointment.builder().id(DomainId.of("33333333-3333-3333-3333-333333333333")).doctorId(doctorId).patientId(DomainId.of("44444444-4444-4444-4444-444444444444")).specialtyId(DomainId.of("55555555-5555-5555-5555-555555555555")).appointmentDateTime(date.atTime(DomainTime.ofMinuteOfDay(8 * 60 + 30))).endDateTime(date.atTime(DomainTime.ofMinuteOfDay(9 * 60))).reason("Consultation").build();
        when(doctors.findById(doctorId)).thenReturn(DomainOptional.of(doctor));
        when(schedules.findByDoctorIdAndDayOfWeek(doctorId, date.getDayOfWeek())).thenReturn(DomainOptional.of(schedule));
        DomainList<Appointment> existing = new DomainList<>(); existing.add(booked);
        when(appointments.findActiveAppointmentsByDoctorAndDate(eq(doctorId), any(), any())).thenReturn(existing);

        DomainList<TimeSlot> slots = new GetDoctorAvailabilityUseCase(doctors, schedules, appointments).execute(doctorId, date);
        assertThat(slots.size()).isEqualTo(4);
        assertThat(slots.get(0).available()).isTrue();
        assertThat(slots.get(1).available()).isFalse();
        assertThat(slots.get(2).available()).isTrue();
    }
}
