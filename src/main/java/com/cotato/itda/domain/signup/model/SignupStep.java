package com.cotato.itda.domain.signup.model;

public enum SignupStep {
	TERMS_REQUIRED, // 약관 동의 필요 상태(회원가입 버튼 누름)
	OPT_REQUIRED, // 약관 동의 성공 상태(약관선택하고 동의하기 버튼 누름) & SMS 발송 성공(인증번호받기 버튼 누름)
	PROFILE_REQUIRED, // OPT 인증 완료 상태 (인증하기 버튼 누름)
	PASSWORD_REQUIRED, // 프로필 제출 완료(확인 버튼 누름[이름,생년월일 넘어옴])
	COMPLETED // 완료된 상태(완료 버튼 누름(비밀번호와 재입력한 비밀번호 넘어옴))
}
