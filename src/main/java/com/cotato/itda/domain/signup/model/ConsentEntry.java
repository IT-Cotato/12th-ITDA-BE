package com.cotato.itda.domain.signup.model;

import java.time.OffsetDateTime;

/**
 * 약관 동의 1건 저장 모델
 */
public record ConsentEntry(
	String code,
	String version,
	boolean agreed,
	OffsetDateTime decidedAt
) {
}
