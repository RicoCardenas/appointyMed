-- Auth Service Database Schema
-- Version 1: Initial schema for authentication and authorization

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Users table
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Roles table
CREATE TABLE roles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(500)
);

-- User-Roles junction table (many-to-many)
CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Permissions table
CREATE TABLE permissions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500)
);

-- Role-Permissions junction table (many-to-many)
CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

-- Refresh tokens table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

-- Audit logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100),
    entity_id UUID,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(45),
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens(expires_at);
CREATE INDEX idx_audit_logs_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);

-- Insert default roles
INSERT INTO roles (name, description) VALUES
    ('ADMIN', 'System administrator with full access'),
    ('DOCTOR', 'Medical doctor with patient and appointment access'),
    ('PATIENT', 'Patient with appointment access'),
    ('RECEPTIONIST', 'Receptionist with patient and appointment management access')
ON CONFLICT (name) DO NOTHING;

-- Insert default permissions
INSERT INTO permissions (name, description) VALUES
    ('READ_PATIENT', 'Read patient information'),
    ('WRITE_PATIENT', 'Create and update patient information'),
    ('READ_DOCTOR', 'Read doctor information'),
    ('WRITE_DOCTOR', 'Create and update doctor information'),
    ('READ_APPOINTMENT', 'Read appointment information'),
    ('WRITE_APPOINTMENT', 'Create, update, cancel appointments'),
    ('MANAGE_USERS', 'Manage user accounts'),
    ('MANAGE_ROLES', 'Manage roles and permissions'),
    ('VIEW_AUDIT', 'View audit logs')
ON CONFLICT (name) DO NOTHING;

-- Assign permissions to roles
-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- DOCTOR permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'DOCTOR' AND p.name IN ('READ_PATIENT', 'READ_DOCTOR', 'READ_APPOINTMENT', 'WRITE_APPOINTMENT')
ON CONFLICT DO NOTHING;

-- PATIENT permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'PATIENT' AND p.name IN ('READ_APPOINTMENT', 'WRITE_APPOINTMENT')
ON CONFLICT DO NOTHING;

-- RECEPTIONIST permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'RECEPTIONIST' AND p.name IN ('READ_PATIENT', 'WRITE_PATIENT', 'READ_DOCTOR', 'READ_APPOINTMENT', 'WRITE_APPOINTMENT')
ON CONFLICT DO NOTHING;