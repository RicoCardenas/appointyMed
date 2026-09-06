package com.appointymed.auth.application.service;

import com.appointymed.auth.application.dto.*;
import com.appointymed.auth.application.mapper.UserMapper;
import com.appointymed.auth.domain.model.Email;
import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.model.User;
import com.appointymed.auth.domain.usecase.*;
import com.appointymed.auth.domain.exception.*;
import com.appointymed.auth.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class AuthApplicationService {
    private final RegisterUserUseCase registerUserUseCase;
    private final AuthenticateUserUseCase authenticateUserUseCase;
    private final RefreshAccessTokenUseCase refreshAccessTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final ForgotPasswordUseCase forgotPasswordUseCase;
    private final ResetPasswordUseCase resetPasswordUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public AuthApplicationService(RegisterUserUseCase registerUserUseCase,
                                  AuthenticateUserUseCase authenticateUserUseCase,
                                  RefreshAccessTokenUseCase refreshAccessTokenUseCase,
                                  LogoutUseCase logoutUseCase,
                                  ForgotPasswordUseCase forgotPasswordUseCase,
                                  ResetPasswordUseCase resetPasswordUseCase,
                                  ChangePasswordUseCase changePasswordUseCase,
                                  UserRepository userRepository,
                                  UserMapper userMapper) {
        this.registerUserUseCase = registerUserUseCase;
        this.authenticateUserUseCase = authenticateUserUseCase;
        this.refreshAccessTokenUseCase = refreshAccessTokenUseCase;
        this.logoutUseCase = logoutUseCase;
        this.forgotPasswordUseCase = forgotPasswordUseCase;
        this.resetPasswordUseCase = resetPasswordUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public UserDto register(RegisterRequest request) {
        var command = new RegisterUserUseCase.RegisterUserCommand(
                request.email(), request.password(), request.fullName(), request.phone()
        );
        User user = registerUserUseCase.execute(command);
        return userMapper.toDto(user);
    }

    public LoginResponse login(LoginRequest request) {
        var command = new AuthenticateUserUseCase.AuthenticateUserCommand(request.email(), request.password());
        var result = authenticateUserUseCase.execute(command);
        List<String> roles = new java.util.ArrayList<>();
        for (int index = 0; index < result.roles().size(); index++) roles.add(result.roles().get(index));
        return new LoginResponse(
                result.accessToken(), result.refreshToken(), java.util.UUID.fromString(result.userId().value()),
                result.email(), result.fullName(), roles
        );
    }

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        var result = refreshAccessTokenUseCase.execute(request.refreshToken());
        return new RefreshTokenResponse(result.accessToken(), result.refreshToken());
    }

    public void logout(String refreshToken) {
        logoutUseCase.execute(refreshToken);
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        forgotPasswordUseCase.execute(request.email());
    }

    public void resetPassword(ResetPasswordRequest request) {
        var command = new ResetPasswordUseCase.ResetPasswordCommand(request.token(), request.newPassword());
        resetPasswordUseCase.execute(command);
    }

    public void changePassword(UUID userId, ChangePasswordRequest request) {
        var command = new ChangePasswordUseCase.ChangePasswordCommand(DomainId.of(userId.toString()), request.currentPassword(), request.newPassword());
        changePasswordUseCase.execute(command);
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(UUID userId) {
        User user = userRepository.findById(DomainId.of(userId.toString()))
                .orElseThrow(() -> new UserNotFoundException(DomainId.of(userId.toString())));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(Email.of(email))
                .orElseThrow(() -> new UserNotFoundException(email));
        return userMapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(int page, int size) {
        List<UserDto> result = new java.util.ArrayList<>();
        var users = userRepository.findAll(page, size);
        for (int index = 0; index < users.size(); index++) result.add(userMapper.toDto(users.get(index)));
        return result;
    }
}
