package com.cotato.itda.domain.signup.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cotato.itda.domain.signup.dto.NextAction;
import com.cotato.itda.domain.signup.dto.SignupDraftRedisValue;
import com.cotato.itda.domain.signup.dto.request.VerifyOtpRequest;
import com.cotato.itda.domain.signup.dto.response.VerifyOtpResponse;
import com.cotato.itda.domain.signup.model.SignupStep;
import com.cotato.itda.domain.signup.repository.SignupDraftRedisRepository;
import com.cotato.itda.global.error.constant.SignupErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupOtpVerifyService {

	private final SignupDraftRedisRepository draftRedisRepository;

	/**
	 * STEP4) OTP 검증
	 *
	 * ✅ 반드시 처리해야 하는 운영 케이스들:
	 * 1) draftKey가 없거나 만료됨
	 * 2) step이 OTP_REQUIRED가 아닌데 호출됨(클라이언트 역순 호출 / 중복 호출)
	 * 3) 아직 Step3(SMS발송) 안 했는데 verify를 호출함
	 * 4) OTP 만료됨 (otpExpiresAt 지남)
	 * 5) 시도 횟수(remainingOtpAttempts) 0
	 * 6) OTP 불일치 → attempts 감소 + nextAction 내려줌
	 * 7) OTP 일치 → PROFILE_REQUIRED로 전이
	 */
	public VerifyOtpResponse verify(String draftKey, VerifyOtpRequest request) {
		//1. draft 조회
		SignupDraftRedisValue draft = draftRedisRepository.findByDraftKey(draftKey)
			.orElseThrow(() -> new BusinessException(SignupErrorCode.SIGNUP_DRAFT_NOT_FOUND));

		//2. Step 검증
		if (draft.step() == SignupStep.PROFILE_REQUIRED) {
			return successResponse(draft); // 이미 성공 상태
		}

		if (draft.step() != SignupStep.OTP_REQUIRED) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}

		// OTP 상태가 없으면 Step2를 거치지 않았거나 Draft가 깨진 상태
		if (draft.otp() == null) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		// ------------------------------------------------------------
		// 3) SMS 발송을 이미 했는지 확인
		// ------------------------------------------------------------
		// Step3이 성공하면 아래 값들이 세팅되어야 한다.
		// - otp.phone
		// - otp.otpExpiresAt
		// - otp.otpCodeHash
		if (draft.otp().phone() == null || draft.otp().otpExpiresAt() == null || draft.otp().otpCodeHash() == null) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}

		OffsetDateTime now = OffsetDateTime.now();

		// ------------------------------------------------------------
		// 4) 만료 체크
		// ------------------------------------------------------------
		// - 만료된 OTP는 맞는 코드를 넣어도 실패로 처리
		// - 만료된 경우 nextAction은 재전송 유도
		if (now.isAfter(draft.otp().otpExpiresAt())) {
			// 만료면 보통 "재전송 가능" 상태로 돌려줌
			// 현재는 resendAvailableAt == otpExpiresAt이므로
			// otpExpiresAt 이후로 재전송 가능
			SignupDraftRedisValue expiredUpdated = SignupDraftRedisValue.enableResendNowIfPossible(draft);

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
		if (draft.otp().remainingOtpAttempts() <= 0) {
			// attempts 0인 상태를 재전송 가능으로 켜줌
			SignupDraftRedisValue attemptsZeroUpdated = SignupDraftRedisValue.enableResendNowIfPossible(draft);

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
		String draftOtpCodeHash = draft.otp().otpCodeHash();
		boolean match = inputOtpCode.equals(draftOtpCodeHash);

		if (!match) {
			// --------------------------------------------------------
			// 7) 불일치: attempts 감소 + 상태 저장 + nextAction 결정
			// --------------------------------------------------------
			SignupDraftRedisValue failed = draft.onOtpVerifyFailed();
			draftRedisRepository.updatePreserveTtl(failed);

			NextAction nextAction = chooseNextActionAfterFailed(failed);

			return failureResponse(failed, nextAction);
		}

		// ------------------------------------------------------------
		// 8) 일치(성공): PROFILE_REQUIRED로 전이 + 상태 저장
		// ------------------------------------------------------------

		SignupDraftRedisValue verified = draft.onOtpVerified();
		draftRedisRepository.updatePreserveTtl(verified);

		return successResponse(verified);


	}


	private VerifyOtpResponse successResponse(SignupDraftRedisValue draft) {
		// 성공 응답
		// step=PROFILE_REQUIRED
		// flags.requiredFields=["name","birthDate"]

		return new VerifyOtpResponse(
			SignupStep.PROFILE_REQUIRED.name(),
			new VerifyOtpResponse.Flags(
				null,
				null,
				null,
				null,
				NextAction.GO_NEXT_STEP,
				List.of("name", "birthDate")
			)
		);
	}

	private VerifyOtpResponse failureResponse(SignupDraftRedisValue draft, NextAction nextAction) {
		// 실패 응답 스펙:
		// step=OTP_REQUIRED
		// flags: canSendSms/resendAvailableAt/remainingOtpAttempts/otpExpiresAt/nextAction
		return new VerifyOtpResponse(
			SignupStep.OTP_REQUIRED.name(),
			new VerifyOtpResponse.Flags(
				draft.otp().canSendSms(),
				draft.otp().resendAvailableAt(),
				draft.otp().remainingOtpAttempts(),
				draft.otp().otpExpiresAt(),
				nextAction,
				null
			)
		);
	}

	/**
	 * OTP 불일치로 실패 처리 후 nextAction 결정
	 */
	private NextAction chooseNextActionAfterFailed(SignupDraftRedisValue draft) {
		// remainingOtpAttempts > 0 : 아직 입력 재시도 가능
		if (draft.otp().remainingOtpAttempts() > 0) {
			return NextAction.RETRY_OTP;
		}

		// attempts == 0 이 되었으면 재전송으로 유도
		if (draft.otp().remainingResendCount() > 0) {
			// canSendSms=true로 켜져있으면 즉시 재전송 가능
			if (draft.otp().canSendSms()) return NextAction.REQUEST_RESEND_SMS;
			return NextAction.WAIT_RESEND_WINDOW;
		}

		// 재전송도 불가면 가입 재시작
		return NextAction.RESTART_SIGNUP;
	}

	/**
	 * 만료된 경우 nextAction
	 */
	private NextAction chooseNextActionForExpired(SignupDraftRedisValue draft) {
		// 만료되었는데 재전송 횟수가 남아있으면 재전송
		if (draft.otp().remainingResendCount() > 0) {
			if (draft.otp().canSendSms()) return NextAction.REQUEST_RESEND_SMS;
			return NextAction.WAIT_RESEND_WINDOW;
		}
		return NextAction.RESTART_SIGNUP;
	}

	/**
	 * attempts가 0인 경우 nextAction
	 */
	private NextAction chooseNextActionWhenNoAttempts(SignupDraftRedisValue draft) {
		if (draft.otp().remainingResendCount() > 0) {
			if (draft.otp().canSendSms()) return NextAction.REQUEST_RESEND_SMS;
			return NextAction.WAIT_RESEND_WINDOW;
		}
		return NextAction.RESTART_SIGNUP;
	}

}
