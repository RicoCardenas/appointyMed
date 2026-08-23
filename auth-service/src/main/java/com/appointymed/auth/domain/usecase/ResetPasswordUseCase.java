package com.appointymed.auth.domain.usecase;
import org.springframework.stereotype.Component;

import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.exception.TokenExpiredException;
import com.appointymed.auth.domain.exception.DomainException;

import java.util.UUID;

@Component
public class ResetPasswordUseCase {
    private final UserRepository userRepository;
    private final ForgotPasswordUseCase.PasswordResetTokenProvider tokenProvider;
    private final Password.PasswordEncoder passwordEncoder;

    public ResetPasswordUseCase(UserRepository userRepository,
                                ForgotPasswordUseCase.PasswordResetTokenProvider tokenProvider,
                                Password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    public void execute(ResetPasswordCommand command) {
        if (!tokenProvider.validateToken(command.token())) {
            throw new TokenExpiredException("Invalid or expired reset token");
        }

        UUID userId = tokenProvider.getUserIdFromToken(command.token());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        Password rawPassword = Password.ofRaw(command.newPassword());
        String hashedPassword = passwordEncoder.encode(rawPassword.getHashedValue());
        Password password = Password.ofHashed(hashedPassword);

        User updatedUser = User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(password)
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

    public record ResetPasswordCommand(String token, String newPassword) {}
}