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
import com.appointymed.auth.domain.exception.DomainException;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Password.PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(UserRepository userRepository,
                               RoleRepository roleRepository,
                               Password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(RegisterUserCommand command) {
        Email email = Email.of(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(email.getValue());
        }

        Password rawPassword = Password.ofRaw(command.password());
        String hashedPassword = passwordEncoder.encode(rawPassword.getHashedValue());
        Password password = Password.ofHashed(hashedPassword);

        PhoneNumber phone = command.phone() != null ? PhoneNumber.of(command.phone()) : null;

        Role defaultRole = roleRepository.findByName(Role.PATIENT.name())
                .orElseThrow(() -> new DomainException("Default role not found", "ROLE_NOT_FOUND"));

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(password)
                .fullName(command.fullName())
                .phone(phone)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .roles(Set.of(defaultRole))
                .build();

        return userRepository.save(user);
    }

    public record RegisterUserCommand(
            String email,
            String password,
            String fullName,
            String phone
    ) {}
}