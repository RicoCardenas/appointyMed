package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.exception.DomainException;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.model.DomainClock;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.UserRepository;

public final class RevokeRoleUseCase {
    private final UserRepository userRepository;
    private final DomainClock clock;

    public RevokeRoleUseCase(UserRepository u, DomainClock c) {
        userRepository = u;
        clock = c;
    }

    public User execute(DomainId id, String name) {
        User u = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        Role role = Role.fromName(name);
        if (!u.getRoles().contains(role))
            throw new DomainException("User does not have role: " + name, "ROLE_NOT_ASSIGNED");
        if (u.getRoles().size() == 1)
            throw new DomainException(
                    "Cannot remove the last role from user", "LAST_ROLE_CANNOT_BE_REMOVED");
        return userRepository.save(
                User.builder()
                        .id(u.getId())
                        .email(u.getEmail())
                        .password(u.getPassword())
                        .fullName(u.getFullName())
                        .phone(u.getPhone())
                        .status(u.getStatus())
                        .emailVerified(u.isEmailVerified())
                        .roles(u.getRoles().remove(role))
                        .createdAt(u.getCreatedAt())
                        .updatedAt(clock.now())
                        .build());
    }
}
