package com.cotato.itda.domain.signup.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * STEP 2 요청 DTO
 *
 * {
 *   "consents": [
 *     {
 *       "code": "TOS_SERVICE",
 *       "version": "2025-12",
 *       "agreed": true
 *     },
 *     {
 *       "code": "TOS_EFIN",
 *       "version": "2025-12",
 *       "agreed": true
 *     },
 *  *     {
 *  *       "code": "TOS_EFIN",
 *  *       "version": "2025-12",
 *  *       "agreed": true
 *      }
 *   ]
 * }
 */
public record SubmitTermsRequest (
	@NotEmpty(
		message = "동의 항목 목록은 비어 있을 수 없습니다."
	)
	@Valid
	List<Consent> consents
){
	public record Consent (
		@NotBlank(message = "code is required") String code,
		@NotBlank(message = "version is required") String version,
		@NotNull(message = "agreed is required") boolean agreed
	){}
}
