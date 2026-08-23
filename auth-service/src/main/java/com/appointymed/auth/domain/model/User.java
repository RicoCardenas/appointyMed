package com.appointymed.auth.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public class User {
    private final UUID id;
    private final Email email;
    private final Password password;
    private final String fullName;
    private final PhoneNumber phone;
    private final UserStatus status;
    private final boolean emailVerified;
    private final Set<Role> roles;
    private final Instant createdAt;
    private final Instant updatedAt;

    private User(Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.password = builder.password;
        this.fullName = builder.fullName;
        this.phone = builder.phone;
        this.status = builder.status;
        this.emailVerified = builder.emailVerified;
        this.roles = builder.roles;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public String getFullName() {
        return fullName;
    }

    public PhoneNumber getPhone() {
        return phone;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public boolean hasPermission(Permission permission) {
        return roles.stream().anyMatch(role -> role.hasPermission(permission));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private Email email;
        private Password password;
        private String fullName;
        private PhoneNumber phone;
        private UserStatus status = UserStatus.ACTIVE;
        private boolean emailVerified = false;
        private Set<Role> roles = Set.of();
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder email(Email email) {
            this.email = email;
            return this;
        }

        public Builder password(Password password) {
            this.password = password;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder phone(PhoneNumber phone) {
            this.phone = phone;
            return this;
        }

        public Builder status(UserStatus status) {
            this.status = status;
            return this;
        }

        public Builder emailVerified(boolean emailVerified) {
            this.emailVerified = emailVerified;
            return this;
        }

        public Builder roles(Set<Role> roles) {
            this.roles = roles;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(Instant updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }
}