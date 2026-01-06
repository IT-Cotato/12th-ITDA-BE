package com.cotato.itda.domain.signup.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.itda.domain.signup.dto.request.SubmitPasswordRequest;
import com.cotato.itda.domain.signup.dto.request.SubmitProfileRequest;
import com.cotato.itda.domain.signup.dto.response.CreateDraftResponse;
import com.cotato.itda.domain.signup.dto.request.SendOtpSmsRequest;
import com.cotato.itda.domain.signup.dto.response.SendOtpSmsResponse;
import com.cotato.itda.domain.signup.dto.request.SubmitTermsRequest;
import com.cotato.itda.domain.signup.dto.response.SubmitPasswordResponse;
import com.cotato.itda.domain.signup.dto.response.SubmitProfileResponse;
import com.cotato.itda.domain.signup.dto.response.SubmitTermsResponse;
import com.cotato.itda.domain.signup.dto.request.VerifyOtpRequest;
import com.cotato.itda.domain.signup.dto.response.VerifyOtpResponse;
import com.cotato.itda.domain.signup.service.SignupDraftService;
import com.cotato.itda.domain.signup.service.SignupOtpVerifyService;
import com.cotato.itda.domain.signup.service.SignupPasswordService;
import com.cotato.itda.domain.signup.service.SignupProfileService;
import com.cotato.itda.domain.signup.service.SignupSmsService;
import com.cotato.itda.domain.signup.service.SignupTermsService;
import com.cotato.itda.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(
	name = "Signup",
	description = "회원가입 단계별 API (Draft 생성 → 약관 제출 → SMS 발송)"
)
@RestController
@RequestMapping("/api/signup")
@RequiredArgsConstructor
public class SignupController {

	/**
	 * Signup 흐름에서 서버가 발급한 임시 draftKey를 담아 주고받는 헤더 이름.
	 * - 클라이언트는 이후 요청마다 이 헤더를 보내야 한다.
	 * - 예: Signup-Draft-Key: 550e8400-e29b-41d4-a716-446655440000
	 */
	private static final String HEADER_DRAFT_KEY = "Signup-Draft-Key";

	private final SignupDraftService signupDraftService;
	private final SignupTermsService signupTermsService;
	private final SignupSmsService signupSmsService;
	private final SignupOtpVerifyService signupOtpVerifyService;
	private final SignupProfileService signupProfileService;
	private final SignupPasswordService signupPasswordService;

	/**
	 * Step1) 회원가입 Draft 생성
	 * <p>
	 * ✅ 하는 일
	 * - 회원가입을 시작하기 위한 "임시 저장소(draft)"를 만든다.
	 * - 생성된 draftKey를 응답 헤더(Signup-Draft-Key)에 실어준다.
	 * <p>
	 * ✅ 프론트 사용 방법
	 * - 이 응답에서 받은 draftKey를 저장해두고
	 * - 다음 단계 요청들에 Header로 계속 보내야 한다.
	 */
	@Operation(
		summary = "Step1) 회원가입 Draft 생성",
		description = "회원가입 절차를 시작하기 위한 임시 Draft를 생성하고, draftKey를 응답 헤더(Signup-Draft-Key)로 반환한다."
	)
	@PostMapping("/drafts")
	public ResponseEntity<ApiResponse<CreateDraftResponse>> createSignupDraft() {
		SignupDraftService.DraftCreationResult result = signupDraftService.createSignupDraft();

		return ResponseEntity
			.ok()
			// ✅ 프론트가 다음 단계에서 이 값을 헤더로 보내야 하므로 헤더로 내려준다.
			.header(HEADER_DRAFT_KEY, result.draftKey())
			.body(ApiResponse.success(result.response()));
	}

	/**
	 * Step2) 약관 동의 제출
	 * <p>
	 * ✅ 요구 사항
	 * - Header: Signup-Draft-Key 필수
	 * - Body: 약관 동의 목록/버전 등
	 */
	@Operation(
		summary = "Step2) 약관 동의 제출",
		description = "DraftKey로 식별되는 회원가입 흐름에서 약관 동의 내용을 제출한다. 성공 시 다음 단계로 진행된다."
	)
	@PostMapping("/drafts/terms")
	public ApiResponse<SubmitTermsResponse> submitTerms(
		@Parameter(
			name = HEADER_DRAFT_KEY,
			in = ParameterIn.HEADER,
			required = true,
			description = "Step1에서 발급받은 draftKey. 이후 모든 회원가입 단계 요청에 포함해야 한다.",
			example = "550e8400-e29b-41d4-a716-446655440000"
		)
		@RequestHeader(HEADER_DRAFT_KEY) String draftKey,

		@RequestBody @Valid SubmitTermsRequest request
	) {
		SubmitTermsResponse response = signupTermsService.submitTerms(draftKey, request);
		return ApiResponse.success(response);
	}

	/**
	 * Step3) 인증번호(SMS) 발송
	 * <p>
	 * ✅ API
	 * - POST /api/signup/sms/send
	 * - Header: Signup-Draft-Key
	 * - Body: { "phoneNumber": "01012345678" }
	 */
	@Operation(
		summary = "Step3) 인증번호 SMS 발송",
		description = "입력된 전화번호로 OTP 인증번호를 SMS로 발송한다. DraftKey가 유효해야 한다."
	)
	@PostMapping("/sms/send")
	public ApiResponse<SendOtpSmsResponse> sendOptSms(
		@Parameter(
			name = HEADER_DRAFT_KEY,
			in = ParameterIn.HEADER,
			required = true,
			description = "Step1에서 발급받은 draftKey",
			example = "550e8400-e29b-41d4-a716-446655440000"
		)
		@RequestHeader(HEADER_DRAFT_KEY) String draftKey,

		@RequestBody @Valid SendOtpSmsRequest request
	) {
		log.info("SignupController.sendOptSms: draftKey={}, phoneNumber={}", draftKey, request.phoneNumber());
		SendOtpSmsResponse response = signupSmsService.sendOtpSms(draftKey, request);
		return ApiResponse.success(response);
	}

	/**
	 * STEP4) OTP 검증
	 * <p>
	 * - Controller는 HTTP 바인딩만 담당한다.
	 * - Draft 조회/검증/상태전이/저장은 Service에서 한다.
	 * <p>
	 * 예시
	 * - Request Header: Signup-Draft-Key: 550e8400-e29b-41d4-a716-446655440000
	 * - Request Body: { "otpCode": "1234" }
	 */
	@Operation(
		summary = "Step4) OTP 검증",
		description = "DraftKey 기반 회원가입 흐름에서 OTP를 검증한다. 결과는 nextAction으로 분기한다."
	)
	@PostMapping("/otp/verify")
	public ApiResponse<VerifyOtpResponse> verifyOtp(
		@Parameter(
			name = HEADER_DRAFT_KEY,
			in = ParameterIn.HEADER,
			required = true,
			description = "Step1에서 발급받은 DraftKey",
			example = "550e8400-e29b-41d4-a716-446655440000"
		)
		@RequestHeader(HEADER_DRAFT_KEY) String draftKey,
		@RequestBody @Valid VerifyOtpRequest request
	) {
		return ApiResponse.success(signupOtpVerifyService.verify(draftKey, request));
	}

	@Operation(
		summary = "Step5) 프로필 정보 제출",
		description = """
			회원가입 DraftKey 흐름에서 프로필 정보를 제출한다.
			일반적으로 Step4(OTP 검증) 성공 이후 호출되며, 성공 시 다음 단계로 진행된다(예: PASSWORD_REQUIRED).
			
			- Header: Signup-Draft-Key (Step1에서 발급)
			- Body: 이름/생년월일 등 프로필 입력값
			"""
	)
	@PostMapping("/profile")
	public ApiResponse<SubmitProfileResponse> submitProfile(
		@Parameter(
			name = HEADER_DRAFT_KEY,
			in = ParameterIn.HEADER,
			required = true,
			description = "Step1에서 발급받은 DraftKey(회원가입 진행 식별자). 이후 모든 단계 요청에 포함한다.",
			example = "550e8400-e29b-41d4-a716-446655440000"
		)
		@RequestHeader(HEADER_DRAFT_KEY) String draftKey,

		@RequestBody @Valid SubmitProfileRequest request
	) {
		return ApiResponse.success(signupProfileService.submit(draftKey, request));
	}

	/**
	 * STEP6) 비밀번호 설정
	 */
	@Operation(
		summary = "Step6) 비밀번호 설정",
		description = """
			회원가입 DraftKey 흐름에서 비밀번호를 설정한다.
			일반적으로 Step5(프로필 제출) 성공 이후 호출되며, 성공 시 회원가입이 최종 완료된다.
			
			- Header: Signup-Draft-Key (Step1에서 발급)
			- Body: password / passwordConfirm(또는 confirmPassword) 등 비밀번호 입력값
			- 성공 시: 회원 DB 커밋 + 로그인 토큰 발급(Access/Refresh) 또는 완료 응답 반환
			"""
	)
	@PostMapping("/password")
	public ApiResponse<SubmitPasswordResponse> submitPassword(
		@Parameter(
			name = HEADER_DRAFT_KEY,
			in = ParameterIn.HEADER,
			required = true,
			description = "Step1에서 발급받은 DraftKey(회원가입 진행 식별자). 이후 모든 단계 요청에 포함한다.",
			example = "550e8400-e29b-41d4-a716-446655440000"
		)
		@RequestHeader(HEADER_DRAFT_KEY) String draftKey,

		@RequestBody @Valid SubmitPasswordRequest request
	) {
		return ApiResponse.success(signupPasswordService.submit(draftKey, request));
	}
}