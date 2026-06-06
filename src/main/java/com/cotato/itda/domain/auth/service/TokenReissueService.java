package com.cotato.itda.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.auth.dto.Tokens;
import com.cotato.itda.domain.auth.repository.RefreshTokenBlacklistRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.entity.MemberStatus;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.JwtErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.token.IssuedToken;
import com.cotato.itda.global.security.jwt.token.JwtSubjectParser;
import com.cotato.itda.global.security.jwt.token.JwtTokenProvider;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenReissueService {
	private final JwtTokenProvider jwtTokenProvider;
	private final RefreshTokenBlacklistRepository refreshTokenBlacklistRepository;
	private final MemberRepository memberRepository;
	private final JwtSubjectParser jwtSubjectParser;

	@Transactional(readOnly = true)
	public Tokens.AccessTokenOnly reissue(String refreshToken, Claims refreshClaims) {
		if (refreshTokenBlacklistRepository.exists(refreshToken)) {
			throw new BusinessException(JwtErrorCode.LOGGED_OUT_TOKEN);
		}

		Long memberId = jwtSubjectParser.parseMemberId(refreshClaims);
		log.info("리프레시 토큰에서 추출한 memberId: {}", memberId);

		Member member = memberRepository.findById(memberId)
			.orElseThrow(() -> new BusinessException(JwtErrorCode.INVALID_TOKEN));
		if (member.getStatus() != MemberStatus.ACTIVE) {
			throw new BusinessException(JwtErrorCode.INVALID_TOKEN);
		}

		// 새 Access Token 발급
		IssuedToken issuedAccessToken = jwtTokenProvider.createAccessToken(memberId, "ROLE_USER");
		log.info("새로 발급한 액세스 토큰: {}", issuedAccessToken.token());
		return new Tokens.AccessTokenOnly(
			issuedAccessToken.token(),
			issuedAccessToken.expiresAt()
		);
	}
}
