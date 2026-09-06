package com.appointymed.auth.infrastructure.persistence.mapper;

import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.PhoneNumber;
import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.appointymed.auth.infrastructure.persistence.entity.UserJpaEntity;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import java.time.Instant;

public class UserPersistenceMapper {
    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        com.appointymed.auth.domain.model.DomainSet<Role> roles = com.appointymed.auth.domain.model.DomainSet.empty();
        for (RoleJpaEntity roleEntity : entity.getRoles()) roles = roles.add(Role.fromName(roleEntity.getName()));

        return User.builder()
                .id(toDomainId(entity.getId()))
                .email(Email.of(entity.getEmail()))
                .password(Password.ofHashed(entity.getPasswordHash()))
                .fullName(entity.getFullName())
                .phone(entity.getPhone() != null ? PhoneNumber.of(entity.getPhone()) : null)
                .status(mapStatus(entity.getStatus()))
                .emailVerified(entity.isEmailVerified())
                .roles(roles)
                .createdAt(toDomainInstant(entity.getCreatedAt()))
                .updatedAt(toDomainInstant(entity.getUpdatedAt()))
                .build();
    }

    public static UserJpaEntity toEntity(User domain) {
        if (domain == null) return null;

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(toUuid(domain.getId()));
        entity.setEmail(domain.getEmail().getValue());
        entity.setPasswordHash(domain.getPassword().getHashedValue());
        entity.setFullName(domain.getFullName());
        entity.setPhone(domain.getPhone() != null ? domain.getPhone().getValue() : null);
        entity.setStatus(mapStatus(domain.getStatus()));
        entity.setEmailVerified(domain.isEmailVerified());
        entity.setCreatedAt(toInstant(domain.getCreatedAt()));
        entity.setUpdatedAt(toInstant(domain.getUpdatedAt()));
        return entity;
    }

    public static void updateEntity(UserJpaEntity entity, User domain) {
        entity.setEmail(domain.getEmail().getValue());
        entity.setPasswordHash(domain.getPassword().getHashedValue());
        entity.setFullName(domain.getFullName());
        entity.setPhone(domain.getPhone() != null ? domain.getPhone().getValue() : null);
        entity.setStatus(mapStatus(domain.getStatus()));
        entity.setEmailVerified(domain.isEmailVerified());
        entity.setUpdatedAt(toInstant(domain.getUpdatedAt()));
    }

    private static UserJpaEntity.UserStatus mapStatus(UserStatus status) {
        return UserJpaEntity.UserStatus.valueOf(status.name());
    }

    private static UserStatus mapStatus(UserJpaEntity.UserStatus status) {
        return UserStatus.valueOf(status.name());
    }

    private static DomainId toDomainId(UUID value) { return value == null ? null : DomainId.of(value.toString()); }
    private static UUID toUuid(DomainId value) { return value == null ? null : UUID.fromString(value.value()); }
    private static DomainInstant toDomainInstant(Instant value) { return value == null ? null : DomainInstant.ofEpochMillis(value.toEpochMilli()); }
    private static Instant toInstant(DomainInstant value) { return value == null ? null : Instant.ofEpochMilli(value.toEpochMillis()); }
}
