package com.cotato.itda.global.common.response;
package com.cotato.itda.global.common.response;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpStatus;

import com.cotato.itda.global.error.constant.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

	/**
	 * 요청 성공 여부
	 * - true  : 비즈니스 로직이 정상 처리된 경우
	 * - false : 비즈니스 예외나 시스템 예외 등으로 실패한 경우
	 */
	private final boolean success;

	/**
	 * HTTP 상태 코드 숫자 값 (200, 400, 401, 500 등)
	 */
	private final int status;

	/**
	 * 우리 서비스의 에러 코드
	 * - 예) USER_ERROR_404_NOT_FOUND
	 * - 성공 응답의 경우 "SUCCESS"
	 */
	private final String code;

	/**
	 * 사용자/클라이언트에게 노출할 메시지
	 * - 예외 상황 설명, 성공 메시지 등
	 */
	private final String message;

	/**
	 * 실제 비즈니스 데이터
	 * - 성공 응답에서만 세팅
	 */
	private final T data;

	/**
	 * 요청 경로 (URI) – 주로 에러 응답에 포함
	 * - HTTP 요청이 발생한 URI를 기록한다.
	 * - 예) `/api/v1/users/100`
	 * - 에러 응답 시, 클라이언트가 어떤 요청 경로에서 에러가 발생했는지 명확하게 파악하도록 돕는다.
	 */
	private final String path;

	/**
	 * 응답 시각
	 */
	private final LocalDateTime timestamp;

	/**
	 * 추가 상세 정보 (필드 검증 오류, 도메인별 이유 등)
	 * - key: 필드명, 상황 코드 등
	 * - value: 상세 메시지 또는 값
	 * - 주로 에러 응답에서 사용되며, 세부적인 에러 발생 이유를 제공한다.
	 * - 예시 1) 필드 검증 오류 (Validation Error):
	 * - key: "username", value: "사용자 이름은 5자 이상이어야 합니다."
	 * - key: "password", value: "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다."
	 * - 예시 2) 도메인별 상세 오류:
	 * - key: "account_status", value: "정지된 계정으로는 작업을 수행할 수 없습니다."
	 */
	private final Map<String, Object> reasons;

	private ApiResponse(
		boolean success,
		int status,
		String code,
		String message,
		T data,
		String path,
		LocalDateTime timestamp,
		Map<String, Object> reasons
	) {
		this.success = success;
		this.status = status;
		this.code = code;
		this.message = message;
		this.data = data;
		this.path = path;
		this.timestamp = timestamp;
		this.reasons = reasons;
	}

	// ==========================
	// 성공 응답용 팩토리 메서드
	// ==========================

	/**
	 * 기본 200 OK 성공 응답
	 */
	public static <T> ApiResponse<T> success(T data) {
		return success(data, HttpStatus.OK);
	}

	/**
	 * 상태 코드를 지정하는 성공 응답
	 * - 예) 생성 시 201 Created
	 */
	public static <T> ApiResponse<T> success(T data, HttpStatus status) {
		return new ApiResponse<>(
			true,
			status.value(),
			"SUCCESS",
			"요청이 성공적으로 처리되었습니다.",
			data,
			null,          // path는 필요시 컨트롤러/핸들러에서 넣어도 됨
			LocalDateTime.now(),
			null
		);
	}

	// ==========================
	// 에러 응답용 팩토리 메서드
	// ==========================

	public static ApiResponse<Void> error(ErrorCode errorCode, String path) {
		return error(errorCode, path, null);
	}

	public static ApiResponse<Void> error(
		ErrorCode errorCode,
		String path,
		Map<String, Object> reasons
	) {
		return new ApiResponse<>(
			false,
			errorCode.getHttpStatus().value(),
			errorCode.getCode(),
			errorCode.getMessage(),
			null,                 // 실패이므로 data 없음
			path,
			LocalDateTime.now(),
			reasons
		);
	}
}
