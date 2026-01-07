package com.cotato.itda.domain.signup.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.cotato.itda.domain.signup.model.SignupDraft;
import com.cotato.itda.domain.signup.model.SignupStep;
import com.cotato.itda.global.error.constant.SignupErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import lombok.Builder;

/**
 * Redis에 저장할 Draft 데이터
 * key: signup:draft:{uuid}
 * value : JSON
 * 예시:
 * {
 *     "draftKey": "uuid",
 *     "step": "TERMS_REQUIRED",
 *     "policy": {
 *         "bundleType": "SIGNUP",
 *         "bundleVersion": "2025-12"
 *     },
 *     "meta": {
 *         "createdAt": "2024-10-01T12:00:00Z",
 *         "updatedAt": "2024-10-01T12:00:00Z"
 *     },
 *     "terms": {
 *         "consents": [
 *             {
 *                 "code": "T001",
 *                 "version": "2025-12",
 *                 "agreed": true
 *             },
 *             ...
 *         ]
 *     },
 *     "otp": {
 *         "phone": "01012345678",
 *         "maskedPhone": "010-****-5678",
 *         "canSendSms": false,
 *         "smsSendCount": 1,
 *         "remainingOtpAttempts": 2,
 *         "remainingResendCount": 2,
 *         "resendAvailableAt": "2024-10-01T12:05:00Z",
 *         "otpExpiresAt": "2024-10-01T12:10:00Z",
 *         "phoneVerified": true,
 *         "verifiedAt": "2024-10-01T12:03:00Z"
 *     },
 *     "profile": {
 *         "name": "홍길동",
 *         "birthDate": "1990-01-01"
 *     }
 * }
 */
@Builder
public record SignupDraftRedisValue(
	String draftKey,
	SignupStep step,
	Policy policy,
	Meta meta,

	TermsState terms,    // Step2에서 채워짐
	OtpState otp,        // Step3~4에서 채워짐
	ProfileState profile // Step 5에서 채워짐
) {

	/**
	 * 약관 번들(정책) 고정용
	 * - Step1에서 서버가 "현재 활성 번들"을 선택해서 고정 저장
	 * - 이후 Step2 검증 시에도 이 버전으로 DB 조회
	 */
	public record Policy(
		String bundleType,
		String bundleVersion
	) {
	}

	/**
	 * createdAt/updatedAt 등의 메타 정보
	 */
	public record Meta(
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
	) {
		public Meta withUpdatedAt() {
			return new Meta(this.createdAt, OffsetDateTime.now());
		}
	}

	//----------------------------
	// STEP 2) TermsState
	//----------------------------

	/**
	 * 약관 동의 상태
	 * - Step2에서 클라이언트가 제출한 약관 동의 정보를 저장
	 * - 전체 목록을 저장
	 */
	public record TermsState(
		List<TermsConsent> consents
	) {
		public record TermsConsent(
			Long id,
			String code,
			String version,
			boolean agreed
		) {
		}
	}

	//----------------------------
	// STEP 3~4) OtpState
	//----------------------------

	/**
	 * OPT/SMS 인증 관련 상태
	 * <p>
	 * STEP2 완료 후 OPT_REQUIRED 진입 시 "기본값"이 세팅되고
	 * STE3(SMS 전송)에서 resendAvailableAt, otpExpiresAt/smsSendCount 등이 갱생된다
	 * STEP4(OTP 검증)에서 verifiedAt/ maskedPhoneNumber 등이 세팅된다
	 */
	@Builder
	public record OtpState(
		String phone,                  // 사용자가 입력한 원본(표준화된 형태 권장: 01012345678)

		boolean canSendSms,            // Step2 직후 true, Step3 직후 false
		int smsSendCount,              // Step3 호출 횟수
		int remainingOtpAttempts,      // “현재 발급된 OTP”에 대한 남은 시도 횟수 (예: 3)

		int remainingResendCount,     // 남은 재전송 횟수 (예: 3)
		//레디스에 저장할때는 OffsetDateTime 형태로 저장
		OffsetDateTime resendAvailableAt, // 재전송 가능 시각
		OffsetDateTime otpExpiresAt,       // OTP 만료 시각
		String otpCodeHash,			// OTP 코드 해시값 (STEP3에서 채워짐 -> STEP4에서 검증)

		boolean phoneVerified,         // Step4 성공 시 true
		OffsetDateTime verifiedAt    // Step4 성공 시각
	) {
	}

	//----------------------------
	// STEP 5) ProfileState
	//----------------------------
	public record ProfileState(
		String name,
		LocalDate birthDate
	) {
	}

	// =========================================================
	// 생성/전이용 정적 팩토리
	// =========================================================

	//Step 1: Draft 최초 생성 (TERMS_REQUIRED)
	public static SignupDraftRedisValue newDraft(String draftKey, Policy policy) {
		OffsetDateTime now = OffsetDateTime.now();
		return SignupDraftRedisValue.builder()
			.draftKey(draftKey)
			.step(SignupStep.TERMS_REQUIRED)
			.policy(policy)
			.meta(new Meta(now, now))
			.terms(null)   // terms 아직 없음
			.otp(null)     // otp 아직 없음
			.profile(null) // profile 아직 없음
			.build();

	}

	/**
	 * Step 2 성공: 약관동의확정+ TERMS_REQUIRED -> OTP_REQUIRED 전이
	 * - terms는 채워지고
	 * - step은 OTP_REQUIRED로 변경
	 * - otp는 OTP 단계 초기 상태로 생성 (아직 phone 없음)
	 */
	public SignupDraftRedisValue onTermsSubmitted(TermsState termsState) {
		// 검증: 현재 step이 TERMS_REQUIRED여야 함
		if (this.step != SignupStep.TERMS_REQUIRED) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}

		OtpState otpInit = OtpState.builder()
			.phone(null)                  // phone (step3에서 채워짐)
			.canSendSms(true)             // canSendSms
			.smsSendCount(0)              // smsSendCount , 아직 전송 안함
			.remainingOtpAttempts(3)      // remainingOtpAttempts , 초기값 3회
			.remainingResendCount(3)      // remainingResendCount , 초기값 3회
			.resendAvailableAt(null)      // resendAvailableAt (Step3에서 채워짐)(역할: 재전송 가능 시각)
			.otpExpiresAt(null)           // otpExpiresAt (Step3에서 채워짐)(역할: OTP 만료 시각)
			.otpCodeHash(null)			// otpCodeHash (STEP3에서 채워짐)
			.phoneVerified(false)         // phoneVerified
			.verifiedAt(null)             // verifiedAt (Step4에서 채워짐)(역할: 인증 성공 시각)
			.build();

		return SignupDraftRedisValue.builder()
			.draftKey(this.draftKey)
			.step(SignupStep.OTP_REQUIRED)
			.policy(this.policy)
			.meta(this.meta.withUpdatedAt())
			.terms(termsState)
			.otp(otpInit)
			.profile(this.profile)
			.build();
	}

	/**
	 * STEP 3 성공: SMS 발송 성공 후 OTP 상태 갱신
	 * - phone 저장
	 * - canSendSms=false
	 * - resendAvailableAt/otpExpiresAt 세팅
	 * - smsSendCount +1
	 * - remainingOtpAttempts는 “새 OTP 발급”이므로 보통 3으로 리셋(정책에 따라)
	 */
	public SignupDraftRedisValue onSmsSent(
		String phone,
		OffsetDateTime resendAvailableAt,
		OffsetDateTime otpExpiresAt,
		String otpCodeHash
	) {
		// 검증 : 현재 step이 OTP_REQUIRED여야 함
		if (this.step != SignupStep.OTP_REQUIRED) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		if (this.otp == null) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		// 검증 : 남아있는 재전송 횟수 있어야 함
		if (this.otp.remainingResendCount <= 0) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		// 다음 OTP 상태 생성
		OtpState nextOtp = OtpState.builder()
			.phone(phone)
			.canSendSms(false) // 지금은 SMS 발송 직후이므로 재전송 불가
			.smsSendCount(this.otp.smsSendCount() + 1)
			.remainingOtpAttempts(3) // 남은 시도 횟수 초기화
			.remainingResendCount(this.otp.remainingResendCount() - 1) // 재전송 횟수 1감소
			.resendAvailableAt(resendAvailableAt)
			.otpExpiresAt(otpExpiresAt)
			.otpCodeHash(otpCodeHash)
			.phoneVerified(false)
			.verifiedAt(null)
			.build();

		// 새로운 Draft 값 반환
		return SignupDraftRedisValue.builder()
			.draftKey(this.draftKey)
			.step(this.step) // 여전히 OTP_REQUIRED
			.policy(this.policy)
			.meta(this.meta.withUpdatedAt())
			.terms(this.terms)
			.otp(nextOtp)
			.profile(this.profile)
			.build();
	}

	// STEP 4 실패: OTP 검증 실패 시 remainingOtpAttempts 감소
	public SignupDraftRedisValue onOtpVerifyFailed() {
		if (this.step != SignupStep.OTP_REQUIRED) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		if (this.otp == null) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		int nextRemainingAttempts = Math.max(0, this.otp.remainingOtpAttempts() - 1);
		OtpState nextOtp;
		if (nextRemainingAttempts == 0) {
			// 남은 시도 횟수 0회면 재전송 가능하도록 변경
			nextOtp = OtpState.builder()
				.phone(this.otp.phone())
				.canSendSms(true) // 재전송 가능
				.smsSendCount(this.otp.smsSendCount())
				.remainingOtpAttempts(nextRemainingAttempts)
				.remainingResendCount(this.otp.remainingResendCount())
				.resendAvailableAt(OffsetDateTime.now()) // 재전송 가능 시각 즉시
				.otpExpiresAt(OffsetDateTime.now()) // OTP 만료 시각 즉시
				.otpCodeHash(this.otp.otpCodeHash())
				.phoneVerified(false)
				.verifiedAt(null)
				.build();

		} else { // 아직 남은 시도 횟수 있음
			nextOtp = OtpState.builder()
				.phone(this.otp.phone())
				.canSendSms(this.otp.canSendSms())
				.smsSendCount(this.otp.smsSendCount())
				.remainingOtpAttempts(nextRemainingAttempts) // 감소된 값
				.remainingResendCount(this.otp.remainingResendCount())
				.resendAvailableAt(this.otp.resendAvailableAt())
				.otpExpiresAt(this.otp.otpExpiresAt())
				.otpCodeHash(this.otp.otpCodeHash())
				.phoneVerified(false)
				.verifiedAt(null)
				.build();

		}
		return SignupDraftRedisValue.builder()
			.draftKey(this.draftKey)
			.step(this.step)
			.policy(this.policy)
			.meta(this.meta.withUpdatedAt())
			.terms(this.terms)
			.otp(nextOtp)
			.profile(this.profile)
			.build();

	}

	/**
	 * STEP 4 성공: OTP 검증 성공 후 PROFILE_REQUIRED 전이
	 * - otp.phoneVerified = true
	 * - otp.verifiedAt 세팅
	 * - step = PROFILE_REQUIRED
	 * - maskedPhoneNumber 세팅
	 */
	public SignupDraftRedisValue onOtpVerified() {
		if (this.step != SignupStep.OTP_REQUIRED) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		if (this.otp == null) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}

		OtpState verifiedOtp = OtpState.builder()
			.phone(this.otp.phone())
			.canSendSms(this.otp.canSendSms())
			.smsSendCount(this.otp.smsSendCount())
			.remainingOtpAttempts(this.otp.remainingOtpAttempts())
			.remainingResendCount(this.otp.remainingResendCount())
			.resendAvailableAt(this.otp.resendAvailableAt())
			.otpExpiresAt(this.otp.otpExpiresAt())
			.otpCodeHash(this.otp.otpCodeHash())
			.phoneVerified(true)
			.verifiedAt(OffsetDateTime.now())
			.build();

		return SignupDraftRedisValue.builder()
			.draftKey(this.draftKey)
			.step(SignupStep.PROFILE_REQUIRED)
			.policy(this.policy)
			.meta(this.meta.withUpdatedAt())
			.terms(this.terms)
			.otp(verifiedOtp)
			.profile(this.profile)
			.build();
	}

	/**
	 * STEP 5 성공: 프로필 제출 후 PASSWORD_REQUIRED 전이
	 * - profile 정보 세팅
	 * - step = PASSWORD_REQUIRED
	 */
	public SignupDraftRedisValue onProfileSubmitted(String name, LocalDate birthDate) {
		if (this.step != SignupStep.PROFILE_REQUIRED) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		ProfileState profileState = new ProfileState(
			name,
			birthDate
		);

		return SignupDraftRedisValue.builder()
			.draftKey(this.draftKey)
			.step(SignupStep.PASSWORD_REQUIRED)
			.policy(this.policy)
			.meta(this.meta.withUpdatedAt())
			.terms(this.terms)
			.otp(this.otp)
			.profile(profileState)
			.build();
	}

	/**
	 * * STEP 6 성공: 비밀번호 설정 + DB 커밋 완료 후 COMPLETED로 전이
	 * * - Draft에는 비밀번호를 저장하지 않는다
	 * * - 이 메서드는 완료 표시만 담당
	 */
	public SignupDraftRedisValue onSignupCompleted() {
		if (this.step != SignupStep.PASSWORD_REQUIRED) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}

		return SignupDraftRedisValue.builder()
			.draftKey(this.draftKey)
			.step(SignupStep.COMPLETED)
			.policy(this.policy)
			.meta(this.meta.withUpdatedAt())
			.terms(this.terms)
			.otp(this.otp)
			.profile(this.profile)
			.build();
	}

	public static SignupDraftRedisValue enableResendNowIfPossible(SignupDraftRedisValue draft) {
		if (draft.otp().remainingResendCount() <= 0) return draft;

		// 이미 canSendSms=true면 그대로
		if (draft.otp().canSendSms()) return draft;

		// canSendSms=false인데, now >= resendAvailableAt이면 true로 켤 수 있다.
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime resendAt = draft.otp().resendAvailableAt();

		if (resendAt == null || !now.isBefore(resendAt)) {
			// OtpState만 살짝 갱신한 새 Draft를 만들어준다.
			// (record라서 불변이므로 builder로 재구성)
			SignupDraftRedisValue.OtpState nextOtp = SignupDraftRedisValue.OtpState.builder()
				.phone(draft.otp().phone())
				.canSendSms(true)
				.smsSendCount(draft.otp().smsSendCount())
				.remainingOtpAttempts(draft.otp().remainingOtpAttempts())
				.remainingResendCount(draft.otp().remainingResendCount())
				.resendAvailableAt(now)
				.otpExpiresAt(draft.otp().otpExpiresAt())
				.otpCodeHash(draft.otp().otpCodeHash())
				.phoneVerified(draft.otp().phoneVerified())
				.verifiedAt(draft.otp().verifiedAt())
				.build();

			return SignupDraftRedisValue.builder()
				.draftKey(draft.draftKey())
				.step(draft.step())
				.policy(draft.policy())
				.meta(draft.meta().withUpdatedAt())
				.terms(draft.terms())
				.otp(nextOtp)
				.profile(draft.profile())
				.build();
		}

		return draft;
	}
}
