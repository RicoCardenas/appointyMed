package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.exception.DomainException;
import com.appointymed.auth.domain.exception.InvalidCredentialsException;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.DomainList;
import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.RefreshToken;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;
import com.appointymed.auth.domain.repository.RefreshTokenRepository;
import com.appointymed.auth.domain.repository.UserRepository;

public final class AuthenticateUserUseCase {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final Password.PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthenticateUserUseCase(
            UserRepository u, RefreshTokenRepository r, Password.PasswordEncoder p, JwtTokenProvider j) {
        userRepository = u;
        refreshTokenRepository = r;
        passwordEncoder = p;
        jwtTokenProvider = j;
    }

    public AuthenticationResult execute(AuthenticateUserCommand command) {
        User user =
                userRepository
                        .findByEmail(Email.of(command.email()))
                        .orElseThrow(InvalidCredentialsException::new);
        if (user.getStatus() != UserStatus.ACTIVE)
            throw new DomainException("User account is not active", "ACCOUNT_NOT_ACTIVE");
        if (!user.getPassword().matches(command.password(), passwordEncoder))
            throw new InvalidCredentialsException();
        String access = jwtTokenProvider.generateAccessToken(user),
                refresh = jwtTokenProvider.generateRefreshToken();
        DomainInstant expiry = jwtTokenProvider.getRefreshTokenExpiry();
        refreshTokenRepository.save(
                RefreshToken.builder().token(refresh).userId(user.getId()).expiresAt(expiry).build());
        DomainList<String> roles = new DomainList<>();
        for (int i = 0; i < user.getRoles().size(); i++) roles.add(user.getRoles().get(i).name());
        return new AuthenticationResult(
                access, refresh, user.getId(), user.getEmail().getValue(), user.getFullName(), roles);
    }

    public record AuthenticateUserCommand(String email, String password) {}

    public record AuthenticationResult(
            String accessToken,
            String refreshToken,
            com.appointymed.auth.domain.model.DomainId userId,
            String email,
            String fullName,
            DomainList<String> roles) {}
}
