package com.cotato.itda.domain.sms.infra.solapi;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.cotato.itda.domain.sms.infra.solapi.config.SolapiProperties;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SolapiAuthHeaderGenerator {

	private final SolapiProperties props;

	/**
	 * SOLAPI API Key 인증 헤더 생성
	 * 요청 헤더 예시:
	 * - Authorization: HMAC-SHA256 apiKey=..., date=..., salt=..., signature=...
	 * - signature = HMAC_SHA256( (date + salt), key = apiSecret ) → hex
	 * - date는 ISO-8601 (Instant.now().toString() 형태)
	 * - 매 요청 salt는 달라야 함
	 */
	public String createAuthHeader() {
		try {
			String dateTime = Instant.now().toString(); // ISO-8601 (예: 2025-12-31T09:00:00Z)
			String salt = UUID.randomUUID()
				.toString()
				.replace("-", ""); // 랜덤 문자열(문서상 12~64 bytes 권장) :contentReference[oaicite:5]{index=5}

			String signature = hmacSha256Hex(props.apiSecret(), dateTime + salt);

			return "HMAC-SHA256 apiKey=%s, date=%s, salt=%s, signature=%s"
				.formatted(props.apiKey(), dateTime, salt, signature);
		} catch (Exception e) {
			// 인증 헤더 생성 실패는 "우리 서버 설정/키 문제"인 경우가 대부분
			throw new IllegalStateException("Failed to create SOLAPI Authorization header", e);
		}
	}

	private String hmacSha256Hex(String secret, String data) throws Exception {
		Mac mac = Mac.getInstance("HmacSHA256");
		mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
		byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
		return HexFormat.of().formatHex(hash);
	}
}
