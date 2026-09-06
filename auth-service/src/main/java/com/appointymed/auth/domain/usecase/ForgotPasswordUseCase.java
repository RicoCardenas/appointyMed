package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.exception.DomainException;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;
import com.appointymed.auth.domain.repository.UserRepository;

public final class ForgotPasswordUseCase {
    private final UserRepository userRepository;
    private final PasswordResetTokenProvider tokenProvider;
    private final NotificationEventPublisher notificationPublisher;

    public ForgotPasswordUseCase(
            UserRepository u, PasswordResetTokenProvider t, NotificationEventPublisher n) {
        userRepository = u;
        tokenProvider = t;
        notificationPublisher = n;
    }

    public void execute(String value) {
        Email email = Email.of(value);
        User user =
                userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(value));
        if (user.getStatus() != UserStatus.ACTIVE)
            throw new DomainException("User account is not active", "ACCOUNT_NOT_ACTIVE");
        String token = tokenProvider.generateToken(user.getId());
        DomainInstant expiry = tokenProvider.getExpiry();
        notificationPublisher.publishPasswordResetRequested(
                user.getId(), user.getEmail().getValue(), user.getFullName(), token, expiry);
    }

    public interface PasswordResetTokenProvider {
        String generateToken(DomainId userId);

        DomainInstant getExpiry();

        boolean validateToken(String token);

        DomainId getUserIdFromToken(String token);
    }

    public interface NotificationEventPublisher {
        void publishPasswordResetRequested(
                DomainId userId, String email, String fullName, String resetToken, DomainInstant expiry);
    }
}
