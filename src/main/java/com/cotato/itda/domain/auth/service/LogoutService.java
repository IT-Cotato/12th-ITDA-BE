package com.cotato.itda.domain.auth.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.auth.repository.RefreshTokenBlacklistRepository;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogoutService {

	private final RefreshTokenBlacklistRepository refreshTokenBlacklistRepository;

	@Transactional
	public void logout(String refreshToken, Claims refreshClaims) {
		Instant expiresAt = refreshClaims.getExpiration().toInstant();
		refreshTokenBlacklistRepository.save(refreshToken, expiresAt);
	}
}
