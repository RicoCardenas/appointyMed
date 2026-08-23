-- Test data for H2 database - H2 compatible syntax
-- Insert default roles
INSERT INTO roles (id, name, description) VALUES
    (RANDOM_UUID(), 'ADMIN', 'System administrator with full access'),
    (RANDOM_UUID(), 'DEVELOPER', 'Developer with restricted user-management access'),
    (RANDOM_UUID(), 'DOCTOR', 'Medical doctor with patient and appointment access'),
    (RANDOM_UUID(), 'PATIENT', 'Patient with appointment access'),
    (RANDOM_UUID(), 'CLIENT', 'Client with appointment access'),
    (RANDOM_UUID(), 'RECEPTIONIST', 'Receptionist with patient and appointment management access');

-- Insert default permissions
INSERT INTO permissions (id, name, description) VALUES
    (RANDOM_UUID(), 'READ_PATIENT', 'Read patient information'),
    (RANDOM_UUID(), 'WRITE_PATIENT', 'Create and update patient information'),
    (RANDOM_UUID(), 'READ_DOCTOR', 'Read doctor information'),
    (RANDOM_UUID(), 'WRITE_DOCTOR', 'Create and update doctor information'),
    (RANDOM_UUID(), 'READ_APPOINTMENT', 'Read appointment information'),
    (RANDOM_UUID(), 'WRITE_APPOINTMENT', 'Create, update, cancel appointments'),
    (RANDOM_UUID(), 'MANAGE_USERS', 'Manage user accounts'),
    (RANDOM_UUID(), 'MANAGE_ROLES', 'Manage roles and permissions'),
    (RANDOM_UUID(), 'VIEW_AUDIT', 'View audit logs');

-- Assign permissions to roles
-- ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'ADMIN';

-- DOCTOR permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'DOCTOR' AND p.name IN ('READ_PATIENT', 'READ_DOCTOR', 'READ_APPOINTMENT', 'WRITE_APPOINTMENT');

-- PATIENT permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'PATIENT' AND p.name IN ('READ_APPOINTMENT', 'WRITE_APPOINTMENT');

-- RECEPTIONIST permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id FROM roles r, permissions p
WHERE r.name = 'RECEPTIONIST' AND p.name IN ('READ_PATIENT', 'WRITE_PATIENT', 'READ_DOCTOR', 'READ_APPOINTMENT', 'WRITE_APPOINTMENT');