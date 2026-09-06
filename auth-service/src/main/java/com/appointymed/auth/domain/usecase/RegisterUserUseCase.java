package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.exception.DomainException;
import com.appointymed.auth.domain.exception.UserAlreadyExistsException;
import com.appointymed.auth.domain.model.DomainSet;
import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.PhoneNumber;
import com.appointymed.auth.domain.model.Role;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.model.UserStatus;
import com.appointymed.auth.domain.repository.RoleRepository;
import com.appointymed.auth.domain.repository.UserRepository;

public final class RegisterUserUseCase {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final Password.PasswordEncoder passwordEncoder;

    public RegisterUserUseCase(
            UserRepository userRepository,
            RoleRepository roleRepository,
            Password.PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User execute(RegisterUserCommand command) {
        Email email = Email.of(command.email());
        if (userRepository.existsByEmail(email)) throw new UserAlreadyExistsException(email.getValue());
        Password raw = Password.ofRaw(command.password());
        Password password = Password.ofHashed(passwordEncoder.encode(raw.getHashedValue()));
        Role role = roleRepository.findByName(Role.CLIENT.name()).orElse(null);
        if (role == null)
            role =
                    roleRepository
                            .findByName(Role.PATIENT.name())
                            .orElseThrow(() -> new DomainException("Default role not found", "ROLE_NOT_FOUND"));
        return userRepository.save(
                User.builder()
                        .email(email)
                        .password(password)
                        .fullName(command.fullName())
                        .phone(PhoneNumber.of(command.phone()))
                        .status(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .roles(DomainSet.of(role))
                        .build());
    }

    public record RegisterUserCommand(String email, String password, String fullName, String phone) {}
}
