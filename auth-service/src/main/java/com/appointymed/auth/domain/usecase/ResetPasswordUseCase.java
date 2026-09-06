package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.exception.TokenExpiredException;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.model.DomainClock;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.UserRepository;

public final class ResetPasswordUseCase {
    private final UserRepository userRepository;
    private final ForgotPasswordUseCase.PasswordResetTokenProvider tokenProvider;
    private final Password.PasswordEncoder passwordEncoder;
    private final DomainClock clock;

    public ResetPasswordUseCase(
            UserRepository u,
            ForgotPasswordUseCase.PasswordResetTokenProvider t,
            Password.PasswordEncoder p,
            DomainClock c) {
        userRepository = u;
        tokenProvider = t;
        passwordEncoder = p;
        clock = c;
    }

    public void execute(ResetPasswordCommand command) {
        if (!tokenProvider.validateToken(command.token()))
            throw new TokenExpiredException("Invalid or expired reset token");
        var id = tokenProvider.getUserIdFromToken(command.token());
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
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

    public record ResetPasswordCommand(String token, String newPassword) {}
}
