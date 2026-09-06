package com.appointymed.auth.domain.model;

public final class User {
    private final DomainId id;
    private final Email email;
    private final Password password;
    private final String fullName;
    private final PhoneNumber phone;
    private final UserStatus status;
    private final boolean emailVerified;
    private final DomainSet<Role> roles;
    private final DomainInstant createdAt, updatedAt;

    private User(Builder b) {
        id = b.id;
        email = b.email;
        password = b.password;
        fullName = b.fullName;
        phone = b.phone;
        status = b.status;
        emailVerified = b.emailVerified;
        roles = b.roles;
        createdAt = b.createdAt;
        updatedAt = b.updatedAt;
    }

    public DomainId getId() {
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

    public DomainSet<Role> getRoles() {
        return roles;
    }

    public DomainInstant getCreatedAt() {
        return createdAt;
    }

    public DomainInstant getUpdatedAt() {
        return updatedAt;
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public boolean hasPermission(Permission permission) {
        return roles.any(
                new DomainSet.DomainPredicate<Role>() {
                    public boolean test(Role role) {
                        return role.hasPermission(permission);
                    }
                });
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private DomainId id;
        private Email email;
        private Password password;
        private String fullName;
        private PhoneNumber phone;
        private UserStatus status = UserStatus.ACTIVE;
        private boolean emailVerified;
        private DomainSet<Role> roles = DomainSet.empty();
        private DomainInstant createdAt, updatedAt;

        public Builder id(DomainId v) {
            id = v;
            return this;
        }

        public Builder email(Email v) {
            email = v;
            return this;
        }

        public Builder password(Password v) {
            password = v;
            return this;
        }

        public Builder fullName(String v) {
            fullName = v;
            return this;
        }

        public Builder phone(PhoneNumber v) {
            phone = v;
            return this;
        }

        public Builder status(UserStatus v) {
            status = v;
            return this;
        }

        public Builder emailVerified(boolean v) {
            emailVerified = v;
            return this;
        }

        public Builder roles(DomainSet<Role> v) {
            roles = v == null ? DomainSet.empty() : v;
            return this;
        }

        public Builder createdAt(DomainInstant v) {
            createdAt = v;
            return this;
        }

        public Builder updatedAt(DomainInstant v) {
            updatedAt = v;
            return this;
        }

        public User build() {
            if (email == null || password == null || fullName == null || fullName.trim().isEmpty())
                throw new IllegalArgumentException("User identity and credentials are required");
            if (roles.size() == 0) {
                throw new IllegalArgumentException("User must have at least one role");
            }
            return new User(this);
        }
    }
}
