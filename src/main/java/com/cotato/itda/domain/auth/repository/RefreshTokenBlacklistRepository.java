package com.cotato.itda.domain.auth.repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshTokenBlacklistRepository {

	private static final String KEY_PREFIX = "auth:refresh:blacklist:";
	private static final String VALUE = "logout";

	private final StringRedisTemplate redis;

	public void save(String refreshToken, Instant expiresAt) {
		Duration ttl = Duration.between(Instant.now(), expiresAt);
		if (ttl.isZero() || ttl.isNegative()) {
			return;
		}

		redis.opsForValue().set(toKey(refreshToken), VALUE, ttl);
	}

	public boolean exists(String refreshToken) {
		Boolean exists = redis.hasKey(toKey(refreshToken));
		return Boolean.TRUE.equals(exists);
	}

	private String toKey(String refreshToken) {
		return KEY_PREFIX + sha256(refreshToken);
	}

	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
		}
	}
}
