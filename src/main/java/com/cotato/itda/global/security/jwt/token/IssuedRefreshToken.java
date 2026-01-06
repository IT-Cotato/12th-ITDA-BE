package com.cotato.itda.global.security.jwt.token;

import java.time.OffsetDateTime;

import com.cotato.itda.global.security.jwt.config.JwtPurpose;

public record IssuedRefreshToken(
	String token,
	OffsetDateTime expiresAt
) implements IssuedToken{
	@Override public JwtPurpose purpose(){return JwtPurpose.REFRESH;}
}
