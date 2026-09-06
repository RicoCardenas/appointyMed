package com.appointymed.appointment.infrastructure.config;

import com.appointymed.appointment.domain.model.DomainClock;
import com.appointymed.appointment.domain.model.DomainDateTime;
import com.appointymed.appointment.domain.model.DomainInstant;
import com.appointymed.appointment.domain.repository.*;
import com.appointymed.appointment.domain.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean DomainClock domainClock() { return () -> DomainDateTime.ofEpochMillis(System.currentTimeMillis()); }
    @Bean BookAppointmentUseCase bookAppointmentUseCase(AppointmentRepository a, DoctorRepository d, PatientRepository p, SpecialtyRepository s, DoctorScheduleRepository ds, AppointmentEventPublisherPort e, DomainClock c) { return new BookAppointmentUseCase(a, d, p, s, ds, e, c); }
    @Bean CancelAppointmentUseCase cancelAppointmentUseCase(AppointmentRepository a, PatientRepository p, DoctorRepository d, AppointmentEventPublisherPort e, DomainClock c) { return new CancelAppointmentUseCase(a, p, d, e, c); }
    @Bean RescheduleAppointmentUseCase rescheduleAppointmentUseCase(AppointmentRepository a, DoctorRepository d, PatientRepository p, SpecialtyRepository s, DoctorScheduleRepository ds, AppointmentEventPublisherPort e, DomainClock c) { return new RescheduleAppointmentUseCase(a, d, p, s, ds, e, c); }
    @Bean GetPatientAppointmentsUseCase getPatientAppointmentsUseCase(AppointmentRepository a, DomainClock c) { return new GetPatientAppointmentsUseCase(a, c); }
    @Bean GetDoctorAppointmentsUseCase getDoctorAppointmentsUseCase(AppointmentRepository a) { return new GetDoctorAppointmentsUseCase(a); }
    @Bean RegisterPatientUseCase registerPatientUseCase(PatientRepository r) { return new RegisterPatientUseCase(r); }
    @Bean GetPatientByIdUseCase getPatientByIdUseCase(PatientRepository r) { return new GetPatientByIdUseCase(r); }
    @Bean UpdatePatientUseCase updatePatientUseCase(PatientRepository r) { return new UpdatePatientUseCase(r); }
    @Bean RegisterDoctorUseCase registerDoctorUseCase(DoctorRepository d, SpecialtyRepository s) { return new RegisterDoctorUseCase(d, s); }
    @Bean GetAllDoctorsUseCase getAllDoctorsUseCase(DoctorRepository r) { return new GetAllDoctorsUseCase(r); }
    @Bean GetDoctorByIdUseCase getDoctorByIdUseCase(DoctorRepository r) { return new GetDoctorByIdUseCase(r); }
    @Bean UpdateDoctorScheduleUseCase updateDoctorScheduleUseCase(DoctorRepository d, DoctorScheduleRepository s) { return new UpdateDoctorScheduleUseCase(d, s); }
    @Bean GetDoctorAvailabilityUseCase getDoctorAvailabilityUseCase(DoctorRepository d, DoctorScheduleRepository s, AppointmentRepository a) { return new GetDoctorAvailabilityUseCase(d, s, a); }
    @Bean CreateSpecialtyUseCase createSpecialtyUseCase(SpecialtyRepository r) { return new CreateSpecialtyUseCase(r); }
    @Bean GetAllSpecialtiesUseCase getAllSpecialtiesUseCase(SpecialtyRepository r) { return new GetAllSpecialtiesUseCase(r); }
}
