package com.cotato.itda.global.security.jwt.filter;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.entity.MemberStatus;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.JwtErrorCode;
import com.cotato.itda.global.error.exception.JwtAuthenticationException;
import com.cotato.itda.global.security.jwt.config.JwtPurpose;
import com.cotato.itda.global.security.jwt.handler.JwtAuthenticationEntryPoint;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import com.cotato.itda.global.security.jwt.token.JwtTokenProvider;
import com.cotato.itda.global.security.jwt.token.JwtTokenValidator;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class AccessTokenFilterTest {

	@Mock
	private JwtTokenValidator jwtTokenValidator;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Mock
	private FilterChain filterChain;

	@InjectMocks
	private AccessTokenFilter accessTokenFilter;

	@Test
	void doFilterInternal_rejects_access_token_when_member_is_not_active() throws Exception {
		String accessToken = "access-token";
		Claims claims = Jwts.claims()
			.subject("1")
			.build();
		JwtPrincipal principal = new JwtPrincipal(1L, "1", "ACCESS");
		Member member = Member.builder()
			.id(1L)
			.status(MemberStatus.WITHDRAWAL_PENDING)
			.build();
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/member/health");
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

		when(jwtTokenValidator.validateAndGetClaims(accessToken, JwtPurpose.ACCESS)).thenReturn(claims);
		when(jwtTokenProvider.getAuthentication(claims))
			.thenReturn(new UsernamePasswordAuthenticationToken(principal, null));
		when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

		accessTokenFilter.doFilterInternal(request, response, filterChain);

		verify(filterChain, never()).doFilter(request, response);
		verify(jwtAuthenticationEntryPoint).commence(
			org.mockito.ArgumentMatchers.eq(request),
			org.mockito.ArgumentMatchers.eq(response),
			org.mockito.ArgumentMatchers.argThat(exception ->
				exception instanceof JwtAuthenticationException jwtException
					&& jwtException.getErrorCode() == JwtErrorCode.INVALID_TOKEN
			)
		);
	}
}
