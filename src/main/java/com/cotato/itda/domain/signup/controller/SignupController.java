package com.cotato.itda.domain.signup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.itda.domain.auth.dto.StepResponse;
import com.cotato.itda.domain.signup.dto.CreateDraftResponse;
import com.cotato.itda.domain.signup.service.SignupDraftService;
import com.cotato.itda.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

/**
 * 자체 로그인 , 회원가입, 토큰 재발급 등의 인증 관련 API를 처리하는 컨트롤러 클래스입니다.
 */
@RestController
@RequestMapping("/api/signup")
@RequiredArgsConstructor
public class SignupController {

	private static final String HEADER_DRAFT_KEY = "Signup-Draft-Key";
	private final SignupDraftService signupDraftService;

	/**
	 * 회원가입 임시 저장소 생성 엔드포인트.
	 * <p>
	 * - 이 엔드포인트는 회원가입 절차를 시작하기 위해 임시 저장소를 생성합니다.
	 * - 생성된 임시 저장소의 키를 반환합니다.
	 */
	@PostMapping("/drafts")
	public ResponseEntity<ApiResponse<CreateDraftResponse>> createSignupDraft() {
		SignupDraftService.DraftCreationResult result = signupDraftService.createSignupDraft();
		return ResponseEntity
			.ok()
			.header(HEADER_DRAFT_KEY,result.draftKey())
			.body(ApiResponse.success(result.response()));
	}
}
