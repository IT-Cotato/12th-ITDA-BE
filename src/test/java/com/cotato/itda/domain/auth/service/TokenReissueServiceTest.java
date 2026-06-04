package com.cotato.itda.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cotato.itda.domain.auth.dto.Tokens;
import com.cotato.itda.domain.auth.repository.RefreshTokenBlacklistRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.entity.MemberStatus;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.JwtErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.token.IssuedAccessToken;
import com.cotato.itda.global.security.jwt.token.JwtTokenProvider;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class TokenReissueServiceTest {

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private RefreshTokenBlacklistRepository refreshTokenBlacklistRepository;

	@Mock
	private MemberRepository memberRepository;

	@InjectMocks
	private TokenReissueService tokenReissueService;

	@Test
	void reissue_rejects_blacklisted_refresh_token() {
		String refreshToken = "refresh-token";
		Claims claims = claims("1");
		when(refreshTokenBlacklistRepository.exists(refreshToken)).thenReturn(true);

		assertThatThrownBy(() -> tokenReissueService.reissue(refreshToken, claims))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(JwtErrorCode.LOGGED_OUT_TOKEN);

		verify(memberRepository, never()).findById(1L);
		verify(jwtTokenProvider, never()).createAccessToken(anyLong(), anyString());
	}

	@Test
	void reissue_issues_access_token_for_active_member() {
		String refreshToken = "refresh-token";
		Claims claims = claims("1");
		OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(1);
		Member member = Member.builder()
			.id(1L)
			.status(MemberStatus.ACTIVE)
			.build();

		when(refreshTokenBlacklistRepository.exists(refreshToken)).thenReturn(false);
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(jwtTokenProvider.createAccessToken(1L, "ROLE_USER"))
			.thenReturn(new IssuedAccessToken("new-access-token", expiresAt));

		Tokens.AccessTokenOnly response = tokenReissueService.reissue(refreshToken, claims);

		assertThat(response.accessToken()).isEqualTo("new-access-token");
		assertThat(response.accessTokenExpiresAt()).isEqualTo(expiresAt);
	}

	@Test
	void reissue_rejects_withdrawn_member() {
		String refreshToken = "refresh-token";
		Claims claims = claims("1");
		Member member = Member.builder()
			.id(1L)
			.status(MemberStatus.WITHDRAWN)
			.build();

		when(refreshTokenBlacklistRepository.exists(refreshToken)).thenReturn(false);
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

		assertThatThrownBy(() -> tokenReissueService.reissue(refreshToken, claims))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(JwtErrorCode.INVALID_TOKEN);

		verify(jwtTokenProvider, never()).createAccessToken(1L, "ROLE_USER");
	}

	private Claims claims(String subject) {
		return Jwts.claims()
			.subject(subject)
			.build();
	}
}
