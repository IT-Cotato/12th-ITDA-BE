package com.cotato.itda.domain.signup.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.auth.dto.Tokens;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.entity.MemberRole;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.member.repository.MemberTermsConsentRepository;
import com.cotato.itda.domain.signup.dto.SignupDraftRedisValue;
import com.cotato.itda.domain.signup.dto.request.SubmitPasswordRequest;
import com.cotato.itda.domain.signup.dto.response.SubmitPasswordResponse;
import com.cotato.itda.domain.signup.entity.MemberTermsConsentEntity;
import com.cotato.itda.domain.signup.entity.TermsItemEntity;
import com.cotato.itda.domain.signup.model.SignupDraft;
import com.cotato.itda.domain.signup.model.SignupStep;
import com.cotato.itda.domain.signup.repository.SignupDraftRedisRepository;
import com.cotato.itda.domain.signup.repository.TermsItemJpaRepository;
import com.cotato.itda.global.error.constant.SignupErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.token.IssuedAccessToken;
import com.cotato.itda.global.security.jwt.token.IssuedRefreshToken;
import com.cotato.itda.global.security.jwt.token.IssuedToken;
import com.cotato.itda.global.security.jwt.token.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupPasswordService {

	private final SignupDraftRedisRepository signupDraftRedisRepository;
	private final MemberRepository memberRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final PasswordEncoder passwordEncoder;
	private final MemberTermsConsentRepository memberTermsConsentRepository;
	private final TermsItemJpaRepository termsItemJpaRepository;

	@Transactional
	public SubmitPasswordResponse submit(
		String draftKey,
		SubmitPasswordRequest request
	) {
		// 1. Draft 조회
		SignupDraftRedisValue draft = signupDraftRedisRepository.findByDraftKey(draftKey)
			.orElseThrow(() -> new BusinessException(SignupErrorCode.SIGNUP_DRAFT_NOT_FOUND));
		log.info("레디스 에서 조회된 Signup Draft: {}", draft);

		//2. step 검증 + 멱등 처리
		// - 이미 COMPLETED인데 Step6이 재호출 될 수 있다(타임아웃/재시도)
		// - 이 경우, 회원 재생성 하면 안 됨
		// - 대신 기존 회원을 찾아 토큰만 새로 발급해서 내려준다.
		if (draft.step() == SignupStep.COMPLETED) {
			log.info("이미 STEP이 COMPLETED 상태, 멱등 처리로 기존 회원 토큰 재발급");

			String phone = requireOtpPhone(draft);

			Long memberId = memberRepository.findByPhoneNumber(phone)
				.orElseThrow(() -> new BusinessException(SignupErrorCode.MEMBER_NOT_FOUND))
				.getId();

			IssuedToken issuedAccessToken = jwtTokenProvider.createAccessToken(memberId, "ROLE_USER");
			if(issuedAccessToken==null){
				throw new BusinessException(SignupErrorCode.TOKEN_ISSUANCE_FAILED);
			}
			IssuedToken issuedRefreshToken = jwtTokenProvider.createRefreshToken(memberId);
			if(issuedRefreshToken==null){
				throw new BusinessException(SignupErrorCode.TOKEN_ISSUANCE_FAILED);
			}
			return toResponse(memberId, issuedAccessToken, issuedRefreshToken);
		}

		// PASSWORD_REQUIRED 단계인지 검증
		if (draft.step() != SignupStep.PASSWORD_REQUIRED) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		// 3. 이전 단계 완료 여부 재검증
		if (draft.terms() == null || draft.terms().consents() == null) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		if (draft.otp() == null || !draft.otp().phoneVerified()) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		if (draft.profile() == null) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}

		String phone = requireOtpPhone(draft);
		String name = draft.profile().name();
		var birthDate = draft.profile().birthDate();

		// 4. 비밀번호 검증 (재입력 일치 + 정책)
		String password = request.password();
		String passwordConfirm = request.confirmPassword();
		if (!password.equals(passwordConfirm)) {
			throw new BusinessException(SignupErrorCode.PASSWORD_MISMATCH);
		}
		log.info("비밀번호 재입력 일치 검증 통과");
		validatePasswordPolicy(password);

		// 5. 중복 가입 방지 (핸드폰 번호 기준, 이미 가입된 회원인지)
		if (memberRepository.existsByPhoneNumber(phone)) {
			throw new BusinessException(SignupErrorCode.MEMBER_ALREADY_EXISTS);
		}
		log.info("중복 가입 검증 통과, 신규 회원 생성 진행");

		// 6. 비밀번호 해시(BCrypt)
		String passwordHash = passwordEncoder.encode(password);
		log.info("비밀번호 해시 생성 완료 : {}", password);

		try{
			// 7. 회원 생성 및 저장
			Member saved = memberRepository.save(
				Member.createLocalMember(
					phone,
					name,
					birthDate,
					MemberRole.USER,
					null,
					passwordHash
				)
			);
			log.info("신규 회원 생성 및 저장 완료: {}", saved);

			// 번들 기준으로 약관 아이템 전체를 조회
			List<TermsItemEntity> items = termsItemJpaRepository
				.findActiveByBundle(draft.policy().bundleType(), draft.policy().bundleVersion());
			if(items.isEmpty()){
				throw new BusinessException(SignupErrorCode.INVALID_TERMS_BUNDLE_PARAMETERS);
			}
			log.info("약관 항목 조회 완료: {}개", items.size());

			// 8. 약관 동의 기록 저장
			List<MemberTermsConsentEntity> consentEntities = draft.terms().consents().stream()
				.map(consent -> MemberTermsConsentEntity.create(
					saved.getId(),
					consent.id(),
					consent.agreed()
				))
				.toList();
			memberTermsConsentRepository.saveAll(consentEntities);
			log.info("약관 동의 기록 저장 완료: {}개", consentEntities.size());

			// 9. 액세스 토큰/리프레시 토큰 발급
			IssuedToken issuedAccessToken = jwtTokenProvider.createAccessToken(saved.getId(), "ROLE_USER");
			IssuedToken issuedRefreshToken = jwtTokenProvider.createRefreshToken(saved.getId());
			log.info("액세스 토큰 및 리프레시 토큰 발급 완료");
			log.info("Access Token: {}", ((IssuedAccessToken)issuedAccessToken).token());
			log.info("Refresh Token: {}", ((IssuedRefreshToken)issuedRefreshToken).token());

			// 10.  Draft COMPLETED로 전이(비밀번호는 저장하지 않음)
			SignupDraftRedisValue completed = draft.onSignupCompleted();
			log.info("Signup Draft 상태 COMPLETED로 전이 완료");
			signupDraftRedisRepository.updatePreserveTtl(completed);
			log.info("COMPLETED 상태 Draft Redis 업데이트 완료");
			return toResponse(saved.getId(), issuedAccessToken, issuedRefreshToken);


		}catch(DataIntegrityViolationException e){
			// 동시성 이슈로 인해 중복 가입이 발생한 경우
			throw new BusinessException(SignupErrorCode.MEMBER_ALREADY_EXISTS);
		}

	}


	private SubmitPasswordResponse toResponse(Long memberId, IssuedToken issuedAccessToken,
		IssuedToken issuedRefreshToken) {
		return new SubmitPasswordResponse(
			SignupStep.COMPLETED.name(),
			memberId,
			new Tokens(
				((IssuedAccessToken)issuedAccessToken).token(),
				((IssuedRefreshToken)issuedRefreshToken).token(),
				((IssuedAccessToken)issuedAccessToken).expiresAt(),
				((IssuedRefreshToken)issuedRefreshToken).expiresAt()
			)
		);
	}

	public String requireOtpPhone(SignupDraftRedisValue draft) {
		if (draft.otp() == null || draft.otp().phone() == null || draft.otp().phone().isBlank()) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		return draft.otp().phone();
	}

	/**
	 * 비밀번호 정책 검증 메서드
	 *
	 * [정책 요약]
	 * 1) null 불가
	 * 2) 길이: 8 ~ 64
	 * 3) 공백 포함 불가
	 * 4) 문자 종류(영문/숫자/특수문자) 중 최소 2종 이상 포함
	 *
	 * [주의]
	 * - 여기서 "문자"는 Character::isLetter 기준이라 한글도 letter로 잡힐 수 있음
	 *   (정확히 영문만 허용하려면 별도 정규식/범위 체크가 필요)
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
		if (hasLetter) kinds++;
		if (hasDigit) kinds++;
		if (hasSpecial) kinds++;

		// 최소 2종 미만이면 정책 위반
		if (kinds < 2) {
			throw new BusinessException(SignupErrorCode.INVALID_PASSWORD_POLICY);
		}

		// 여기까지 오면 정책 통과(예외 발생 없음)
	}
}
