package com.cotato.itda.domain.signup.dto;

import java.time.OffsetDateTime;

import com.cotato.itda.domain.signup.model.SignupStep;

/**
 * Redis에 저장할 Draft 데이터
 * key: signup:draft:{uuid}
 * value : JSON
 * 예시
 {
 "draftKey": "3f2c6d6b-2c5c-4c61-9f3d-8a7a4f1c9b1e",
 "step": "TERMS_REQUIRED",
 "bundleType": "SIGNUP",
 "bundleVersion": "2025-12",
 "createdAt": "2025-12-28T13:05:12Z"
 }
 */
public record SignupDraftRedisValue(
	String draftKey,
	SignupStep step,
	String bundleType,
	String bundleVersion,
	OffsetDateTime createdAt
) {
}
