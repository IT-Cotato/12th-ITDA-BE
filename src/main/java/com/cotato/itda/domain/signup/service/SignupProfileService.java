package com.cotato.itda.domain.signup.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.cotato.itda.domain.signup.dto.SignupDraftRedisValue;
import com.cotato.itda.domain.signup.dto.request.SubmitProfileRequest;
import com.cotato.itda.domain.signup.dto.response.SubmitProfileResponse;
import com.cotato.itda.domain.signup.model.SignupStep;
import com.cotato.itda.domain.signup.repository.SignupDraftRedisRepository;
import com.cotato.itda.global.error.exception.BusinessException;

import com.cotato.itda.global.error.constant.SignupErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SignupProfileService {

	private final SignupDraftRedisRepository draftRedisRepository;

	/**
	 * STEP5) 프로필 제출 처리
	 * <p>
	 * 처리 순서
	 * 1) draftKey로 Draft 조회
	 * 2) step 검증 (PROFILE_REQUIRED에서만 허용)
	 * 3) OTP 검증이 완료된 상태인지 확인(otp.phoneVerified == true)
	 * 4) 입력값 정규화/검증 (name trim, birthDate 미래 날짜 방지 등)
	 * 5) Draft 전이: draft.onProfileSubmitted(name, birthDate)
	 * 6) Redis 저장(TTL 보존)
	 * 7) 응답: step=PASSWORD_REQUIRED
	 */
	public SubmitProfileResponse submit(
		String draftKey,
		SubmitProfileRequest request
	) {
		// 1) Draft 조회
		SignupDraftRedisValue draft = draftRedisRepository.findByDraftKey(draftKey)
			.orElseThrow(() -> new BusinessException(SignupErrorCode.SIGNUP_DRAFT_NOT_FOUND));

		// 2) step 검증 + 멱등 처리

		// - 사용자가 버튼을 두 번 누르거나
		// - 클라이언트가 타임아웃으로 같은 요청을 재전송하면
		// - 같은 결과를 내려줌
		if (draft.step() == SignupStep.PASSWORD_REQUIRED) {
			// 이미 Step5가 성공한 상태이므로 "성공 응답"을 그대로 반환
			return new SubmitProfileResponse(SignupStep.PASSWORD_REQUIRED.name());
		}

		// Step5는 PROFILE_REQUIRED에서만 허용
		if (draft.step() != SignupStep.PROFILE_REQUIRED) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}

		// 3) OTP 검증 완료 상태인지 확인
		// Step4 성공을 거쳐야 PROFILE_REQUIRED로 오지만,
		// Draft가 비정상 조작/손상된 경우를 대비해 서버에서 다시 확인한다.
		if (draft.otp() == null) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		// OTP 인증이 안 된 상태에서 프로필 제출이 들어오면 안 됨
		if (!draft.otp().phoneVerified()) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		// phoneVerified=true인데 verifiedAt=null이면 Draft 데이터가 깨짐
		if (draft.otp().verifiedAt() == null) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}

		// 4) 입력값 정규화/검증
		String normalizedName = normalizeName(request.name());
		LocalDate birthDate = request.birthDate();

		validateName(normalizedName);
		validateBirthDate(birthDate);

		// 5) Draft 전이 (PROFILE_REQUIRED -> PASSWORD_REQUIRED)
		SignupDraftRedisValue updated = draft.onProfileSubmitted(normalizedName, birthDate);

		// 6) Redis 저장 (TTL 보존)
		draftRedisRepository.updatePreserveTtl(updated);

		// 7) 응답
		return new SubmitProfileResponse(updated.step().name());
	}

	// 입력값 검증/정규화 helpers

	private String normalizeName(String raw) {
		// - 앞뒤 공백 제거
		return raw == null ? null : raw.trim();
	}

	private void validateName(String name) {
		if (name == null || name.isBlank()) {
			throw new BusinessException(SignupErrorCode.INVALID_PROFILE_NAME);
		}

		// 길이 제한: 1~30자
		if (name.length() < 1 || name.length() > 30) {
			throw new BusinessException(SignupErrorCode.INVALID_PROFILE_NAME);
		}
	}

	private void validateBirthDate(LocalDate birthDate) {
		// 미래 생일 방지
		if (birthDate.isAfter(LocalDate.now())) {
			throw new BusinessException(SignupErrorCode.INVALID_PROFILE_BIRTHDATE);
		}
	}
}
