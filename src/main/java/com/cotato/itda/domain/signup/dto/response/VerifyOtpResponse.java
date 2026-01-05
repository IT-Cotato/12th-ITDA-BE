package com.cotato.itda.domain.signup.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

import com.cotato.itda.domain.signup.dto.NextAction;

/**
	 * STEP4 응답 DTO
	 * - 성공/실패 모두 같은 Response 타입을 쓰되,
	 *   상황에 따라 flags 일부 필드는 null일 수 있다.
	 */
	public record VerifyOtpResponse(
		String step,
		Flags flags
	) {
		public record Flags(
			// 실패/상태용
			Boolean canSendSms,
			OffsetDateTime resendAvailableAt,
			Integer remainingOtpAttempts,
			OffsetDateTime otpExpiresAt,
			NextAction nextAction,
			// 성공용
			List<String> requiredFields
		) {}

		public record Prefill(String phone) {}
	}


