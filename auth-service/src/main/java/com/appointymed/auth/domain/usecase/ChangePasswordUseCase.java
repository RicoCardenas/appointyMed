package com.appointymed.auth.domain.usecase;
import org.springframework.stereotype.Component;

import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.exception.InvalidCredentialsException;

import java.util.UUID;

@Component
public class ChangePasswordUseCase {
    private final UserRepository userRepository;
    private final Password.PasswordEncoder passwordEncoder;

    public ChangePasswordUseCase(UserRepository userRepository,
                                 Password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void execute(ChangePasswordCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        Password currentPassword = Password.ofRaw(command.currentPassword());
        if (!currentPassword.matches(command.currentPassword(), passwordEncoder)) {
            throw new InvalidCredentialsException();
        }

        Password newRawPassword = Password.ofRaw(command.newPassword());
        String hashedPassword = passwordEncoder.encode(newRawPassword.getHashedValue());
        Password newPassword = Password.ofHashed(hashedPassword);

        User updatedUser = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(newPassword)
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(java.time.Instant.now())
                .build();

        userRepository.save(updatedUser);
    }

    public record ChangePasswordCommand(UUID userId, String currentPassword, String newPassword) {}
}