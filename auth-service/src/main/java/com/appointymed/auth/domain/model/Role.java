package com.appointymed.auth.domain.model;

public enum Role {
    ADMIN(
            DomainSet.of(
                    Permission.MANAGE_USERS,
                    Permission.MANAGE_ROLES,
                    Permission.VIEW_AUDIT,
                    Permission.READ_PATIENT,
                    Permission.WRITE_PATIENT,
                    Permission.READ_DOCTOR,
                    Permission.WRITE_DOCTOR,
                    Permission.READ_APPOINTMENT,
                    Permission.WRITE_APPOINTMENT)),

    DEVELOPER(
            DomainSet.of(
                    Permission.MANAGE_USERS,
                    Permission.VIEW_AUDIT,
                    Permission.READ_PATIENT,
                    Permission.READ_DOCTOR,
                    Permission.READ_APPOINTMENT)),

    DOCTOR(
            DomainSet.of(
                    Permission.READ_PATIENT,
                    Permission.READ_DOCTOR,
                    Permission.READ_APPOINTMENT,
                    Permission.WRITE_APPOINTMENT)),

    PATIENT(DomainSet.of(Permission.READ_APPOINTMENT, Permission.WRITE_APPOINTMENT)),
    CLIENT(DomainSet.of(Permission.READ_APPOINTMENT, Permission.WRITE_APPOINTMENT)),

    RECEPTIONIST(
            DomainSet.of(
                    Permission.READ_PATIENT,
                    Permission.WRITE_PATIENT,
                    Permission.READ_DOCTOR,
                    Permission.READ_APPOINTMENT,
                    Permission.WRITE_APPOINTMENT));

    private final DomainSet<Permission> permissions;

    Role(DomainSet<Permission> permissions) {
        this.permissions = permissions;
    }

    public DomainSet<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public static Role fromName(String name) {
        if (name != null)
            for (Role role : values()) if (role.name().equalsIgnoreCase(name.trim())) return role;
        throw new IllegalArgumentException("Unknown role: " + name);
    }
}
