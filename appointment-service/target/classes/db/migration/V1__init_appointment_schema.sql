-- Appointment Service Database Schema
-- Version 1: Initial schema for specialties, doctors, patients, schedules, and appointments

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. Specialties table
CREATE TABLE specialties (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    consultation_duration_minutes INT NOT NULL DEFAULT 30,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Doctors table
CREATE TABLE doctors (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    medical_license VARCHAR(50) NOT NULL UNIQUE,
    specialty_id UUID NOT NULL REFERENCES specialties(id) ON DELETE RESTRICT,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    consultation_room VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Patients table
CREATE TABLE patients (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE,
    document_type VARCHAR(20) NOT NULL DEFAULT 'CC',
    document_number VARCHAR(50) NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    birth_date DATE,
    blood_type VARCHAR(10),
    address VARCHAR(255),
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Doctor Schedules table (weekly recurring working hours)
CREATE TABLE doctor_schedules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    day_of_week INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7), -- 1: Monday ... 7: Sunday
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INT NOT NULL DEFAULT 30,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_time_window CHECK (end_time > start_time),
    CONSTRAINT uk_doctor_day UNIQUE (doctor_id, day_of_week)
);

-- 5. Appointments table
CREATE TABLE appointments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    doctor_id UUID NOT NULL REFERENCES doctors(id) ON DELETE RESTRICT,
    patient_id UUID NOT NULL REFERENCES patients(id) ON DELETE RESTRICT,
    specialty_id UUID NOT NULL REFERENCES specialties(id) ON DELETE RESTRICT,
    appointment_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    reason VARCHAR(500) NOT NULL,
    cancellation_reason VARCHAR(500),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Performance & Integrity Indexes
CREATE INDEX idx_doctors_specialty ON doctors(specialty_id);
CREATE INDEX idx_doctors_user_id ON doctors(user_id);
CREATE INDEX idx_patients_user_id ON patients(user_id);
CREATE INDEX idx_patients_document ON patients(document_number);
CREATE INDEX idx_appointments_doctor ON appointments(doctor_id);
CREATE INDEX idx_appointments_patient ON appointments(patient_id);
CREATE INDEX idx_appointments_date ON appointments(appointment_date_time);
CREATE INDEX idx_appointments_status ON appointments(status);

-- Anti-collision constraint: Doctor cannot have two non-cancelled appointments at the exact same time
CREATE UNIQUE INDEX idx_doctor_active_appointment 
ON appointments(doctor_id, appointment_date_time) 
WHERE status NOT IN ('CANCELLED', 'RESCHEDULED');

-- Initial Seed Data: Specialties
INSERT INTO specialties (id, name, description, consultation_duration_minutes) VALUES
    ('11111111-1111-1111-1111-111111111111', 'Medicina General', 'Atención médica primaria y preventiva', 30),
    ('22222222-2222-2222-2222-222222222222', 'Cardiología', 'Prevención, diagnóstico y tratamiento de enfermedades cardiovasculares', 45),
    ('33333333-3333-3333-3333-333333333333', 'Pediatría', 'Atención integral de salud para niños y adolescentes', 30),
    ('44444444-4444-4444-4444-444444444444', 'Dermatología', 'Cuidado y tratamiento de afecciones de la piel', 30),
    ('55555555-5555-5555-5555-555555555555', 'Ortopedia', 'Diagnóstico y tratamiento de lesiones del sistema musculoesquelético', 40)
ON CONFLICT (name) DO NOTHING;
