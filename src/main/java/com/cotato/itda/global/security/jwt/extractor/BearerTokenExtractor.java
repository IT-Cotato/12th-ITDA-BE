package com.cotato.itda.global.security.jwt.extractor;

import com.cotato.itda.global.error.constant.JwtErrorCode;
import com.cotato.itda.global.error.exception.JwtAuthenticationException;

/**
 * BearerTokenExtractor
 * 입력으로 받는 헤더에서 Bearer을 빼고 token 값만 추출하는 유틸
 * - 유틸이므로 도메인 예외를 던지는 것보다 IllegalArgumentException같은 일반적인 예외를 던지고,
 * - 상위 계층에서 이를 도메인 예외로 번역하는 패턴이 계층 분리 측면에서 좋다고 판단
 */
public final class BearerTokenExtractor {
	private static final String BEARER_PREFIX = "Bearer ";

	// Lombok의 @NoArgsConstructor는 기본 동작이 public 생성자 생성이다.
	// 그래서 누구든지 인스턴스를 만들 수 있다.
	// 유틸 클래스에서는 인스턴스를 만들지 않는게 원칙이므로 private 생성자로 인스턴스 생성을 막는다.
	private BearerTokenExtractor() {
	}

	// 헤더에 Bearer 토큰이 없으면 null 반환
	public static String extractOrNull(String authorizationHeader) {
		if (authorizationHeader == null || authorizationHeader.isBlank()) {
			return null;
		}
		if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
			return null;
		}
		String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
		return token.isEmpty() ? null : token;
	}

	// 헤더에 Bearer 토큰이 없으면 JwtAuthenticationException 예외 던짐
	public static String extractOrThrow(String authorizationHeader) {
		String token = extractOrNull(authorizationHeader);
		if (token == null) {
			throw new JwtAuthenticationException(JwtErrorCode.TOKEN_NOT_FOUND);
		}
		return token;
	}
}