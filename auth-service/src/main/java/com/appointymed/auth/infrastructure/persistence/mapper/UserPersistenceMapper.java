package com.appointymed.auth.infrastructure.persistence.mapper;

import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.PhoneNumber;
import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;
import com.appointymed.auth.infrastructure.persistence.entity.RoleJpaEntity;
import com.appointymed.auth.infrastructure.persistence.entity.UserJpaEntity;

import java.util.Set;
import java.util.stream.Collectors;

public class UserPersistenceMapper {
    public static User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;

        Set<Role> roles = entity.getRoles().stream()
                .map(roleEntity -> Role.fromName(roleEntity.getName()))
                .collect(Collectors.toSet());

        return User.builder()
                .id(entity.getId())
                .email(Email.of(entity.getEmail()))
                .password(Password.ofHashed(entity.getPasswordHash()))
                .fullName(entity.getFullName())
                .phone(entity.getPhone() != null ? PhoneNumber.of(entity.getPhone()) : null)
                .status(mapStatus(entity.getStatus()))
                .emailVerified(entity.isEmailVerified())
                .roles(roles)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static UserJpaEntity toEntity(User domain) {
        if (domain == null) return null;

        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(domain.getId());
        entity.setEmail(domain.getEmail().getValue());
        entity.setPasswordHash(domain.getPassword().getHashedValue());
        entity.setFullName(domain.getFullName());
        entity.setPhone(domain.getPhone() != null ? domain.getPhone().getValue() : null);
        entity.setStatus(mapStatus(domain.getStatus()));
        entity.setEmailVerified(domain.isEmailVerified());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());
        return entity;
    }

    public static void updateEntity(UserJpaEntity entity, User domain) {
        entity.setEmail(domain.getEmail().getValue());
        entity.setPasswordHash(domain.getPassword().getHashedValue());
        entity.setFullName(domain.getFullName());
        entity.setPhone(domain.getPhone() != null ? domain.getPhone().getValue() : null);
        entity.setStatus(mapStatus(domain.getStatus()));
        entity.setEmailVerified(domain.isEmailVerified());
        entity.setUpdatedAt(domain.getUpdatedAt());
    }

    private static UserJpaEntity.UserStatus mapStatus(UserStatus status) {
        return UserJpaEntity.UserStatus.valueOf(status.name());
    }

    private static UserStatus mapStatus(UserJpaEntity.UserStatus status) {
        return UserStatus.valueOf(status.name());
    }
}