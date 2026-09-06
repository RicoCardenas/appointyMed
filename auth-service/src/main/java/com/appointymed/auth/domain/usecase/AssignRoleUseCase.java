package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.model.DomainClock;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.UserRepository;

public final class AssignRoleUseCase {
    private final UserRepository userRepository;
    private final DomainClock clock;

    public AssignRoleUseCase(UserRepository u, DomainClock c) {
        userRepository = u;
        clock = c;
    }

    public User execute(DomainId id, String name) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
        return userRepository.save(copy(user, user.getRoles().add(Role.fromName(name))));
    }

    private User copy(User u, com.appointymed.auth.domain.model.DomainSet<Role> roles) {
        return User.builder()
                .id(u.getId())
                .email(u.getEmail())
                .password(u.getPassword())
                .fullName(u.getFullName())
                .phone(u.getPhone())
                .status(u.getStatus())
                .emailVerified(u.isEmailVerified())
                .roles(roles)
                .createdAt(u.getCreatedAt())
                .updatedAt(clock.now())
                .build();
    }
}
