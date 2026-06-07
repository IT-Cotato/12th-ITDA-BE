package com.cotato.itda.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cotato.itda.domain.auth.dto.LoginRequest;
import com.cotato.itda.domain.auth.dto.Tokens;
import com.cotato.itda.domain.member.config.MemberWithdrawalProperties;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.entity.MemberStatus;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.AuthErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.token.IssuedAccessToken;
import com.cotato.itda.global.security.jwt.token.IssuedRefreshToken;
import com.cotato.itda.global.security.jwt.token.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private MemberWithdrawalProperties memberWithdrawalProperties;

	@InjectMocks
	private AuthService authService;

	@Test
	void login_rejects_withdrawn_member() {
		LoginRequest request = new LoginRequest("01012345678", "password");
		Member member = Member.builder()
			.id(1L)
			.phoneNumber("01012345678")
			.status(MemberStatus.WITHDRAWN)
			.passwordHash("encoded-password")
			.build();

		when(memberRepository.findByPhoneNumber("01012345678")).thenReturn(Optional.of(member));

		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);

		verify(passwordEncoder, never()).matches("password", "encoded-password");
		verify(jwtTokenProvider, never()).createAccessToken(1L, "ROLE_USER");
	}

	@Test
	void login_restores_withdrawal_pending_member_within_grace_period() {
		LoginRequest request = new LoginRequest("01012345678", "password");
		Member member = Member.builder()
			.id(1L)
			.phoneNumber("01012345678")
			.status(MemberStatus.WITHDRAWAL_PENDING)
			.withdrawnAt(OffsetDateTime.now().minusDays(3))
			.passwordHash("encoded-password")
			.build();
		OffsetDateTime accessExpiresAt = OffsetDateTime.now().plusHours(1);
		OffsetDateTime refreshExpiresAt = OffsetDateTime.now().plusDays(14);

		when(memberRepository.findByPhoneNumber("01012345678")).thenReturn(Optional.of(member));
		when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
		when(memberWithdrawalProperties.getGracePeriodDays()).thenReturn(7);
		when(jwtTokenProvider.createAccessToken(1L, "ROLE_USER"))
			.thenReturn(new IssuedAccessToken("access-token", accessExpiresAt));
		when(jwtTokenProvider.createRefreshToken(1L))
			.thenReturn(new IssuedRefreshToken("refresh-token", refreshExpiresAt));

		Tokens tokens = authService.login(request);

		assertThat(member.getStatus()).isEqualTo(MemberStatus.ACTIVE);
		assertThat(member.getWithdrawnAt()).isNull();
		assertThat(tokens.accessToken().accessToken()).isEqualTo("access-token");
		assertThat(tokens.refreshToken().refreshToken()).isEqualTo("refresh-token");
	}

	@Test
	void login_rejects_withdrawal_pending_member_after_grace_period() {
		LoginRequest request = new LoginRequest("01012345678", "password");
		Member member = Member.builder()
			.id(1L)
			.phoneNumber("01012345678")
			.status(MemberStatus.WITHDRAWAL_PENDING)
			.withdrawnAt(OffsetDateTime.now().minusDays(8))
			.passwordHash("encoded-password")
			.build();

		when(memberRepository.findByPhoneNumber("01012345678")).thenReturn(Optional.of(member));
		when(passwordEncoder.matches("password", "encoded-password")).thenReturn(true);
		when(memberWithdrawalProperties.getGracePeriodDays()).thenReturn(7);

		assertThatThrownBy(() -> authService.login(request))
			.isInstanceOf(BusinessException.class)
			.extracting("errorCode")
			.isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);

		assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWAL_PENDING);
		verify(jwtTokenProvider, never()).createAccessToken(1L, "ROLE_USER");
	}
}
