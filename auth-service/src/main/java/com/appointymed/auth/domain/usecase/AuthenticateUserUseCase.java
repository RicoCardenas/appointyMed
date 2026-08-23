package com.appointymed.auth.domain.usecase;
import org.springframework.stereotype.Component;

import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.RefreshToken;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.repository.RefreshTokenRepository;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import com.appointymed.auth.domain.exception.InvalidCredentialsException;
import com.appointymed.auth.domain.exception.DomainException;

import java.time.Instant;
import java.util.UUID;

@Component
public class AuthenticateUserUseCase {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Password.PasswordEncoder passwordEncoder;
    private final com.appointymed.auth.domain.usecase.JwtTokenProvider jwtTokenProvider;

    public AuthenticateUserUseCase(UserRepository userRepository,
                                   RefreshTokenRepository refreshTokenRepository,
                                   Password.PasswordEncoder passwordEncoder,
                                   com.appointymed.auth.domain.usecase.JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public AuthenticationResult execute(AuthenticateUserCommand command) {
        Email email = Email.of(command.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException());

        if (user.getStatus() != com.appointymed.auth.domain.model.UserStatus.ACTIVE) {
            throw new DomainException("User account is not active", "ACCOUNT_NOT_ACTIVE");
        }

        if (!user.getPassword().matches(command.password(), passwordEncoder)) {
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshTokenValue = jwtTokenProvider.generateRefreshToken();
        Instant refreshTokenExpiry = jwtTokenProvider.getRefreshTokenExpiry();

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userId(user.getId())
                .expiresAt(refreshTokenExpiry)
                .build();

        refreshTokenRepository.save(refreshToken);

        return new AuthenticationResult(
                accessToken,
                refreshTokenValue,
                user.getId(),
                user.getEmail().getValue(),
                user.getFullName(),
                user.getRoles().stream().map(Enum::name).toList()
        );
    }

    public record AuthenticateUserCommand(String email, String password) {}

    public record AuthenticationResult(
            String accessToken,
            String refreshToken,
            UUID userId,
            String email,
            String fullName,
            java.util.List<String> roles
    ) {}

    }