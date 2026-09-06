package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.exception.UserAlreadyExistsException;
import com.appointymed.auth.domain.model.*;
import com.appointymed.auth.domain.repository.RoleRepository;
import com.appointymed.auth.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {
    @Mock UserRepository userRepository;
    @Mock RoleRepository roleRepository;
    @Mock Password.PasswordEncoder passwordEncoder;

    @Test
    void registersUserWithDefaultRole() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(roleRepository.findByName(Role.CLIENT.name())).thenReturn(DomainOptional.of(Role.CLIENT));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = new RegisterUserUseCase(userRepository, roleRepository, passwordEncoder).execute(
                new RegisterUserUseCase.RegisterUserCommand("test@example.com", "SecurePass123!", "John Doe", "+123"));

        assertThat(result.getEmail().getValue()).isEqualTo("test@example.com");
        assertThat(result.getRoles().size()).isEqualTo(1);
        assertThat(result.getRoles().get(0)).isEqualTo(Role.CLIENT);
        assertThat(result.getPassword().getHashedValue()).isEqualTo("hashed");
    }

    @Test
    void rejectsDuplicateEmail() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);
        assertThatThrownBy(() -> new RegisterUserUseCase(userRepository, roleRepository, passwordEncoder).execute(
                new RegisterUserUseCase.RegisterUserCommand("test@example.com", "SecurePass123!", "John Doe", null)))
                .isInstanceOf(UserAlreadyExistsException.class);
        verifyNoInteractions(passwordEncoder, roleRepository);
    }
}
