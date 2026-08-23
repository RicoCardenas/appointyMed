package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.RefreshToken;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;
import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.repository.RefreshTokenRepository;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.exception.InvalidCredentialsException;
import com.appointymed.auth.domain.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private Password.PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private AuthenticateUserUseCase authenticateUserUseCase;

    @BeforeEach
    void setUp() {
        authenticateUserUseCase = new AuthenticateUserUseCase(
                userRepository, refreshTokenRepository, passwordEncoder, jwtTokenProvider);
    }

    @Test
    void shouldAuthenticateUserSuccessfully() {
        // Given
        String email = "test@example.com";
        String password = "SecurePass123!";
        UUID userId = UUID.randomUUID();

        User user = User.builder()
                .id(userId)
                .email(Email.of(email))
                .password(Password.ofHashed("$2a$12$hashedPasswordValue"))
                .fullName("John Doe")
                .status(UserStatus.ACTIVE)
                .roles(Set.of(Role.PATIENT))
                .build();

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access-token");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refresh-token");
        when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(Instant.now().plusSeconds(604800));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        AuthenticateUserUseCase.AuthenticationResult result = authenticateUserUseCase.execute(
                new AuthenticateUserUseCase.AuthenticateUserCommand(email, password)
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.email()).isEqualTo(email);
        assertThat(result.fullName()).isEqualTo("John Doe");
        assertThat(result.roles()).containsExactly("PATIENT");

        verify(userRepository).findByEmail(any(Email.class));
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(jwtTokenProvider).generateAccessToken(user);
        verify(jwtTokenProvider).generateRefreshToken();
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticateUserUseCase.execute(
                new AuthenticateUserUseCase.AuthenticateUserCommand("test@example.com", "SecurePass123!")
        )).isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository).findByEmail(any(Email.class));
    }

    @Test
    void shouldThrowExceptionWhenInvalidPassword() {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(Email.of("test@example.com"))
                .password(Password.ofHashed("ValidPass123!"))
                .status(UserStatus.ACTIVE)
                .build();

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authenticateUserUseCase.execute(
                new AuthenticateUserUseCase.AuthenticateUserCommand("test@example.com", "WrongPassword123!")
        )).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void shouldThrowExceptionWhenUserNotActive() {
        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .email(Email.of("test@example.com"))
                .password(Password.ofHashed("hashedPassword"))
                .status(UserStatus.SUSPENDED)
                .build();

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> authenticateUserUseCase.execute(
                new AuthenticateUserUseCase.AuthenticateUserCommand("test@example.com", "SecurePass123!")
        )).isInstanceOf(DomainException.class)
                .hasMessageContaining("not active");
    }
}