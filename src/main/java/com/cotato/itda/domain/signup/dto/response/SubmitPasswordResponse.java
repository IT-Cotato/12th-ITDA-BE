package com.cotato.itda.domain.signup.dto.response;

import java.time.OffsetDateTime;

import com.cotato.itda.domain.auth.dto.Tokens;

/**
 * Step6 응답: access + refresh 동시 발급
 */
public record SubmitPasswordResponse (
	String step,
	Long memberId,
	Tokens tokens
){

}
