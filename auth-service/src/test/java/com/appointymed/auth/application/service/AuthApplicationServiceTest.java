package com.appointymed.auth.application.service;

import com.appointymed.auth.application.dto.*;
import com.appointymed.auth.application.mapper.UserMapper;
import com.appointymed.auth.domain.model.*;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.usecase.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthApplicationServiceTest {
    @Mock RegisterUserUseCase register;
    @Mock AuthenticateUserUseCase authenticate;
    @Mock RefreshAccessTokenUseCase refresh;
    @Mock LogoutUseCase logout;
    @Mock ForgotPasswordUseCase forgot;
    @Mock ResetPasswordUseCase reset;
    @Mock ChangePasswordUseCase change;
    @Mock UserRepository users;
    @Mock UserMapper mapper;

    private AuthApplicationService service() { return new AuthApplicationService(register, authenticate, refresh, logout, forgot, reset, change, users, mapper); }

    @Test
    void registers() {
        User user = User.builder().id(DomainId.of("11111111-1111-1111-1111-111111111111")).email(Email.of("test@example.com")).password(Password.ofHashed("hash")).fullName("John Doe").roles(DomainSet.of(Role.PATIENT)).build();
        UserDto dto = new UserDto(java.util.UUID.fromString(user.getId().value()), "test@example.com", "John Doe", null, "ACTIVE", true, List.of("PATIENT"), null, null);
        when(register.execute(any())).thenReturn(user); when(mapper.toDto(user)).thenReturn(dto);
        assertThat(service().register(new RegisterRequest("test@example.com", "SecurePass123!", "John Doe", null))).isEqualTo(dto);
    }

    @Test
    void logsInAndConvertsDomainValues() {
        DomainId id = DomainId.of("11111111-1111-1111-1111-111111111111");
        DomainList<String> roles = new DomainList<>(); roles.add("PATIENT");
        when(authenticate.execute(any())).thenReturn(new AuthenticateUserUseCase.AuthenticationResult("access", "refresh", id, "test@example.com", "John Doe", roles));
        LoginResponse result = service().login(new LoginRequest("test@example.com", "SecurePass123!"));
        assertThat(result.userId()).isEqualTo(java.util.UUID.fromString(id.value()));
        assertThat(result.roles()).containsExactly("PATIENT");
    }

    @Test
    void refreshesAndLogsOut() {
        when(refresh.execute("refresh")).thenReturn(new RefreshAccessTokenUseCase.RefreshTokenResult("access", "new-refresh"));
        RefreshTokenResponse response = service().refreshToken(new RefreshTokenRequest("refresh"));
        assertThat(response.accessToken()).isEqualTo("access");
        service().logout("refresh");
        verify(logout).execute("refresh");
    }
}
