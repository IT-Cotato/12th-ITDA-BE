package com.cotato.itda.global.error.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

	// ========================
	// 400 Bad Request (요청 자체가 잘못된 경우)
	// ========================

	/**
	 * JSON 형식은 맞지만, 비즈니스 규칙상 성립하지 않는 요청일 때 사용.
	 * 예) 퀴즈 마감 시간이 이미 지났는데 제출을 시도, 범위를 벗어난 숫자 요청 등
	 */
	INVALID_REQUEST(
		HttpStatus.BAD_REQUEST,
		"CLIENT_ERROR_400_INVALID_REQUEST",
		"유효하지 않은 요청입니다."
	),

	/**
	 * @Valid, Bean Validation, 바인딩 오류 등 필드 단위 검증 실패.
	 * 예) 필수 필드 누락, 이메일 형식 아님, 문자열 길이 초과 등
	 */
	VALIDATION_ERROR(
		HttpStatus.BAD_REQUEST,
		"CLIENT_ERROR_400_VALIDATION_ERROR",
		"요청 값이 유효하지 않습니다."
	),

	// ========================
	// 401 Unauthorized (인증 문제)
	// ========================

	/**
	 * 인증 자체가 안 된 상태.
	 * 예) Authorization 헤더 없음, 로그인 안 된 사용자가 보호된 API 호출
	 */
	UNAUTHORIZED(
		HttpStatus.UNAUTHORIZED,
		"CLIENT_ERROR_401_UNAUTHORIZED",
		"인증이 필요합니다."
	),

	/**
	 * 토큰 구조/서명/파싱에 문제가 있는 경우.
	 * 예) Bearer 토큰이 아닌 문자열, 서명 검증 실패, 잘못된 형식의 JWT 등
	 */
	INVALID_TOKEN(
		HttpStatus.UNAUTHORIZED,
		"CLIENT_ERROR_401_INVALID_TOKEN",
		"유효하지 않은 인증 토큰입니다."
	),

	/**
	 * exp가 지난 JWT 토큰을 사용했을 때.
	 * 예) 이미 만료된 액세스 토큰으로 API 호출
	 */
	EXPIRED_TOKEN(
		HttpStatus.UNAUTHORIZED,
		"CLIENT_ERROR_401_EXPIRED_TOKEN",
		"만료된 인증 토큰입니다."
	),

	// ========================
	// 403 Forbidden (인가/권한 문제)
	// ========================

	/**
	 * 인증은 되었지만, 권한이 부족한 경우.
	 * 예) 본인이 아닌 다른 사용자의 리소스에 접근, ROLE_USER로 관리자 API 호출
	 */
	FORBIDDEN(
		HttpStatus.FORBIDDEN,
		"CLIENT_ERROR_403_FORBIDDEN",
		"접근 권한이 없습니다."
	),

	// ========================
	// 404 Not Found (리소스 없음)
	// ========================

	/**
	 * 요청한 리소스가 존재하지 않을 때 사용하는 기본 404.
	 * 예) 없는 placeId로 장소 조회, 탈퇴한 회원 조회 등
	 */
	NOT_FOUND(
		HttpStatus.NOT_FOUND,
		"CLIENT_ERROR_404_NOT_FOUND",
		"요청한 리소스를 찾을 수 없습니다."
	),

	// ========================
	// 409 Conflict (상태 충돌)
	// ========================

	/**
	 * 이미 동일한 리소스가 있을 때.
	 * 예) 이미 존재하는 이메일로 회원가입, 같은 장소 이름을 중복 등록 시도 등
	 */
	DUPLICATE_RESOURCE(
		HttpStatus.CONFLICT,
		"CLIENT_ERROR_409_DUPLICATE_RESOURCE",
		"이미 존재하는 리소스입니다."
	),

	// ========================
	// 429 Too Many Requests (요청 과다)
	// ========================

	/**
	 * Rate Limit, Flooding 방지 등으로 요청을 제한할 때.
	 * 예) 짧은 시간 내에 로그인 시도/외부 API 호출을 과도하게 반복
	 */
	TOO_MANY_REQUESTS(
		HttpStatus.TOO_MANY_REQUESTS,
		"CLIENT_ERROR_429_TOO_MANY_REQUESTS",
		"요청이 너무 많습니다."
	),

	// ========================
	// 5xx Server Errors (서버/인프라/외부 의존성 문제)
	// ========================

	/**
	 * 예상하지 못한 서버 내부 예외의 기본값.
	 * 예) NullPointerException, 처리하지 않은 런타임 예외 등
	 */
	INTERNAL_SERVER_ERROR(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"SERVER_ERROR_500_INTERNAL_SERVER_ERROR",
		"서버 내부 오류입니다."
	),

	/**
	 * DB 쿼리/연결 등 데이터베이스 관련 예외.
	 * 예) Unique 제약 위반, FK 제약 위반, DB 연결 끊김 등
	 */
	DATABASE_ERROR(
		HttpStatus.INTERNAL_SERVER_ERROR,
		"SERVER_ERROR_500_DATABASE_ERROR",
		"데이터베이스 처리 중 오류가 발생했습니다."
	),

	/**
	 * OpenWeather, Kakao 등 외부 API 호출에 실패했을 때.
	 * 예) 외부 서버 5xx, 타임아웃, 응답 포맷 깨짐, 네트워크 장애 등
	 */
	EXTERNAL_API_ERROR(
		HttpStatus.BAD_GATEWAY,
		"SERVER_ERROR_502_EXTERNAL_API_ERROR",
		"외부 API 통신 오류입니다."
	);

	/**
	 * HTTP 상태 코드 (예: 400, 401, 403, 404, 500...)
	 * 클라이언트/게이트웨이에서 공통적으로 이해하는 표준 상태 코드.
	 */
	private final HttpStatus httpStatus;

	/**
	 * 우리 서비스 내부에서 쓰는 식별용 에러 코드.
	 * - 로그/모니터링/프론트 분기에서 사용
	 * - message는 나중에 바뀌어도 되지만, code는 되도록 안정적으로 유지
	 */
	private final String code;

	/**
	 * 사용자/클라이언트에게 노출할 메시지.
	 * - 기본 한국어 설명
	 * - 나중에 i18n(다국어) 적용 시 이 필드를 locale에 맞게 변환해서 내려줄 수 있음
	 */
	private final String message;
}
