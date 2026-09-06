package com.appointymed.auth.infrastructure.config;

import com.appointymed.auth.domain.model.DomainClock;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.Password;
import com.appointymed.auth.domain.repository.*;
import com.appointymed.auth.domain.usecase.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {
    @Bean DomainClock domainClock() { return () -> DomainInstant.ofEpochMillis(System.currentTimeMillis()); }
    @Bean RegisterUserUseCase registerUserUseCase(UserRepository users, RoleRepository roles, Password.PasswordEncoder encoder) { return new RegisterUserUseCase(users, roles, encoder); }
    @Bean AuthenticateUserUseCase authenticateUserUseCase(UserRepository users, RefreshTokenRepository tokens, Password.PasswordEncoder encoder, JwtTokenProvider jwt) { return new AuthenticateUserUseCase(users, tokens, encoder, jwt); }
    @Bean RefreshAccessTokenUseCase refreshAccessTokenUseCase(RefreshTokenRepository tokens, UserRepository users, JwtTokenProvider jwt, DomainClock clock) { return new RefreshAccessTokenUseCase(tokens, users, jwt, clock); }
    @Bean LogoutUseCase logoutUseCase(RefreshTokenRepository tokens) { return new LogoutUseCase(tokens); }
    @Bean ForgotPasswordUseCase forgotPasswordUseCase(UserRepository users, ForgotPasswordUseCase.PasswordResetTokenProvider tokens, ForgotPasswordUseCase.NotificationEventPublisher publisher) { return new ForgotPasswordUseCase(users, tokens, publisher); }
    @Bean ResetPasswordUseCase resetPasswordUseCase(UserRepository users, ForgotPasswordUseCase.PasswordResetTokenProvider tokens, Password.PasswordEncoder encoder, DomainClock clock) { return new ResetPasswordUseCase(users, tokens, encoder, clock); }
    @Bean ChangePasswordUseCase changePasswordUseCase(UserRepository users, Password.PasswordEncoder encoder, DomainClock clock) { return new ChangePasswordUseCase(users, encoder, clock); }
    @Bean AssignRoleUseCase assignRoleUseCase(UserRepository users, DomainClock clock) { return new AssignRoleUseCase(users, clock); }
    @Bean RevokeRoleUseCase revokeRoleUseCase(UserRepository users, DomainClock clock) { return new RevokeRoleUseCase(users, clock); }
    @Bean GetUserPermissionsUseCase getUserPermissionsUseCase(UserRepository users) { return new GetUserPermissionsUseCase(users); }
    @Bean AuditOperationUseCase auditOperationUseCase(AuditLogRepository audits) { return new AuditOperationUseCase(audits); }
}
