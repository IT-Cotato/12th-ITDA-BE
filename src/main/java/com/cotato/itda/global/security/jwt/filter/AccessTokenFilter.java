package com.cotato.itda.global.security.jwt.filter;

import java.io.IOException;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.cotato.itda.global.security.jwt.config.JwtPurpose;
import com.cotato.itda.global.security.jwt.extractor.BearerTokenExtractor;
import com.cotato.itda.global.security.jwt.token.JwtTokenProvider;
import com.cotato.itda.global.security.jwt.token.JwtTokenValidator;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * JwtAuthenticationFilter (ACCESS 전용)
 * <p>
 * 역할
 * - 매 요청마다 Authorization 헤더에서 Bearer 토큰을 꺼낸다.
 * - 토큰이 존재하면 "ACCESS 목적" 기준으로 JwtTokenValidator로 검증한다.
 * - 검증 성공 시 Authentication을 생성해 SecurityContext에 저장한다.
 * <p>
 * 설계 의도
 * - Access/Refresh/Onboarding 목적을 섞지 않기 위해
 * 이 필터는 "ACCESS 인증"만 담당한다.
 * - Refresh/Onboarding은 해당 전용 엔드포인트에서 별도 검증한다.
 */
@Component
@RequiredArgsConstructor
public class AccessTokenFilter extends OncePerRequestFilter {

	private final JwtTokenValidator jwtTokenValidator;
	private final JwtTokenProvider jwtTokenProvider;

	/**
	 * Access 인증이 필요 없는 경로는 필터를 스킵한다.
	 * <p>
	 * - 온보딩/리프레시 같은 "특수 목적 토큰" 엔드포인트는
	 * Access 필터에서 먼저 막지 않도록 제외하는 것이 안전하다.
	 */
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String uri = request.getRequestURI();

		// WS 엔드포인트는 필터링 제외
		if (uri.startsWith("/ws")) return true;

		if (uri.startsWith("/api/auth/refresh"))
			return true;
		if (uri.startsWith("/api/onboarding"))
			return true;

		return false;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String token = BearerTokenExtractor.extractOrNull(request.getHeader(HttpHeaders.AUTHORIZATION));

		// 토큰이 없으면 그냥 다음 필터로
		// 토큰이 없으면 SecurityContext에 Authentication이 안 들어가고,
		// 그 상태로 인증이 필요한 엔드포인트에 접근하면
		// Spring Security가 에러를 낸다
		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		/**
		 * 1) ACCESS 목적 기준으로 토큰 검증 + Claims 추출
		 *    - purpose/alg/만료/서명/형식 검증이 여기서 모두 끝난다.
		 *
		 * 2) 검증 성공 시에만 Authentication 생성
		 */
		Claims claims = jwtTokenValidator.validateAndGetClaims(token, JwtPurpose.ACCESS);

		/**
		 *   JwtTokenProvider가 "Claims 기반 Authentication 생성" 오버로드를 제공하는 것.
		 */
		Authentication authentication = jwtTokenProvider.getAuthentication(claims);

		SecurityContextHolder.getContext().setAuthentication(authentication);

		filterChain.doFilter(request, response);
	}

}
