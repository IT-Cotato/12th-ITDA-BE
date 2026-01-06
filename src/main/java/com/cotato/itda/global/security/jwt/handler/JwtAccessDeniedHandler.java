package com.cotato.itda.global.security.jwt.handler;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.error.constant.GlobalErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAccessDeniedHandler implements AccessDeniedHandler {
	// 스프링이 관리하는 ObjectMapper 빈을 주입
	// - 전역 설정(날짜 포맷, 직렬화 설정 등)을 그대로 따라감
	//  spring-boot-starter-web을 의존성에 넣으면
	//  스프링 부트가 내부에서 Jackson 관련 자동 설정을 켜고, ObjectMapper, Jaskson2ObjectMapperBuilder 같은 걸 자동으로 Bean 등록해줌
	private final ObjectMapper objectMapper;

	/**
	 * 권한이 부족해서(AccessDeniedException) 403이 발생했을 때 호출되는 메서드
	 *
	 * @param request  : 현재 들어온 HTTP 요청 정보
	 * @param response : 우리가 작성해서 돌려줄 HTTP 응답 객체
	 * @param ex       : 발생한 AccessDeniedException (권한 부족 예외)
	 *                 <p>
	 *                 사용자가 JWT로 인증된 상태(Authentication 있음)
	 * @PreAuthorize("hasRole('ADMIN')") 같은 권한 조건을 만족 못하면 AccessDeniedException 발생
	 * <p>
	 * ExceptionTranslationFilter가 이 예외를 잡아서
	 * AccessDeniedHandler에게 넘김
	 * <p>
	 * 우리가 등록해 둔 JwtAccessDeniedHandler가 호출됨
	 * 로그 남기고, 403 상태 코드 설정, JSON 형태의 에러 응답 바디를 내려줌
	 * <p>
	 * 반대로 아예 인증이 안 된 상태(=익명 사용자)에서 보호된 리소스에 접근하면
	 * AccessDeniedHandler가 아니라 AuthenticationEntryPoint가 호출돼서
	 * 401 Unauthorized 응답을 내려줌
	 */
	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException ex
	) throws IOException {

		//  누가/어디로 요청하다가 권한 거부가 되었는지 파악하기 위한 로그
		log.warn("[ACCESS DENIED] uri={}, message={}", request.getRequestURI(), ex.getMessage());

		// 클라이언트에게 내려줄 에러 응답 바디 생성
		ApiResponse<Void> errorResponse = ApiResponse.error(GlobalErrorCode.FORBIDDEN, request.getRequestURI());
		// HTTP 응답 상태코드 설정
		// 403 Forbidden (인증은 됐지만, 이 리소스에 접근할 권한이 없음)
		response.setStatus(GlobalErrorCode.FORBIDDEN.getHttpStatus().value());

		//응답 헤더 설정
		// Content-Type: application/json
		// 캐릭터셋: UTF-8
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		// 에러 DTO를 JSON 문자열로 직렬화해서 HTTP 응답 바디에 써주기
		// - objectMapper.writeValueAsString(errorResponse) -> {"status":403, "code":"FORBIDDEN", ...}
		// - response.getWriter()를 통해 바디에 JSON 텍스트를 직접 작성
		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}
