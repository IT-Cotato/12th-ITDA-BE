package com.cotato.itda.domain.auth.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.auth.dto.Tokens;
import com.cotato.itda.global.error.constant.JwtErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.token.IssuedToken;
import com.cotato.itda.global.security.jwt.token.JwtTokenProvider;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenReissueService {
	private final JwtTokenProvider jwtTokenProvider;
	// 만약 실제 회원 상태 검증이 필요하면 DB 확인
	// private final MemberRepository memberRepository;

	@Transactional(readOnly = true)
	public Tokens.AccessTokenOnly reissue(Claims refreshClaims) {

		// sub에서 사용자 식별자(memberId) 추출
		String subject = refreshClaims.getSubject();
		if (subject == null || subject.isBlank()) {
			throw new BusinessException(JwtErrorCode.INVALID_TOKEN);
		}
		log.info("리프레시 토큰에서 추출한 subject: {}", subject);
		Long memberId;

		// subject(String) -> memberId(Long) 파싱
		try {
			memberId = Long.parseLong(subject);
		} catch (NumberFormatException e) {
			throw new BusinessException(JwtErrorCode.INVALID_TOKEN);
		}
		log.info("리프레시 토큰에서 추출한 memberId: {}", memberId);
		// 만약 사용자 상태 검증할 시 사용( 탈퇴/정지/휴면 여부 확인 등)
		// Member member = memberRepository.findById(memberId)
		// 	.orElseThrow(()-> BaseException.from(MemberErrorCode.MEMBER_NOT_FOUND));

		// 새 Access Token 발급
		IssuedToken issuedAccessToken = jwtTokenProvider.createAccessToken(memberId, "ROLE_USER");
		log.info("새로 발급한 액세스 토큰: {}", issuedAccessToken.token());
		return new Tokens.AccessTokenOnly(
			issuedAccessToken.token(),
			issuedAccessToken.expiresAt()
		);
	}
}
