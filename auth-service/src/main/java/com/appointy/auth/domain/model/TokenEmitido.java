package com.appointy.auth.domain.model;

import java.time.Instant;

public record TokenEmitido(String token, String jti, Instant expiraEn) {}
