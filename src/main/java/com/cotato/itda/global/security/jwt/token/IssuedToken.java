package com.cotato.itda.global.security.jwt.token;

import java.time.Instant;
import java.time.OffsetDateTime;

import com.cotato.itda.global.security.jwt.config.JwtPurpose;

public sealed interface IssuedToken
permits IssuedAccessToken, IssuedRefreshToken {
	JwtPurpose purpose();
	String token();
	OffsetDateTime expiresAt();
}
