package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.exception.InvalidCredentialsException;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.model.DomainClock;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.UserRepository;

public final class ChangePasswordUseCase {
    private final UserRepository userRepository;
    private final Password.PasswordEncoder passwordEncoder;
    private final DomainClock clock;

    public ChangePasswordUseCase(UserRepository u, Password.PasswordEncoder p, DomainClock c) {
        userRepository = u;
        passwordEncoder = p;
        clock = c;
    }

    public void execute(ChangePasswordCommand command) {
        User user =
                userRepository
                        .findById(command.userId())
                        .orElseThrow(() -> new UserNotFoundException(command.userId()));
        if (!user.getPassword().matches(command.currentPassword(), passwordEncoder))
            throw new InvalidCredentialsException();
        Password raw = Password.ofRaw(command.newPassword());
        Password password = Password.ofHashed(passwordEncoder.encode(raw.getHashedValue()));
        userRepository.save(
                User.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .password(password)
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .status(user.getStatus())
                        .emailVerified(user.isEmailVerified())
                        .roles(user.getRoles())
                        .createdAt(user.getCreatedAt())
                        .updatedAt(clock.now())
                        .build());
    }

    public record ChangePasswordCommand(
            DomainId userId, String currentPassword, String newPassword) {}
}
