package com.appointymed.auth.domain.model;

import java.util.Set;

public enum Role {
    ADMIN(Set.of(
            Permission.MANAGE_USERS,
            Permission.MANAGE_ROLES,
            Permission.VIEW_AUDIT,
            Permission.READ_PATIENT,
            Permission.WRITE_PATIENT,
            Permission.READ_DOCTOR,
            Permission.WRITE_DOCTOR,
            Permission.READ_APPOINTMENT,
            Permission.WRITE_APPOINTMENT
    )),
    DEVELOPER(Set.of(
            Permission.MANAGE_USERS,
            Permission.VIEW_AUDIT,
            Permission.READ_PATIENT,
            Permission.READ_DOCTOR,
            Permission.READ_APPOINTMENT
    )),
    DOCTOR(Set.of(
            Permission.READ_PATIENT,
            Permission.READ_DOCTOR,
            Permission.READ_APPOINTMENT,
            Permission.WRITE_APPOINTMENT
    )),
    PATIENT(Set.of(
            Permission.READ_APPOINTMENT,
            Permission.WRITE_APPOINTMENT
    )),
    CLIENT(Set.of(
            Permission.READ_APPOINTMENT,
            Permission.WRITE_APPOINTMENT
    )),
    RECEPTIONIST(Set.of(
            Permission.READ_PATIENT,
            Permission.WRITE_PATIENT,
            Permission.READ_DOCTOR,
            Permission.READ_APPOINTMENT,
            Permission.WRITE_APPOINTMENT
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public boolean hasPermission(Permission permission) {
        return permissions.contains(permission);
    }

    public static Role fromName(String name) {
        for (Role role : values()) {
            if (role.name().equalsIgnoreCase(name)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + name);
    }
}