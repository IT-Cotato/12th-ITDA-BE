package com.cotato.itda.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.auth.service.LogoutService;
import com.cotato.itda.domain.member.dto.WithdrawRequest;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.entity.MemberStatus;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.JwtErrorCode;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.config.JwtPurpose;
import com.cotato.itda.global.security.jwt.token.JwtTokenValidator;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final JwtTokenValidator jwtTokenValidator;
	private final LogoutService logoutService;

	@Transactional
	public void withdraw(Long memberId, WithdrawRequest request) {
		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
		if (member.getStatus() != MemberStatus.ACTIVE) {
			throw new BusinessException(UserErrorCode.WITHDRAWN_USER);
		}

		Claims refreshClaims = jwtTokenValidator.validateAndGetClaims(request.refreshToken(), JwtPurpose.REFRESH);
		Long refreshMemberId = parseMemberId(refreshClaims);
		if (!memberId.equals(refreshMemberId)) {
			throw new BusinessException(JwtErrorCode.INVALID_TOKEN);
		}

		member.withdraw();
		logoutService.logout(request.refreshToken(), refreshClaims);
	}

	private Long parseMemberId(Claims refreshClaims) {
		String subject = refreshClaims.getSubject();
		if (subject == null || subject.isBlank()) {
			throw new BusinessException(JwtErrorCode.INVALID_TOKEN);
		}

		try {
			return Long.parseLong(subject);
		} catch (NumberFormatException e) {
			throw new BusinessException(JwtErrorCode.INVALID_TOKEN);
		}
	}
}
