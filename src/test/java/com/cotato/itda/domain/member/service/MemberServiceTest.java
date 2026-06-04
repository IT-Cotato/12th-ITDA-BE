package com.cotato.itda.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cotato.itda.domain.auth.service.LogoutService;
import com.cotato.itda.domain.member.dto.WithdrawRequest;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.entity.MemberStatus;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.JwtErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.config.JwtPurpose;
import com.cotato.itda.global.security.jwt.token.JwtTokenValidator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private JwtTokenValidator jwtTokenValidator;

	@Mock
	private LogoutService logoutService;

	@InjectMocks
	private MemberService memberService;

	@Test
	void withdraw_marks_member_as_withdrawn_and_blacklists_refresh_token() {
		String refreshToken = "refresh-token";
		WithdrawRequest request = new WithdrawRequest(refreshToken);
		Member member = Member.builder()
			.id(1L)
			.status(MemberStatus.ACTIVE)
			.build();
		Claims claims = claims("1");

		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(jwtTokenValidator.validateAndGetClaims(refreshToken, JwtPurpose.REFRESH)).thenReturn(claims);

		memberService.withdraw(1L, request);

		assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
		verify(logoutService).logout(refreshToken, claims);
	}

	@Test
	void withdraw_rejects_refresh_token_of_another_member() {
		String refreshToken = "refresh-token";
		WithdrawRequest request = new WithdrawRequest(refreshToken);
		Member member = Member.builder()
			.id(1L)
			.status(MemberStatus.ACTIVE)
			.build();
		Claims claims = claims("2");

		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
		when(jwtTokenValidator.validateAndGetClaims(refreshToken, JwtPurpose.REFRESH)).thenReturn(claims);

		assertThatThrownBy(() -> memberService.withdraw(1L, request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(JwtErrorCode.INVALID_TOKEN);

		assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
		verify(logoutService, never()).logout(refreshToken, claims);
	}

	private Claims claims(String subject) {
		return Jwts.claims()
			.subject(subject)
			.build();
	}
}
