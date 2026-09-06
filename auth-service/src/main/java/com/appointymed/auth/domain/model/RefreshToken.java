package com.appointymed.auth.domain.model;

public final class RefreshToken {
    private final DomainId id, userId;
    private final String token;
    private final DomainInstant expiresAt, createdAt;
    private boolean revoked;

    private RefreshToken(Builder b) {
        id = b.id;
        token = b.token;
        userId = b.userId;
        expiresAt = b.expiresAt;
        createdAt = b.createdAt;
        revoked = b.revoked;
    }

    public DomainId getId() {
        return id;
    }

    public String getToken() {
        return token;
    }

    public DomainId getUserId() {
        return userId;
    }

    public DomainInstant getExpiresAt() {
        return expiresAt;
    }

    public DomainInstant getCreatedAt() {
        return createdAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void revoke() {
        revoked = true;
    }

    public boolean isValid(DomainInstant now) {
        return !revoked && expiresAt != null && now.isBefore(expiresAt);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private DomainId id, userId;
        private String token;
        private DomainInstant expiresAt, createdAt;
        private boolean revoked;

        public Builder id(DomainId v) {
            id = v;
            return this;
        }

        public Builder token(String v) {
            token = v;
            return this;
        }

        public Builder userId(DomainId v) {
            userId = v;
            return this;
        }

        public Builder expiresAt(DomainInstant v) {
            expiresAt = v;
            return this;
        }

        public Builder createdAt(DomainInstant v) {
            createdAt = v;
            return this;
        }

        public Builder revoked(boolean v) {
            revoked = v;
            return this;
        }

        public RefreshToken build() {
            if (token == null || token.isEmpty())
                throw new IllegalArgumentException("Refresh token is required");
            if (userId == null) {
                throw new IllegalArgumentException("Refresh token user is required");
            }
            if (expiresAt == null) {
                throw new IllegalArgumentException("Refresh token expiry is required");
            }
            return new RefreshToken(this);
        }
    }
}
