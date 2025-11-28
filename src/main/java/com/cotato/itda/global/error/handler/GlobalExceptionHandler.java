package com.cotato.itda.global.error.handler;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.error.constant.GlobalErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * 비즈니스 예외 (도메인/서비스 계층에서 명시적으로 던지는 예외)
	 */
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<Void>> handleBusinessException(
		BusinessException ex,
		HttpServletRequest request
	) {
		log.warn("[BusinessException] path={}, code={}, message={}",
			request.getRequestURI(),
			ex.getErrorCode().getCode(),
			ex.getMessage()
		);

		ApiResponse<Void> body = ApiResponse.error(
			ex.getErrorCode(),
			request.getRequestURI(),
			ex.getReasons()
		);

		return ResponseEntity
			.status(ex.getErrorCode().getHttpStatus())
			.body(body);
	}

	/**
	 * Bean Validation(@Valid) 실패 – RequestBody, PathVariable, RequestParam 바인딩 에러 처리 핸들러이다.
	 * Spring의  @Valid 또는 @Validated 어노테이션을 사용한 요청 객체(DTO)의
	 * 유효성 검증 실패 시 발생하는 예외들을 포착한다.
	 */
	@ExceptionHandler({
		MethodArgumentNotValidException.class, //  @RequestBody 유효성 검증 실패 시 발생 (주로 POST/PUT)
		BindException.class                   //  @ModelAttribute 또는 @PathVariable, @RequestParam 유효성 검증 실패 시 발생
	})
	public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
		Exception ex,
		HttpServletRequest request
	) {
		// 1. 유효성 검증 오류의 상세 정보를 담고 있는 BindingResult 객체를 추출하기 위해 초기화한다.
		BindingResult bindingResult = null;

		// 2. 발생한 예외가 MethodArgumentNotValidException (RequestBody 검증 오류)인 경우
		if (ex instanceof MethodArgumentNotValidException manve) {
			// 예외 객체에서 BindingResult를 가져온다. BindingResult에 모든 필드 오류 정보가 담겨 있다.
			bindingResult = manve.getBindingResult();
		}
		// 3. 발생한 예외가 BindException (다른 타입 바인딩/검증 오류)인 경우
		else if (ex instanceof BindException be) {
			// BindException 객체에서 BindingResult를 가져온다.
			bindingResult = be.getBindingResult();
		}

		// 4. 클라이언트에게 전달할 상세 오류 정보 (reasons)를 저장할 Map을 초기화한다.
		Map<String, Object> reasons = new HashMap<>();

		// 5. BindingResult가 정상적으로 추출되었으면 (유효성 검증 오류가 실제로 발생했다면)
		if (bindingResult != null) {
			// 6. BindingResult에 담긴 모든 FieldError를 순회한다.
			for (FieldError fieldError : bindingResult.getFieldErrors()) {
				// 7. reasons Map에 '필드명'을 Key로, '오류 메시지'를 Value로 저장한다.
				//    예) {"username": "사용자 이름은 필수 항목입니다."}
				reasons.put(fieldError.getField(), fieldError.getDefaultMessage());
			}
		}

		// 8. 서버 로그에 요청 경로와 추출된 상세 오류 정보(reasons)를 기록한다.
		log.warn("[Validation] path={}, reasons={}", request.getRequestURI(), reasons);

		// 9. GlobalErrorCode.VALIDATION_ERROR 정보를 바탕으로 ApiResponse<Void> 에러 바디를 생성한다.
		ApiResponse<Void> body = ApiResponse.error(
			GlobalErrorCode.VALIDATION_ERROR, // 서비스 표준 유효성 에러 코드 (HTTP 400 Bad Request)
			request.getRequestURI(),
			reasons // 상세 오류 정보 Map을 reasons 필드에 포함시킨다.
		);

		// 10. HTTP 상태 코드와 에러 바디를 담은 ResponseEntity를 최종 반환한다.
		return ResponseEntity
			.status(GlobalErrorCode.VALIDATION_ERROR.getHttpStatus())
			.body(body);
	}

	/**
	 * JSON 파싱 실패 (바디 형식이 이상한 경우)
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException ex,
		HttpServletRequest request
	) {
		log.warn("[HttpMessageNotReadable] path={}, message={}",
			request.getRequestURI(),
			ex.getMessage()
		);

		ApiResponse<Void> body = ApiResponse.error(
			GlobalErrorCode.INVALID_REQUEST,
			request.getRequestURI()
		);

		return ResponseEntity
			.status(GlobalErrorCode.INVALID_REQUEST.getHttpStatus())
			.body(body);
	}

	/**
	 * 지원하지 않는 HTTP 메서드
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(
		HttpRequestMethodNotSupportedException ex,
		HttpServletRequest request
	) {
		log.warn("[MethodNotSupported] path={}, method={}",
			request.getRequestURI(),
			ex.getMethod()
		);

		ApiResponse<Void> body = ApiResponse.error(
			GlobalErrorCode.INVALID_REQUEST,
			request.getRequestURI(),
			Map.of("method", ex.getMethod())
		);

		return ResponseEntity
			.status(GlobalErrorCode.INVALID_REQUEST.getHttpStatus())
			.body(body);
	}


	/**
	 * 그 외 처리하지 못한 모든 예외에 대한 마지막 방어선
	 */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<Void>> handleException(
		Exception ex,
		HttpServletRequest request
	) {
		log.error("[UnhandledException] path=" + request.getRequestURI(), ex);

		ApiResponse<Void> body = ApiResponse.error(
			GlobalErrorCode.INTERNAL_SERVER_ERROR,
			request.getRequestURI()
		);

		return ResponseEntity
			.status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(body);
	}
}
