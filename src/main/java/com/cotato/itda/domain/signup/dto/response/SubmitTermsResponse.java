package com.cotato.itda.domain.signup.dto.response;

import com.cotato.itda.domain.signup.model.SignupStep;

/**
 * STEP 2 응답 DTO
 *
 * {
 *   "step": "OTP_REQUIRED",
 *   "flags": {
 *     "canSendSms": true,
 *     "smsSendCount": 0,
 *     "remainingOtpAttempts": 3
 *   }
 * }
 */
public record SubmitTermsResponse (
	SignupStep step,
	Flags flags
){
	public record Flags(
		boolean canSendSms,
		int smsSendCount,
		int remainingOtpAttempts
	){

	}
}
