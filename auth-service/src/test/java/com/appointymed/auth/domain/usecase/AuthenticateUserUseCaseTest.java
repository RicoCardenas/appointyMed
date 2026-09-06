package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.exception.InvalidCredentialsException;
import com.appointymed.auth.domain.model.*;
import com.appointymed.auth.domain.repository.RefreshTokenRepository;
import com.appointymed.auth.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticateUserUseCaseTest {
    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock Password.PasswordEncoder passwordEncoder;
    @Mock JwtTokenProvider jwtTokenProvider;

    @Test
    void authenticatesActiveUser() {
        DomainId id = DomainId.of("11111111-1111-1111-1111-111111111111");
        User user = User.builder().id(id).email(Email.of("test@example.com"))
                .password(Password.ofHashed("hash")).fullName("John Doe").status(UserStatus.ACTIVE)
                .roles(DomainSet.of(Role.PATIENT)).build();
        when(userRepository.findByEmail(any(Email.class))).thenReturn(DomainOptional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access");
        when(jwtTokenProvider.generateRefreshToken()).thenReturn("refresh");
        when(jwtTokenProvider.getRefreshTokenExpiry()).thenReturn(DomainInstant.ofEpochMillis(System.currentTimeMillis() + 1000));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = new AuthenticateUserUseCase(userRepository, refreshTokenRepository, passwordEncoder, jwtTokenProvider).execute(
                new AuthenticateUserUseCase.AuthenticateUserCommand("test@example.com", "password"));

        assertThat(result.accessToken()).isEqualTo("access");
        assertThat(result.userId()).isEqualTo(id);
        assertThat(result.roles().get(0)).isEqualTo("PATIENT");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void rejectsMissingUser() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(DomainOptional.empty());
        assertThatThrownBy(() -> new AuthenticateUserUseCase(userRepository, refreshTokenRepository, passwordEncoder, jwtTokenProvider).execute(
                new AuthenticateUserUseCase.AuthenticateUserCommand("test@example.com", "password")))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
