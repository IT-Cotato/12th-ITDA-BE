package com.cotato.itda.domain.passwordreset.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.itda.domain.passwordreset.dto.SendOtpRequest;
import com.cotato.itda.domain.passwordreset.dto.response.PasswordResetCreateDraftResponse;
import com.cotato.itda.domain.passwordreset.service.PasswordResetDraftService;
import com.cotato.itda.domain.passwordreset.service.PasswordResetOtpVerifyService;
import com.cotato.itda.domain.passwordreset.service.PasswordResetPasswordService;
import com.cotato.itda.domain.passwordreset.service.PasswordResetSmsService;
import com.cotato.itda.domain.signup.dto.request.SendOtpSmsRequest;
import com.cotato.itda.domain.signup.dto.request.SubmitPasswordRequest;
import com.cotato.itda.domain.signup.dto.request.VerifyOtpRequest;
import com.cotato.itda.domain.signup.dto.response.SendOtpSmsResponse;
import com.cotato.itda.domain.signup.dto.response.SubmitPasswordResponse;
import com.cotato.itda.domain.signup.dto.response.VerifyOtpResponse;
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
	name = "Password Reset",
	description = "비밀번호 재설정 단계별 API"
)
@RequestMapping("/api/password-reset")
@RestController
@RequiredArgsConstructor
public class PasswordResetController {

	/**
	 * Password Reset 흐름에서 서버가 발급한 임시 draftKey를 담아 주고받는 헤더 이름.
	 * - 클라이언트는 이후 요청마다 이 헤더를 보내야 한다.
	 * - 예: Password-Reset-Draft-Key: 550e8400-e29b-41d4-a716-446655440000
	 */
	private static final String HEADER_DRAFT_KEY = "Password-Reset-Draft-Key";
	private final PasswordResetDraftService passwordResetDraftService;
	private final PasswordResetSmsService passwordResetSmsService;
	private final PasswordResetOtpVerifyService passwordResetOtpVerifyService;
	private final PasswordResetPasswordService passwordResetPasswordService;
	@Operation(
		summary = "STEP1) Password Reset Draft 생성",
		description = "비밀번호 재설정을 위한 Draft를 생성합니다. 생성된 Draft Key는 이후 요청에 사용됩니다."
	)
	@PostMapping("/drafts")
	public ResponseEntity<ApiResponse<PasswordResetCreateDraftResponse>> createPasswordResetDraft(){
		PasswordResetDraftService.DraftCreationResult result= passwordResetDraftService.createDraft();
		return ResponseEntity
			.ok()
			.header(HEADER_DRAFT_KEY, result.draftKey())
			.body(ApiResponse.success(result.response()));
	}

	/**
	 * Step2) 인증번호(SMS) 발송
	 * <p>
	 * ✅ API
	 * - POST /api/signup/sms/send
	 * - Header: Signup-Draft-Key
	 * - Body: { "phoneNumber": "01012345678", name="김기민" }
	 */
	@Operation(
		summary = "Step2) 인증번호 SMS 발송",
		description = "입력된 전화번호로 OTP 인증번호를 SMS로 발송한다. DraftKey가 유효해야 한다.",
		security = {}
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

		@RequestBody @Valid SendOtpRequest request
	) {
		log.info("SignupController.sendOptSms: draftKey={}, phoneNumber={}", draftKey, request.phoneNumber());
		SendOtpSmsResponse response = passwordResetSmsService.sendOtpSms(draftKey, request);
		return ApiResponse.success(response);
	}

	/**
	 * Step3) 인증번호(OTP) 검증
	 * 	 * 예시
	 * 	 * - Request Header: Signup-Draft-Key: 550e8400-e29b-41d4-a716-446655440000
	 * 	 * - Request Body: { "otpCode": "1234" }
	 */
	@Operation(
		summary = "Step3) OTP 검증",
		description = "DraftKey 기반 회원가입 흐름에서 OTP를 검증한다. 결과는 nextAction으로 분기한다.",
		security = {}
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
		return ApiResponse.success(passwordResetOtpVerifyService.verify(draftKey, request));
	}

	/**
	 * STEP4) 새 비밀번호 설정
	 * <p>
	 * - Controller는 HTTP 바인딩만 담당한다.
	 * - Draft 조회/검증/상태전이/저장은 Service에서 한다.
	 * <p>
	 * 예시
	 * - Request Header: Password-Reset-Draft-Key: 550e8400-e29b-41d4-a716-446655440000
	 * - Request Body: { "newPassword": "NewP@ssw0rd!" }
	 */
	@Operation(
		summary = "Step4) 비밀번호 재설정",
		description = """
			DraftKey 흐름에서 비밀번호를 재설정한다.
			
			- Header: Signup-Draft-Key (Step1에서 발급)
			- Body: password / passwordConfirm(또는 confirmPassword) 등 비밀번호 입력값
			- 성공 시: 비밀번호 변경 + 로그인 페이지로 이동 유도
			""",
		security = {}
	)
	@PostMapping("/password")
	public ApiResponse<Void> submitPassword(
		@Parameter(
			name = HEADER_DRAFT_KEY,
			in = ParameterIn.HEADER,
			required = true,
			description = "Step1에서 발급받은 DraftKey. 이후 모든 단계 요청에 포함한다.",
			example = "550e8400-e29b-41d4-a716-446655440000"
		)
		@RequestHeader(HEADER_DRAFT_KEY) String draftKey,

		@RequestBody @Valid SubmitPasswordRequest request
	) {
		return ApiResponse.success(passwordResetPasswordService.resetPassword(draftKey, request));
	}
}

