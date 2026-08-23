package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.exception.DomainException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class ForgotPasswordUseCase {
    private final UserRepository userRepository;
    private final PasswordResetTokenProvider tokenProvider;
    private final NotificationEventPublisher notificationPublisher;

    public ForgotPasswordUseCase(UserRepository userRepository,
                                 PasswordResetTokenProvider tokenProvider,
                                 NotificationEventPublisher notificationPublisher) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.notificationPublisher = notificationPublisher;
    }

    public void execute(String email) {
        Email emailVO = Email.of(email);
        User user = userRepository.findByEmail(emailVO)
                .orElseThrow(() -> new UserNotFoundException(email));

        if (user.getStatus() != com.appointymed.auth.domain.model.UserStatus.ACTIVE) {
            throw new DomainException("User account is not active", "ACCOUNT_NOT_ACTIVE");
        }

        String resetToken = tokenProvider.generateToken();
        Instant expiry = tokenProvider.getExpiry();

        notificationPublisher.publishPasswordResetRequested(
                user.getId(),
                user.getEmail().getValue(),
                user.getFullName(),
                resetToken,
                expiry
        );
    }

    public interface PasswordResetTokenProvider {
        String generateToken();
        Instant getExpiry();
        boolean validateToken(String token);
        UUID getUserIdFromToken(String token);
    }

    public interface NotificationEventPublisher {
        void publishPasswordResetRequested(UUID userId, String email, String fullName, String resetToken, Instant expiry);
    }
}