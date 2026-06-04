package com.cotato.itda.domain.auth.service;

import java.time.OffsetDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.auth.dto.LoginRequest;
import com.cotato.itda.domain.auth.dto.Tokens;
import com.cotato.itda.domain.member.config.MemberWithdrawalProperties;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.entity.MemberStatus;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.AuthErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.token.IssuedToken;
import com.cotato.itda.global.security.jwt.token.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenProvider jwtTokenProvider;
	private final MemberWithdrawalProperties memberWithdrawalProperties;

	@Transactional
	public Tokens login(LoginRequest request) {
		// 1. 전화번호 검증
		log.info("입력받은 전화번호: {}", request.phoneNumber());
		String phoneNumber = normalizePhone(request.phoneNumber());
		log.info("정규화된 전화번호: {}", phoneNumber);
		validatePhone(phoneNumber);
		log.info("전화번호 검증 통과");

		// 2. 전화번호(유니크)로 DB에서 유저 조회
		Member savedMember = memberRepository.findByPhoneNumber(phoneNumber)
			.orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));
		log.info("전화번호로 Member 조회 성공");
		if (savedMember.getStatus() != MemberStatus.ACTIVE
			&& savedMember.getStatus() != MemberStatus.WITHDRAWAL_PENDING) {
			throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
		}

		// 3. DB에서 조회한 유저의 해시 변환된 비밀번호와 비교
		String inputPassword = request.password();
		String storedPasswordHash = savedMember.getPasswordHash();
		boolean ok = passwordEncoder.matches(inputPassword, storedPasswordHash);
		if (!ok) {
			throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
		}
		log.info("입력받은 비밀번호와 DB저장된 비밀번호 검증 통과");

		if (savedMember.getStatus() == MemberStatus.WITHDRAWAL_PENDING) {
			if (isWithdrawalGracePeriodExpired(savedMember)) {
				throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
			}
			savedMember.restore();
			log.info("탈퇴 유예 기간 내 로그인으로 회원 복구 완료: memberId={}", savedMember.getId());
		}

		// 4. access token 생성
		IssuedToken issuedAccessToken = jwtTokenProvider.createAccessToken(savedMember.getId(), "ROLE_USER");
		// 5. refresh token 생성
		IssuedToken issuedRefreshToken = jwtTokenProvider.createRefreshToken(savedMember.getId());
		log.info("액세스 토큰 및 리프레시 토큰 발급 완료");
		// 6. 응답 반환
		return new Tokens(
			new Tokens.AccessTokenOnly(
				issuedAccessToken.token(),
				issuedAccessToken.expiresAt()
			),
			new Tokens.RefreshTokenOnly(
				issuedRefreshToken.token(),
				issuedRefreshToken.expiresAt()
			)
		);
	}

	private boolean isWithdrawalGracePeriodExpired(Member member) {
		if (member.getWithdrawnAt() == null) {
			return true;
		}

		OffsetDateTime expiresAt = member.getWithdrawnAt()
			.plusDays(memberWithdrawalProperties.getGracePeriodDays());
		return !OffsetDateTime.now().isBefore(expiresAt);
	}

	private String normalizePhone(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.isBlank()) {
			throw new BusinessException(AuthErrorCode.INVALID_PHONE_NUMBER);
		}
		String normalizePhoneNumber = phoneNumber.replaceAll("[^0-9]", "");

		return normalizePhoneNumber;
	}

	private void validatePhone(String phoneNumber) {
		if (phoneNumber == null || phoneNumber.isBlank()) {
			throw new BusinessException(AuthErrorCode.INVALID_PHONE_NUMBER);
		}
		if (phoneNumber.length() < 10 || phoneNumber.length() > 11) {
			throw new BusinessException(AuthErrorCode.INVALID_PHONE_NUMBER);
		}

		// ^ : 문자열 시작
		// 010 : 문자 그대로 010
		// \\d{8} : 숫자(\d)가 정확히 8개
		// - 자바 문자열 안에서는 \d를 쓰려면 \\d처럼 백슬래시를 한 번 더 써야함
		// $ : 문자열 끝
		if (!phoneNumber.matches("^010\\d{8}$")) {
			throw new BusinessException(AuthErrorCode.INVALID_PHONE_NUMBER);
		}
	}
}
