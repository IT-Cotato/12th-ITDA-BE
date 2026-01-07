package com.cotato.itda.domain.auth.dto;

import java.time.OffsetDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

public record Tokens(
	@Schema(description = "Access Token(JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	String accessToken,

	@Schema(description = "Refresh Token(JWT)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	String refreshToken,

	@Schema(description = "Access Token 만료 시각", example = "2026-01-07T23:59:59+09:00")
	OffsetDateTime accessTokenExpiresAt,

	@Schema(description = "Refresh Token 만료 시각", example = "2026-01-21T23:59:59+09:00")
	OffsetDateTime refreshTokenExpiresAt
){}