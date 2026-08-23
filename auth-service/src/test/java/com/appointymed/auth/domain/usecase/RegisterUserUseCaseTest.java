package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.PhoneNumber;
import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;
import com.appointymed.auth.domain.repository.RoleRepository;
import com.appointymed.auth.domain.repository.UserRepository;
import com.appointymed.auth.domain.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private Password.PasswordEncoder passwordEncoder;

    private RegisterUserUseCase registerUserUseCase;

    @BeforeEach
    void setUp() {
        registerUserUseCase = new RegisterUserUseCase(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        // Given
        String email = "test@example.com";
        String password = "SecurePass123!";
        String fullName = "John Doe";
        String phone = "+1234567890";

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName(Role.CLIENT.name())).thenReturn(Optional.of(Role.CLIENT));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = registerUserUseCase.execute(
                new RegisterUserUseCase.RegisterUserCommand(email, password, fullName, phone)
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail().getValue()).isEqualTo(email);
        assertThat(result.getFullName()).isEqualTo(fullName);
        assertThat(result.getPhone().getValue()).isEqualTo(phone);
        assertThat(result.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(result.isEmailVerified()).isTrue();
        assertThat(result.getRoles()).containsExactly(Role.CLIENT);
        assertThat(result.getPassword().getHashedValue()).isEqualTo("hashedPassword");

        verify(userRepository).existsByEmail(any(Email.class));
        verify(passwordEncoder).encode(password);
        verify(roleRepository).findByName(Role.CLIENT.name());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        // Given
        String email = "test@example.com";
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> registerUserUseCase.execute(
                new RegisterUserUseCase.RegisterUserCommand(email, "SecurePass123!", "John Doe", null)
        )).isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining(email);

        verify(userRepository).existsByEmail(any(Email.class));
        verifyNoInteractions(passwordEncoder, roleRepository);
    }

    @Test
    void shouldRegisterUserWithoutPhone() {
        // Given
        String email = "test@example.com";
        String password = "SecurePass123!";
        String fullName = "John Doe";

        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(roleRepository.findByName(Role.CLIENT.name())).thenReturn(Optional.of(Role.CLIENT));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = registerUserUseCase.execute(
                new RegisterUserUseCase.RegisterUserCommand(email, password, fullName, null)
        );

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPhone()).isNull();
    }
}