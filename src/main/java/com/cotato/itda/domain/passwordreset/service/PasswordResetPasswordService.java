package com.cotato.itda.domain.passwordreset.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.passwordreset.dto.PasswordResetDraftRedisValue;
import com.cotato.itda.domain.passwordreset.dto.PasswordResetStep;
import com.cotato.itda.domain.passwordreset.repository.PasswordResetDraftRedisRepository;
import com.cotato.itda.domain.signup.dto.request.SubmitPasswordRequest;
import com.cotato.itda.global.error.constant.PasswordResetErrorCode;
import com.cotato.itda.global.error.constant.SignupErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetPasswordService {

	private final PasswordResetDraftRedisRepository passwordResetDraftRedisRepository;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public Void resetPassword(
		String draftKey,
		SubmitPasswordRequest request
	) {
		PasswordResetDraftRedisValue draft = passwordResetDraftRedisRepository.findByDraftKey(draftKey)
			.orElseThrow(()-> new BusinessException(PasswordResetErrorCode.PASSWORD_RESET_DRAFT_NOT_FOUND));
		log.info("레디스 에서 조회된 Password Reset Draft: {}", draft);

		if(draft.step() == PasswordResetStep.PASSWORD_RESET_COMPLETED) {
			log.info("이미 STEP이 COMPLETED 상태, 멱등 처리로 종료");
			return null;
		}
		//2. step 검증
		if (draft.step() != PasswordResetStep.PASSWORD_REQUIRED) {
			throw new BusinessException(PasswordResetErrorCode.INVALID_PASSWORD_RESET_STEP);
		}
		String phone = requireOtpPhone(draft);

		String password = request.password();
		String passwordConfirm = request.confirmPassword();
		if (!password.equals(passwordConfirm)) {
			throw new BusinessException(PasswordResetErrorCode.PASSWORD_MISMATCH);
		}
		log.info("비밀번호 일치 확인 완료");
		validatePasswordPolicy(password);

		if(!memberRepository.existsByPhoneNumber(phone)) {
			throw new BusinessException(PasswordResetErrorCode.MEMBER_NOT_FOUND);
		}
		log.info("회원 존재 확인 완료");

		String passwordHash  = passwordEncoder.encode(password);
		log.info("비밀번호 해시 생성 완료");

		try{
			memberRepository.updatePasswordByPhoneNumber(passwordHash,phone);
			log.info("비밀번호 재설정 완료");
			PasswordResetDraftRedisValue completed = draft.onPasswordResetCompleted();
			log.info("Password Reset Draft 상태 PASSWORD_RESET_COMPLETED로 전이 시도");
			passwordResetDraftRedisRepository.updatePreserveTtl(completed);
			log.info("Password Reset Draft 상태 PASSWORD_RESET_COMPLETED로 전이 완료");

		} catch(Exception e) {
			log.error("비밀번호 재설정 중 오류 발생: {}", e.getMessage());
			throw new BusinessException(PasswordResetErrorCode.PASSWORD_RESET_FAILED);
		}
		return null;
	}

	public String requireOtpPhone(PasswordResetDraftRedisValue draft) {
		if (draft.otpState() == null || draft.otpState().phone() == null || draft.otpState().phone().isBlank()) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		return draft.otpState().phone();
	}

	/**
	 * 비밀번호 정책 검증 메서드
	 * <p>
	 * [정책 요약]
	 * 1) null 불가
	 * 2) 길이: 8 ~ 64
	 * 3) 공백 포함 불가
	 * 4) 문자 종류(영문/숫자/특수문자) 중 최소 2종 이상 포함
	 * <p>
	 * [주의]
	 * - 여기서 "문자"는 Character::isLetter 기준이라 한글도 letter로 잡힐 수 있음
	 * (정확히 영문만 허용하려면 별도 정규식/범위 체크가 필요)
	 */
	private void validatePasswordPolicy(String password) {

		// =========================
		// 1) null 체크
		// =========================
		// - 회원가입/비밀번호 설정에서 password가 null이면 검증 자체가 불가능
		// - 정책 위반으로 동일한 에러코드(INVALID_PASSWORD_POLICY)를 던짐
		if (password == null) {
			throw new BusinessException(SignupErrorCode.INVALID_PASSWORD_POLICY);
		}

		// =========================
		// 2) 길이 제한 체크 (6~64)
		// =========================
		int len = password.length();
		if (len < 6 || len > 64) {
			throw new BusinessException(SignupErrorCode.INVALID_PASSWORD_POLICY);
		}

		// =========================
		// 3) 공백 포함 여부 체크
		// =========================
		// - 공백이 들어가면 사용자가 "보이는 문자열"과 "실제 입력"이 달라질 수 있어
		//   로그인 실패/혼란을 유발하기 쉽다.
		// - 그래서 애초에 공백을 금지하는 정책
		if (password.contains(" ")) {
			throw new BusinessException(SignupErrorCode.INVALID_PASSWORD_POLICY);
		}

		// =========================
		// 4) 문자 종류(영문/숫자/특수문자) 포함 여부 체크
		// =========================

		// 4-1) 영문(ASCII) 포함 여부
		// - ASCII 영문자(A~Z, a~z)만 true로 인정하도록 범위를 체크한다.
		boolean hasLetter = password.chars().anyMatch(ch ->
			(ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z')
		);

		// 4-2) ASCII 숫자만 포함 여부
		// - 여기서는 '0'~'9' 사이 문자가 하나라도 있는지 확인한다.
		boolean hasDigit = password.chars().anyMatch(ch -> ch >= '0' && ch <= '9');

		// 4-3) 특수문자 포함 여부
		boolean hasSpecial = password.chars().anyMatch(
			ch -> "!@#$%^&*()_+-=[]{};':\",.<>/?\\|`~".indexOf(ch) >= 0
		);

		// =========================
		// 5) "종류" 카운트 후 최소 2종 이상 요구
		// =========================
		// - hasLetter/hasDigit/hasSpecial 중 true인 개수를 센다.
		// - 예: 영문만 있으면 1종 → 정책 위반
		// - 예: 영문+숫자 있으면 2종 → 통과
		// - 예: 숫자+특수문자 있으면 2종 → 통과
		int kinds = 0;
		if (hasLetter)
			kinds++;
		if (hasDigit)
			kinds++;
		if (hasSpecial)
			kinds++;

		// 최소 2종 미만이면 정책 위반
		if (kinds < 2) {
			throw new BusinessException(SignupErrorCode.INVALID_PASSWORD_POLICY);
		}

		// 여기까지 오면 정책 통과(예외 발생 없음)
	}



}
