package com.cotato.itda.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cotato.itda.domain.auth.repository.RefreshTokenBlacklistRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

	@Mock
	private RefreshTokenBlacklistRepository refreshTokenBlacklistRepository;

	@InjectMocks
	private LogoutService logoutService;

	@Test
	void logout_saves_refresh_token_to_blacklist_until_expiration() {
		String refreshToken = "refresh-token";
		Date expiration = Date.from(Instant.now().plusSeconds(3600));
		Claims claims = Jwts.claims()
			.expiration(expiration)
			.build();

		logoutService.logout(refreshToken, claims);

		ArgumentCaptor<Instant> expiresAtCaptor = ArgumentCaptor.forClass(Instant.class);
		verify(refreshTokenBlacklistRepository).save(eq(refreshToken), expiresAtCaptor.capture());
		assertThat(expiresAtCaptor.getValue()).isEqualTo(claims.getExpiration().toInstant());
	}
}
