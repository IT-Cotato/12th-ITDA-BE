package com.cotato.itda.domain.auth.service;

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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cotato.itda.domain.auth.dto.LoginRequest;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.entity.MemberStatus;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.AuthErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.token.JwtTokenProvider;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

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
}
