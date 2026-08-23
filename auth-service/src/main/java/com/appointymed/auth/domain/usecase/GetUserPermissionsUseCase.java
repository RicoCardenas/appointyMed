package com.appointymed.auth.domain.usecase;
import org.springframework.stereotype.Component;

import com.appointymed.auth.domain.model.Permission;
import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.exception.UserNotFoundException;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class GetUserPermissionsUseCase {
    private final UserRepository userRepository;

    public GetUserPermissionsUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Set<Permission> execute(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .collect(Collectors.toSet());
    }
}