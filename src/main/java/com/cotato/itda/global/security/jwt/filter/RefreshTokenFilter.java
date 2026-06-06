package com.cotato.itda.global.security.jwt.filter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cotato.itda.global.error.constant.JwtErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.error.exception.JwtAuthenticationException;
import com.cotato.itda.global.security.jwt.config.JwtPurpose;
import com.cotato.itda.global.security.jwt.extractor.BearerTokenExtractor;
import com.cotato.itda.global.security.jwt.handler.JwtAuthenticationEntryPoint;
import com.cotato.itda.global.security.jwt.token.JwtTokenValidator;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * RefreshTokenFilter
 * <p>
 * 역할
 * - Refresh 토큰이 필요한 엔드포인트에서만 동작
 * - Authorization 헤더에서 Bearer 토큰을 추출
 * - JwtTokenValidator로 "REFRESH 목적" 기준 검증 + Claims 추출
 * - 검증된 결과를 request attribute로 컨트롤러에 전달
 * <p>
 * 설계 의도
 * - Refresh는 "재발급 전용 토큰"이므로
 * SecurityContext에 Authentication을 심지 않는다.
 * - Access 인증 필터와 책임을 명확히 분리한다.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenFilter extends OncePerRequestFilter {

	public static final String ATTR_REFRESH_CLAIMS = "REFRESH_CLAIMS";
	public static final String ATTR_REFRESH_TOKEN = "REFRESH_TOKEN";

	private final JwtTokenValidator jwtTokenValidator;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();

		// refresh token 검증이 필요한 인증 엔드포인트에서만 필터 동작
		return !uri.startsWith("/api/auth/refresh") && !uri.startsWith("/api/auth/logout");
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String header = request.getHeader(HttpHeaders.AUTHORIZATION);

		try {
			// Bearer 토큰 추출
			String refreshToken = BearerTokenExtractor.extractOrThrow(header);

			// 목적/alg/만료/서명/형식 검증 + Claims 반환
			Claims claims = jwtTokenValidator.validateAndGetClaims(refreshToken, JwtPurpose.REFRESH);

			request.setAttribute(ATTR_REFRESH_TOKEN, refreshToken);
			request.setAttribute(ATTR_REFRESH_CLAIMS, claims);

			filterChain.doFilter(request, response);
		} catch (JwtAuthenticationException e) {
			SecurityContextHolder.clearContext();
			jwtAuthenticationEntryPoint.commence(request, response, e);
			return;
		} catch (BusinessException e) {
			SecurityContextHolder.clearContext();
			JwtErrorCode errorCode = e.getErrorCode() instanceof JwtErrorCode jwtErrorCode
				? jwtErrorCode
				: JwtErrorCode.INVALID_TOKEN;
			jwtAuthenticationEntryPoint.commence(request, response, new JwtAuthenticationException(errorCode));
			return;
		}
	}
}
