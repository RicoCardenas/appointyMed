package com.appointymed.auth.domain.usecase;
import org.springframework.stereotype.Component;

import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.RoleRepository;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.exception.DomainException;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class AssignRoleUseCase {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AssignRoleUseCase(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User execute(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Role role = Role.fromName(roleName);

        Set<Role> updatedRoles = Set.copyOf(
                java.util.stream.Stream.concat(user.getRoles().stream(), java.util.stream.Stream.of(role))
                        .collect(Collectors.toSet())
        );

        User updatedUser = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .roles(updatedRoles)
                .createdAt(user.getCreatedAt())
                .updatedAt(java.time.Instant.now())
                .build();

        return userRepository.save(updatedUser);
    }
}