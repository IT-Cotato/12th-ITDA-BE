package com.cotato.itda.domain.signup.dto;

public enum NextAction {
	RETRY_OTP,           // OTP가 틀렸지만 재시도 가능 → OTP 입력 화면 유지하고 다시 입력 유도
	REQUEST_RESEND_SMS,  // OTP 만료/재발송 필요 → SMS 재발송 요청 버튼/흐름으로 유도
	WAIT_RESEND_WINDOW,  // 재발송 쿨타임 중 → resendAvailableAt까지 대기(버튼 비활성/카운트다운)
	RESTART_SIGNUP,      // draft 만료/상태 불일치 등 → 회원가입 흐름을 처음부터 다시 시작
	GO_NEXT_STEP,         // OTP 검증 성공 → 다음 단계(프로필/비밀번호 등)로 진행
	RESTART_PASSWORD_RESET // 비밀번호 재설정 전체 과정 처음부터 다시 시작
}
