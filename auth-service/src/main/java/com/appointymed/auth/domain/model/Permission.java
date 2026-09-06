package com.appointymed.auth.domain.model;

public enum Permission {
    READ_PATIENT("Read patient information"),
    WRITE_PATIENT("Create and update patient information"),
    READ_DOCTOR("Read doctor information"),
    WRITE_DOCTOR("Create and update doctor information"),
    READ_APPOINTMENT("Read appointment information"),
    WRITE_APPOINTMENT("Create, update, cancel appointments"),
    MANAGE_USERS("Manage user accounts"),
    MANAGE_ROLES("Manage roles and permissions"),
    VIEW_AUDIT("View audit logs");

    private final String description;

    Permission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
