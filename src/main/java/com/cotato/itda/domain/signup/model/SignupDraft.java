package com.cotato.itda.domain.signup.model;

import java.time.OffsetDateTime;

// Redis에 저장할 Draft
public record SignupDraft (
	String draftKey,          // UUID 문자열
	SignupStep step,          // 현재 단계
	TermsBundle termsBundle,  // 약관 번들(버전/ID)
	OffsetDateTime createdAt  // 생성 시간
){
	public static SignupDraft createNew(String draftKey, TermsBundle termsBundle, OffsetDateTime now){
		return new SignupDraft(
			draftKey,
			SignupStep.TERMS_REQUIRED,
			termsBundle,
			now
		);
	}
}
