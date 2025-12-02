package com.cotato.itda.global.error.constant;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KakaoErrorCode implements ErrorCode {

	// 400 Bad Request
	INVALID_KAKAO_AUTHORIZATION_CODE(
		HttpStatus.BAD_REQUEST,
		"유효하지 않은 카카오 인가 코드입니다.",
		"KAKAO_ERROR_400_INVALID_AUTHORIZATION_CODE"
	),

	MISSING_KAKAO_USER_ID(
		HttpStatus.BAD_REQUEST,
		"카카오에서 사용자 식별 정보를 반환하지 않았습니다.",
		"KAKAO_ERROR_400_MISSING_USER_ID"
	),

	MISSING_REQUIRED_KAKAO_PROFILE(
		HttpStatus.BAD_REQUEST,
		"카카오에서 필수 프로필 정보를 반환하지 않았습니다.",
		"KAKAO_ERROR_400_MISSING_REQUIRED_PROFILE"
	),

	// 401 Unauthorized
	INVALID_KAKAO_ACCESS_TOKEN(
		HttpStatus.UNAUTHORIZED,
		"유효하지 않은 카카오 액세스 토큰입니다.",
		"KAKAO_ERROR_401_INVALID_ACCESS_TOKEN"
	),

	EXPIRED_KAKAO_ACCESS_TOKEN(
		HttpStatus.UNAUTHORIZED,
		"만료된 카카오 액세스 토큰입니다.",
		"KAKAO_ERROR_401_EXPIRED_ACCESS_TOKEN"
	),

	// 409 Conflict
	KAKAO_ACCOUNT_ALREADY_LINKED(
		HttpStatus.CONFLICT,
		"이미 다른 계정에 연동된 카카오 계정입니다.",
		"KAKAO_ERROR_409_ACCOUNT_ALREADY_LINKED"
	),

	// 502 Bad Gateway (Upstream Kakao 장애)
	KAKAO_TOKEN_REQUEST_FAILED(
		HttpStatus.BAD_GATEWAY,
		"카카오 토큰 발급 요청 중 오류가 발생했습니다.",
		"KAKAO_ERROR_502_TOKEN_REQUEST_FAILED"
	),

	KAKAO_USERINFO_REQUEST_FAILED(
		HttpStatus.BAD_GATEWAY,
		"카카오 사용자 정보 조회 중 오류가 발생했습니다.",
		"KAKAO_ERROR_502_USERINFO_REQUEST_FAILED"
	),

	// 503 Service Unavailable
	KAKAO_SERVICE_UNAVAILABLE(
		HttpStatus.SERVICE_UNAVAILABLE,
		"카카오 서비스가 일시적으로 응답하지 않습니다.",
		"KAKAO_ERROR_503_SERVICE_UNAVAILABLE"
	);

	private final HttpStatus httpStatus;
	private final String message;
	private final String code;
}
