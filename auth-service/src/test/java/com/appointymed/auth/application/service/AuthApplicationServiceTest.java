package com.appointymed.auth.application.service;

import com.appointymed.auth.application.dto.*;
import com.appointymed.auth.application.mapper.UserMapper;
import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;
import com.appointymed.auth.domain.usecase.*;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.exception.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;

    @Mock
    private AuthenticateUserUseCase authenticateUserUseCase;

    @Mock
    private RefreshAccessTokenUseCase refreshAccessTokenUseCase;

    @Mock
    private LogoutUseCase logoutUseCase;

    @Mock
    private ForgotPasswordUseCase forgotPasswordUseCase;

    @Mock
    private ResetPasswordUseCase resetPasswordUseCase;

    @Mock
    private ChangePasswordUseCase changePasswordUseCase;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private AuthApplicationService authApplicationService;

    @BeforeEach
    void setUp() {
        authApplicationService = new AuthApplicationService(
                registerUserUseCase,
                authenticateUserUseCase,
                refreshAccessTokenUseCase,
                logoutUseCase,
                forgotPasswordUseCase,
                resetPasswordUseCase,
                changePasswordUseCase,
                userRepository,
                userMapper
        );
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        RegisterRequest request = new RegisterRequest(
                "test@example.com", "SecurePass123!", "John Doe", "+1234567890"
        );

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(Email.of("test@example.com"))
                .password(Password.ofHashed("hashed"))
                .fullName("John Doe")
                .status(UserStatus.PENDING_VERIFICATION)
                .roles(Set.of(Role.PATIENT))
                .build();

        UserDto userDto = new UserDto(
                user.getId(), "test@example.com", "John Doe", "+1234567890",
                "PENDING_VERIFICATION", false, List.of("PATIENT"),
                user.getCreatedAt(), user.getUpdatedAt()
        );

        when(registerUserUseCase.execute(any())).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        // When
        UserDto result = authApplicationService.register(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo("test@example.com");
        assertThat(result.fullName()).isEqualTo("John Doe");
        verify(registerUserUseCase).execute(any());
        verify(userMapper).toDto(user);
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        LoginRequest request = new LoginRequest("test@example.com", "SecurePass123!");
        UUID userId = UUID.randomUUID();

        AuthenticateUserUseCase.AuthenticationResult authResult = new AuthenticateUserUseCase.AuthenticationResult(
                "access-token", "refresh-token", userId, "test@example.com", "John Doe", List.of("PATIENT")
        );

        when(authenticateUserUseCase.execute(any())).thenReturn(authResult);

        // When
        LoginResponse result = authApplicationService.login(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.userId()).isEqualTo(userId);
        verify(authenticateUserUseCase).execute(any());
    }

    @Test
    void shouldRefreshTokenSuccessfully() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");
        RefreshAccessTokenUseCase.RefreshTokenResult refreshResult = new RefreshAccessTokenUseCase.RefreshTokenResult(
                "new-access-token", "new-refresh-token"
        );

        when(refreshAccessTokenUseCase.execute("refresh-token")).thenReturn(refreshResult);

        // When
        RefreshTokenResponse result = authApplicationService.refreshToken(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
    }

    @Test
    void shouldLogoutSuccessfully() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token");

        // When
        authApplicationService.logout(request.refreshToken());

        // Then
        verify(logoutUseCase).execute("refresh-token");
    }

    @Test
    void shouldGetUserByIdSuccessfully() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email(Email.of("test@example.com"))
                .fullName("John Doe")
                .roles(Set.of(Role.PATIENT))
                .build();

        UserDto userDto = new UserDto(
                userId, "test@example.com", "John Doe", null,
                "ACTIVE", true, List.of("PATIENT"),
                user.getCreatedAt(), user.getUpdatedAt()
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        // When
        UserDto result = authApplicationService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(userId);
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authApplicationService.getUserById(userId))
                .isInstanceOf(UserNotFoundException.class);
    }
}