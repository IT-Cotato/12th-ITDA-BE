package com.cotato.itda.global.security.jwt.token;

import java.time.OffsetDateTime;

import com.cotato.itda.global.security.jwt.config.JwtPurpose;

/**
 *
 * 예시
 * {
 *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
 *   "accessTokenExpiresAt": "2024-10-01T12:34:56+00:00"
 * }
 */
public record IssuedAccessToken(
	String token,
	OffsetDateTime expiresAt
) implements IssuedToken{
	@Override public JwtPurpose purpose(){return JwtPurpose.ACCESS;}
}
