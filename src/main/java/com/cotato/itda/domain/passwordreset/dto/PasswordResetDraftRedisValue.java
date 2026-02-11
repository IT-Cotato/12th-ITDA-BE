package com.cotato.itda.domain.passwordreset.dto;

import java.time.OffsetDateTime;

import com.cotato.itda.domain.signup.dto.SignupDraftRedisValue;
import com.cotato.itda.global.error.constant.PasswordResetErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import lombok.Builder;

@Builder
public record PasswordResetDraftRedisValue(
	String draftKey,
	DraftMeta meta,
	PasswordResetStep step,

	Long memberId, // OTP 검증 성공 후 세팅
	OtpState otpState
) {

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

	/**
	 * createdAt/updatedAt 등의 메타 정보
	 */
	public record DraftMeta(
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt
	) {
		public DraftMeta withUpdatedAt() {
			return new DraftMeta(this.createdAt, OffsetDateTime.now());
		}
	}

	// Step 1: Draft 최초 생성(OTP_REQUIRED)
	public static PasswordResetDraftRedisValue newDraft(String draftKey) {
		OffsetDateTime now = OffsetDateTime.now();

		OtpState otpInit = OtpState.builder()
			.phone(null)                  // phone (step3에서 채워짐)
			.canSendSms(true)             // canSendSms
			.smsSendCount(0)              // smsSendCount , 아직 전송 안함
			.remainingOtpAttempts(3)      // remainingOtpAttempts , 초기값 3회
			.remainingResendCount(3)      // remainingResendCount , 초기값 3회
			.resendAvailableAt(null)      // resendAvailableAt (Step2에서 채워짐)(역할: 재전송 가능 시각)
			.otpExpiresAt(null)           // otpExpiresAt (Step2에서 채워짐)(역할: OTP 만료 시각)
			.otpCodeHash(null)            // otpCodeHash (STEP2에서 채워짐)
			.phoneVerified(false)         // phoneVerified
			.verifiedAt(null)             // verifiedAt (Step3에서 채워짐)(역할: 인증 성공 시각)
			.build();

		return PasswordResetDraftRedisValue.builder()
			.draftKey(draftKey)
			.meta(new DraftMeta(now, now))
			.step(PasswordResetStep.OTP_REQUIRED)
			.memberId(null)
			.otpState(otpInit)
			.build();
	}

	// Step 2: SMS 발송 성공 후 OTP 상태 갱신
	// - otpState 값들 세팅
	// meberId 도 세팅 (서비스 로직에서 phone, 이름 입력받은걸로 멤버 조회&검증 후 아이디 세팅)
	public PasswordResetDraftRedisValue onSmsSent(
		String phone,
		Long memberId,
		OffsetDateTime resendAvailableAt,
		OffsetDateTime otpExpiresAt,
		String otpCodeHash
	) {
		if (this.step != PasswordResetStep.OTP_REQUIRED) {
			throw new BusinessException(PasswordResetErrorCode.INVALID_PASSWORD_RESET_STEP);
		}
		if (this.otpState == null) {
			throw new BusinessException(PasswordResetErrorCode.OTP_STATE_NOT_FOUND);
		}
		if (this.otpState.remainingResendCount() <= 0) {
			throw new BusinessException(PasswordResetErrorCode.OTP_RESEND_LIMIT_EXCEEDED);
		}
		OtpState nextOtp = OtpState.builder()
			.phone(phone)
			.canSendSms(false) // SMS 발송 직후에는 재전송 불가
			.smsSendCount(this.otpState.smsSendCount() + 1)
			.remainingOtpAttempts(3) // OTP 발송 시도 시 남은 시도 횟수 초기화
			.remainingResendCount(this.otpState.remainingResendCount() - 1) // 재전송 가능 횟수 차감
			.resendAvailableAt(resendAvailableAt)
			.otpExpiresAt(otpExpiresAt)
			.otpCodeHash(otpCodeHash)
			.phoneVerified(false)
			.verifiedAt(null)
			.build();

		return PasswordResetDraftRedisValue.builder()
			.draftKey(this.draftKey)
			.meta(this.meta.withUpdatedAt())
			.step(this.step) //여전히 OTP_REQUIRED
			.memberId(memberId)
			.otpState(nextOtp)
			.build();
	}

	/**
	 * STEP 3 성공: OTP 검증 성공 후 멤버 ID 세팅
	 * - otpState.phoneVerified = true
	 * - otpState.verifiedAt 세팅
	 * - step = PROFILE_REQUIRED
	 */
	public PasswordResetDraftRedisValue onOtpVerified() {
		if (this.step != PasswordResetStep.OTP_REQUIRED) {
			throw new BusinessException(PasswordResetErrorCode.INVALID_PASSWORD_RESET_STEP);
		}
		if (this.otpState == null) {
			throw new BusinessException(PasswordResetErrorCode.OTP_STATE_NOT_FOUND);
		}
		if(this.memberId==null){
			throw new BusinessException(PasswordResetErrorCode.MEMBER_ID_NOT_SET);
		}
		OtpState nextOtp = OtpState.builder()
			.phone(this.otpState.phone())
			.canSendSms(this.otpState.canSendSms())
			.smsSendCount(this.otpState.smsSendCount())
			.remainingOtpAttempts(this.otpState.remainingOtpAttempts())
			.remainingResendCount(this.otpState.remainingResendCount())
			.resendAvailableAt(this.otpState.resendAvailableAt())
			.otpExpiresAt(this.otpState.otpExpiresAt())
			.otpCodeHash(this.otpState.otpCodeHash())
			.phoneVerified(true)
			.verifiedAt(OffsetDateTime.now())
			.build();

		return PasswordResetDraftRedisValue.builder()
			.draftKey(this.draftKey)
			.meta(this.meta.withUpdatedAt())
			.step(PasswordResetStep.PASSWORD_REQUIRED) //다음 스텝으로
			.memberId(this.memberId)
			.otpState(nextOtp)
			.build();
	}

	/**
	 * STEP 3 실패: OTP 검증 실패 시 남은 시도 횟수 차감
	 * - otpState.remainingOtpAttempts 차감
	 */
	public PasswordResetDraftRedisValue onOtpVerifyFailed() {
		if (this.step != PasswordResetStep.OTP_REQUIRED) {
			throw new BusinessException(PasswordResetErrorCode.INVALID_PASSWORD_RESET_STEP);
		}
		if (this.otpState == null) {
			throw new BusinessException(PasswordResetErrorCode.OTP_STATE_NOT_FOUND);
		}
		int nextRemainingAttempts = Math.max(0, this.otpState.remainingOtpAttempts() - 1);
		OtpState nextOtp;
		if (nextRemainingAttempts == 0) {
			//남은 시도 횟수 0회면 재전송 가능하도록 변경
			nextOtp = OtpState.builder()
				.phone(this.otpState.phone())
				.canSendSms(true) // 재전송 가능하도록 변경
				.smsSendCount(this.otpState.smsSendCount())
				.remainingOtpAttempts(nextRemainingAttempts)
				.remainingResendCount(this.otpState.remainingResendCount())
				.resendAvailableAt(OffsetDateTime.now()) // 재전송 가능 시각 즉시
				.otpExpiresAt(OffsetDateTime.now()) //만료 시간도 현재 시각으로 변경
				.otpCodeHash(this.otpState.otpCodeHash())
				.phoneVerified(false)
				.verifiedAt(null)
				.build();
		} else { //아직 남은 시도 횟수 있음
			nextOtp = OtpState.builder()
				.phone(this.otpState.phone())
				.canSendSms(this.otpState.canSendSms())
				.smsSendCount(this.otpState.smsSendCount())
				.remainingOtpAttempts(nextRemainingAttempts) //감소된 값
				.remainingResendCount(this.otpState.remainingResendCount())
				.resendAvailableAt(this.otpState.resendAvailableAt())
				.otpExpiresAt(this.otpState.otpExpiresAt())
				.otpCodeHash(this.otpState.otpCodeHash())
				.phoneVerified(false)
				.verifiedAt(null)
				.build();
		}

		return PasswordResetDraftRedisValue.builder()
			.draftKey(this.draftKey)
			.meta(this.meta.withUpdatedAt())
			.step(this.step) //여전히 OTP_REQUIRED
			.memberId(this.memberId)
			.otpState(nextOtp)
			.build();
	}

	// Step 4: 비밀번호 재설정 완료 후 DB 커밋 완료 후 COMPLETED 로 전이
	public PasswordResetDraftRedisValue onPasswordResetCompleted() {
		if (this.step != PasswordResetStep.PASSWORD_REQUIRED) {
			throw new BusinessException(PasswordResetErrorCode.INVALID_PASSWORD_RESET_STEP);
		}

		return PasswordResetDraftRedisValue.builder()
			.draftKey(this.draftKey)
			.meta(this.meta.withUpdatedAt())
			.step(PasswordResetStep.PASSWORD_RESET_COMPLETED) //완료 상태로
			.memberId(this.memberId)
			.otpState(this.otpState)
			.build();
	}
	// 재전송 가능 상태로 변경 가능한지 검사 후, 가능하면 변경해주는 유틸 메서드
	public static PasswordResetDraftRedisValue enableResendNowIfPossible(PasswordResetDraftRedisValue draft) {
		// 남은 재전송 횟수 없으면 불가능
		if (draft.otpState().remainingResendCount() <= 0) return draft;

		// 이미 canSendSms=true면 그대로
		if (draft.otpState().canSendSms()) return draft;

		// canSendSms=false인데, now >= resendAvailableAt이면 true로 켤 수 있다.
		OffsetDateTime now = OffsetDateTime.now();
		OffsetDateTime resendAt = draft.otpState().resendAvailableAt();

		if (resendAt == null || !now.isBefore(resendAt)) {
			// OtpState만 살짝 갱신한 새 Draft를 만들어준다.
			// (record라서 불변이므로 builder로 재구성)
			OtpState nextOtp = OtpState.builder()
				.phone(draft.otpState().phone())
				.canSendSms(true)
				.smsSendCount(draft.otpState().smsSendCount())
				.remainingOtpAttempts(draft.otpState().remainingOtpAttempts())
				.remainingResendCount(draft.otpState().remainingResendCount())
				.resendAvailableAt(now)
				.otpExpiresAt(draft.otpState().otpExpiresAt())
				.otpCodeHash(draft.otpState().otpCodeHash())
				.phoneVerified(draft.otpState().phoneVerified())
				.verifiedAt(draft.otpState().verifiedAt())
				.build();

			return PasswordResetDraftRedisValue.builder()
				.draftKey(draft.draftKey())
				.step(draft.step())
				.meta(draft.meta().withUpdatedAt())
				.memberId(draft.memberId())
				.otpState(nextOtp)
				.build();
		}

		return draft;
	}
}
