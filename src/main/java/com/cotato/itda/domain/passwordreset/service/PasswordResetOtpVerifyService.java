package com.cotato.itda.domain.passwordreset.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cotato.itda.domain.passwordreset.dto.PasswordResetDraftRedisValue;
import com.cotato.itda.domain.passwordreset.dto.PasswordResetStep;
import com.cotato.itda.domain.passwordreset.repository.PasswordResetDraftRedisRepository;
import com.cotato.itda.domain.signup.dto.NextAction;
import com.cotato.itda.domain.signup.dto.request.VerifyOtpRequest;
import com.cotato.itda.domain.signup.dto.response.VerifyOtpResponse;
import com.cotato.itda.global.error.constant.PasswordResetErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;


import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetOtpVerifyService {
	private final PasswordResetDraftRedisRepository draftRedisRepository;

	public VerifyOtpResponse verify(String draftKey, VerifyOtpRequest request) {
		PasswordResetDraftRedisValue draft = draftRedisRepository.findByDraftKey(draftKey)
			.orElseThrow(()-> new BusinessException(PasswordResetErrorCode.PASSWORD_RESET_DRAFT_NOT_FOUND));

		// 2. step 검증
		if(draft.step() == PasswordResetStep.PASSWORD_REQUIRED){
			return successResponse(draft); // 이미 성공 상태
		}

		if (draft.step() != PasswordResetStep.OTP_REQUIRED) {
			throw new BusinessException(PasswordResetErrorCode.INVALID_PASSWORD_RESET_STEP);
		}

		// OTP 상태가 없으면 Step2를 거치지 않았거나 Draft가 깨진 상태
		if (draft.otpState() == null) {
			throw new BusinessException(PasswordResetErrorCode.INVALID_PASSWORD_RESET_STEP);
		}
		// ------------------------------------------------------------
		// 3) SMS 발송을 이미 했는지 확인
		// ------------------------------------------------------------
		// Step3이 성공하면 아래 값들이 세팅되어야 한다.
		// - otp.phone
		// - otp.otpExpiresAt
		// - otp.otpCodeHash
		if (draft.otpState().phone() == null || draft.otpState().otpExpiresAt() == null || draft.otpState().otpCodeHash() == null) {
			throw new BusinessException(PasswordResetErrorCode.INVALID_PASSWORD_RESET_STEP);
		}

		OffsetDateTime now = OffsetDateTime.now();

		// ------------------------------------------------------------
		// 4) 만료 체크
		// ------------------------------------------------------------
		// - 만료된 OTP는 맞는 코드를 넣어도 실패로 처리
		// - 만료된 경우 nextAction은 재전송 유도
		if (now.isAfter(draft.otpState().otpExpiresAt())) {
			// 만료면 보통 "재전송 가능" 상태로 돌려줌
			// 현재는 resendAvailableAt == otpExpiresAt이므로
			// otpExpiresAt 이후로 재전송 가능
			PasswordResetDraftRedisValue expiredUpdated = PasswordResetDraftRedisValue.enableResendNowIfPossible(draft);

			// 저장(상태를 바꿨을 수 있으니)
			if (expiredUpdated != draft) {
				draftRedisRepository.updatePreserveTtl(expiredUpdated);
			}

			return failureResponse(
				expiredUpdated,
				chooseNextActionForExpired(expiredUpdated)
			);
		}

		// ------------------------------------------------------------
		// 5) 시도 횟수 체크
		// ------------------------------------------------------------
		// attempts가 0이면 더 이상 verify를 받아주지 않고 재전송을 유도
		if (draft.otpState().remainingOtpAttempts() <= 0) {
			// attempts 0인 상태를 재전송 가능으로 켜줌
			PasswordResetDraftRedisValue attemptsZeroUpdated = PasswordResetDraftRedisValue.enableResendNowIfPossible(draft);

			if (attemptsZeroUpdated != draft) {
				draftRedisRepository.updatePreserveTtl(attemptsZeroUpdated);
			}

			return failureResponse(
				attemptsZeroUpdated,
				chooseNextActionWhenNoAttempts(attemptsZeroUpdated)
			);
		}

		// ------------------------------------------------------------
		// 6) OTP 비교
		// ------------------------------------------------------------
		// - todo: 현재는 평문 비교, 실제로는 해시 비교
		String  inputOtpCode = request.otpCode();
		String draftOtpCodeHash = draft.otpState().otpCodeHash();
		boolean match = inputOtpCode.equals(draftOtpCodeHash);

		if (!match) {
			// --------------------------------------------------------
			// 7) 불일치: attempts 감소 + 상태 저장 + nextAction 결정
			// --------------------------------------------------------
			PasswordResetDraftRedisValue failed = draft.onOtpVerifyFailed();
			draftRedisRepository.updatePreserveTtl(failed);

			NextAction nextAction = chooseNextActionAfterFailed(failed);

			return failureResponse(failed, nextAction);
		}

		// ------------------------------------------------------------
		// 8) 일치(성공): PROFILE_REQUIRED로 전이 + 상태 저장
		// ------------------------------------------------------------

		PasswordResetDraftRedisValue verified = draft.onOtpVerified();
		draftRedisRepository.updatePreserveTtl(verified);

		return successResponse(verified);
	}

	private VerifyOtpResponse successResponse(PasswordResetDraftRedisValue draft) {
		// 성공 응답
		// step=PROFILE_REQUIRED
		// flags.requiredFields=["name","birthDate"]

		return new VerifyOtpResponse(
			PasswordResetStep.PASSWORD_REQUIRED.name(),
			new VerifyOtpResponse.Flags(
				null,
				null,
				null,
				null,
				NextAction.GO_NEXT_STEP,
				List.of("password")
			)
		);
	}

	private VerifyOtpResponse failureResponse(PasswordResetDraftRedisValue draft, NextAction nextAction) {
		// 실패 응답 스펙:
		// step=OTP_REQUIRED
		// flags: canSendSms/resendAvailableAt/remainingOtpAttempts/otpExpiresAt/nextAction
		return new VerifyOtpResponse(
			PasswordResetStep.OTP_REQUIRED.name(),
			new VerifyOtpResponse.Flags(
				draft.otpState().canSendSms(),
				draft.otpState().resendAvailableAt(),
				draft.otpState().remainingOtpAttempts(),
				draft.otpState().otpExpiresAt(),
				nextAction,
				null
			)
		);
	}

	/**
	 * OTP 불일치로 실패 처리 후 nextAction 결정
	 */
	private NextAction chooseNextActionAfterFailed(PasswordResetDraftRedisValue draft) {
		// remainingOtpAttempts > 0 : 아직 입력 재시도 가능
		if (draft.otpState().remainingOtpAttempts() > 0) {
			return NextAction.RETRY_OTP;
		}

		// attempts == 0 이 되었으면 재전송으로 유도
		if (draft.otpState().remainingResendCount() > 0) {
			// canSendSms=true로 켜져있으면 즉시 재전송 가능
			if (draft.otpState().canSendSms()) return NextAction.REQUEST_RESEND_SMS;
			return NextAction.WAIT_RESEND_WINDOW;
		}

		// 재전송도 불가면 가입 재시작
		return NextAction.RESTART_PASSWORD_RESET;
	}

	/**
	 * 만료된 경우 nextAction
	 */
	private NextAction chooseNextActionForExpired(PasswordResetDraftRedisValue draft) {
		// 만료되었는데 재전송 횟수가 남아있으면 재전송
		if (draft.otpState().remainingResendCount() > 0) {
			if (draft.otpState().canSendSms()) return NextAction.REQUEST_RESEND_SMS;
			return NextAction.WAIT_RESEND_WINDOW;
		}
		return NextAction.RESTART_PASSWORD_RESET;
	}

	/**
	 * attempts가 0인 경우 nextAction
	 */
	private NextAction chooseNextActionWhenNoAttempts(PasswordResetDraftRedisValue draft) {
		if (draft.otpState().remainingResendCount() > 0) {
			if (draft.otpState().canSendSms()) return NextAction.REQUEST_RESEND_SMS;
			return NextAction.WAIT_RESEND_WINDOW;
		}
		return NextAction.RESTART_PASSWORD_RESET;
	}

}
