package com.appointymed.auth.domain.usecase;

import com.appointymed.auth.domain.model.DomainId;
import com.appointymed.auth.domain.model.DomainInstant;
import com.appointymed.auth.domain.model.DomainList;
import com.appointymed.auth.domain.model.User;

public interface JwtTokenProvider {
    String generateAccessToken(User user);

    String generateRefreshToken();

    DomainInstant getRefreshTokenExpiry();

    DomainId getUserIdFromAccessToken(String token);

    DomainId getUserIdFromRefreshToken(String token);

    DomainList<String> getRolesFromAccessToken(String token);
}
