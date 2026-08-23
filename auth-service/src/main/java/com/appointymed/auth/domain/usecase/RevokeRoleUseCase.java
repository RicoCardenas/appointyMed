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
public class RevokeRoleUseCase {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public RevokeRoleUseCase(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public User execute(UUID userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Role roleToRevoke = Role.fromName(roleName);

        if (!user.getRoles().contains(roleToRevoke)) {
            throw new DomainException("User does not have role: " + roleName, "ROLE_NOT_ASSIGNED");
        }

        if (user.getRoles().size() == 1) {
            throw new DomainException("Cannot remove the last role from user", "LAST_ROLE_CANNOT_BE_REMOVED");
        }

        Set<Role> updatedRoles = user.getRoles().stream()
                .filter(role -> role != roleToRevoke)
                .collect(Collectors.toSet());

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