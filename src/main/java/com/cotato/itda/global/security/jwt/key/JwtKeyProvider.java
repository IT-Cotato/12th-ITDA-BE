package com.cotato.itda.global.security.jwt.key;

import java.util.EnumMap;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.cotato.itda.global.security.jwt.config.JwtProperties;
import com.cotato.itda.global.security.jwt.config.JwtPurpose;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

/**
 * JwtKeyProvider
 * 역할
 * - ACCESS / REFRESH 등 "토큰 목적(JwtPurpose)" 별로
 * 서명에 사용할 키(Key)를 생성하고 캐싱하여 제공한다.
 * 설계 의도
 * 키 생성 책임과 토큰 생성/검증 책임의 분리(SRP)
 * - JwtKeyProvider: "키를 어떻게 만들고 관리할지"에만 집중
 * - JwtTokenProvider: "토큰을 어떻게 만들고 검증할지"에만 집중
 * <p>
 * 목적에 따라 다른 키를 사용하는 이유
 * - 토큰 목적과 위험도가 다르기 때문에 키를 분리하면 보안 사고 시 피해 범위를 줄일 수 있다.
 * - 예를 들어 온보딩 로직의 취약점이 Access/Refresh까지 확산되는 것을 막고,
 * 특정 목적 토큰만 독립적으로 키 로테이션할 수 있어 운영에도 유리하다.
 * <p>
 * GlobalErrorCode가 아닌 IllegalStateException을 쓴 이유
 * - JWT secret 설정 오류는 클라이언트 문제가 아닌 서버 설정 문제이다.
 * - 따라서 GlobalException 대신에 IllegalStateException으로 기동 실패시키는 방식이 자연스럽다고 판단했다.
 * -> IllegalArgumentException: 호출자가 전달한 인자가 유효하지 않을 때 사용
 * -> IllegalStateException: 인자는 정상일 수 있지만 객체나 시스템의 현재 상태가 해당 동작을 허용하지 않을 때 사용
 */
@Component
@RequiredArgsConstructor
public class JwtKeyProvider {

	private final JwtProperties properties;
	private final EnumMap<JwtPurpose, SecretKey> keys = new EnumMap<>(JwtPurpose.class);

	/**
	 * - Spring 컨테이너가 빈을 생성하고 의존성 주입을 끝낸 직후 호출된다.
	 * - 여기서 목적별 secret을 읽고 Key로 변환하여 캐시에 적재한다.
	 */
	@PostConstruct
	void init() {
		// ACCESS 토큰 서명 키 생성 및 캐싱
		keys.put(JwtPurpose.ACCESS, build(properties.get(JwtPurpose.ACCESS).getSecretKey(), JwtPurpose.ACCESS));
		// REFRESH 토큰 서명 키 생성 및 캐싱
		keys.put(JwtPurpose.REFRESH, build(properties.get(JwtPurpose.REFRESH).getSecretKey(), JwtPurpose.REFRESH));

		// 모든 키가 잘 로딩되었는지 확인
		if (keys.size() != JwtPurpose.values().length) {
			throw new IllegalStateException("모든 JWT 키가 설정되지 않았습니다.");
		}
	}

	// 목적에 해당하는 서명 키를 반환한다.
	public SecretKey getKey(JwtPurpose purpose) {
		// 호출 시점에 키가 없으면 예외 처리 혹은 null 반환
		SecretKey key = keys.get(purpose);
		if (key == null) {
			throw new IllegalArgumentException("지원하지 않거나 설정되지 않은 토큰 목적입니다: " + purpose);
		}
		return key;
	}

	/**
	 * build() : 설정에 저장된 Base64 문자열을 실제 HMAC 서명키로 변환
	 *
	 * @param base64  : 랜덤 바이트 키를 Base64로 인코딩해 저장한 문자열
	 * @param purpose : 어떤 목적의 키를 만드는지
	 */
	private SecretKey build(String base64, JwtPurpose purpose) {
		// secret이 설정 파일/환경변수에 제대로 존재하는지 1차 검증
		if (base64 == null || base64.isBlank()) {
			throw new IllegalArgumentException(purpose + " JWT 비밀키가 설정 파일에 존재하지 않습니다.");
		}
		try {
			/**
			 * Base64 디코딩
			 *
			 * - base64 문자열은 "포장지"이고,
			 * - decode 결과가 실제 "랜덤 바이트 키"다.
			 *
			 * 즉, getBytes()로 문자열 자체를 키로 쓰는 방식과는 의미가 다르다.
			 */
			byte[] keyBytes = Decoders.BASE64.decode(base64);
			return Keys.hmacShaKeyFor(keyBytes);
		} catch (Exception e) {
			throw new IllegalArgumentException(purpose + " JWT 비밀키가 올바른 Base64 형식이 아닙니다.", e);
		}
	}
}
