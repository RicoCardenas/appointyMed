package com.appointy.auth.domain.model;

import java.time.Instant;

public record TokenRevocado(String jti, Instant expiraEn) {}
