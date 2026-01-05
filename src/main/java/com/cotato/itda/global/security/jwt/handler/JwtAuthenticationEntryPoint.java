package com.cotato.itda.global.security.jwt.handler;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.error.constant.GlobalErrorCode;
import com.cotato.itda.global.error.exception.JwtAuthenticationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JwtAuthenticationEntryPoint
 * <p>
 * - 인증되지 않은 사용자가 보호된 자원에 접근했을 때 동작하는 진입점(EntryPoint)
 * - 예: 토큰이 없거나, 토큰이 완전 깨졌거나, 시큐리티가 현재 사용자를 Anonymous로 판단하는 경우
 * - 이런 상황에서 401 Unauthorized + JSON 에러 응답을 내려준다.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	/**
	 * - 인증이 필요한 리소스에 대해, 현재 인증 정보가 없거나 실패했을 때 호출되는 메서드
	 * - Spring Security 필터 체인에서 AuthenticationException이 발생하면 이 메서드가 트리거 된다.
	 *
	 * @param request       현재 요청 정보 (URL, 헤더 등)
	 * @param response      클라이언트에게 돌려줄 HTTP 응답 객체
	 * @param authException 발생한 인증 예외 (자격 증명 실패 / 미인증 상태를 나타냄)
	 */
	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException
	) throws IOException {

		log.warn("[UNAUTHORIZED] uri={}, message={}",
			request.getRequestURI(), authException.getMessage());

		ApiResponse<Void> errorResponse;

		if (authException instanceof JwtAuthenticationException jwtEx) {
			errorResponse = ApiResponse.error(jwtEx.getErrorCode(), request.getRequestURI());
		} else {
			errorResponse = ApiResponse.error(GlobalErrorCode.UNAUTHORIZED, request.getRequestURI());
		}

		response.setStatus(GlobalErrorCode.UNAUTHORIZED.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}