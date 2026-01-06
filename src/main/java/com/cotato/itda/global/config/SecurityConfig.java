package com.cotato.itda.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.cotato.itda.global.security.jwt.filter.AccessTokenFilter;
import com.cotato.itda.global.security.jwt.filter.RefreshTokenFilter;
import com.cotato.itda.global.security.jwt.handler.JwtAccessDeniedHandler;
import com.cotato.itda.global.security.jwt.handler.JwtAuthenticationEntryPoint;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	private final AccessTokenFilter accessTokenFilter;
	private final RefreshTokenFilter refreshTokenFilter;
	private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
	private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

	/**
	 * HttpSecurity를 사용해서 애플리케이션의 보안 규칙을 정의하는 메서드
	 * 여기서 리턴하는 FilterChain이 모든 요청에 대해 적용된다.
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource,
		RefreshTokenFilter refreshTokenFilter,
		AccessTokenFilter accessTokenFilter) throws Exception {

		http
			// 1. CSRF 설정
			// - REST API + JWT 조합에서는 보통 세션을 쓰지 않고, 브라우저 기반 폼도 안 쓰기 때문에 CSRF를 꺼버린다.
			.csrf(csrf -> csrf.disable())
			// 2️. CORS 설정
			// - 프론트 도메인과 백엔드 도메인이 다를 때(CORS 이슈) 기본 설정을 사용.
			// - 필요하면 별도 CorsConfigurationSource 빈으로 상세 설정 가능.
			.cors(cors -> cors.configurationSource(corsConfigurationSource))
			// 3️. 세션 관리 전략
			// - STATELESS: 스프링 시큐리티가 HttpSession을 사용해서 로그인 상태를 저장하지 않음
			// - 매 요청마다 JWT로부터 다시 인증 정보를 세팅하는 방식(JWT + REST API에 맞는 전략)
			.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
			)

			// formLogin, httpBasic, logout 비활성화
			.formLogin(AbstractHttpConfigurer::disable)
			.httpBasic(AbstractHttpConfigurer::disable)
			.logout(AbstractHttpConfigurer::disable)

			// 4. URL 별 인가(authorization) 규칙
			.authorizeHttpRequests(auth -> auth
				//인증 없이 누구나 접근 가능한 URL 패턴
				.requestMatchers(
					"/api/signup/**",
					"/api/auth/login",
					"/api/auth/refresh",
					"/swagger-ui/**",
					"/api/onboarding/**",
					"/v3/api-docs/**"
				).permitAll()
				//관리자 전용 URL 패턴
				//                        .requestMatchers(
				//                                "/api/admin/**"
				//                        ).hasRole("ADMIN")
				//위에서 명시한 것 외의 모든 요청은 인증 필요
				.anyRequest().authenticated()
			)
			// 5. 예외 처리 설정
			.exceptionHandling(ex -> ex
				// 인증되지 않은 사용자가 보호된 자원에 접근했을 때 -> 401 Unauthorized 응답
				// (예: JWT 없음, 완전 깨진 JWT, SecurityContext에 Authentication이 없는 경우)
				.authenticationEntryPoint(jwtAuthenticationEntryPoint)
				// 인증은 되었지만 권한이 부족할 때 -> 403 Forbidden 응답
				// (예: ROLE_USER로 로그인했는데, ADMIN 전용 API에 접근할 때)
				.accessDeniedHandler(jwtAccessDeniedHandler)
			)
			// 6. JWT 필터 등록
			// - UsernamePasswordAuthenticationFilter(폼 로그인 처리)보다 앞에 JWT 필터를 배치
			// - 요청이 들어오면 먼저 JwtAuthenticationFilter가 Authorization 헤더에서 JWT를 파싱하고,
			//   토큰이 유효하면 SecurityContext에 Authentication을 심어준다.
			.addFilterBefore(accessTokenFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterBefore(refreshTokenFilter, UsernamePasswordAuthenticationFilter.class);

		// 7. 최종 FilterChain 객체를 빌드해서 스프링에 등록
		return http.build();
	}
}
