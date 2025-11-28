# backend-error-handling-guide.md

# 백엔드 예외 처리 & `ApiResponse` 사용 가이드

> 이 문서는 백엔드 개발자용입니다.
>
>
> 컨트롤러 · 서비스 계층에서 `ApiResponse`, `BusinessException`, `GlobalExceptionHandler`, `ErrorCode`를 어떻게 사용할지 정리합니다.
>

---

## 1. 기본 원칙

1. **컨트롤러는 성공 응답만 직접 만든다.**
    - 성공 시: `ApiResponse.success(...)` 사용
    - 실패 시: 예외를 던지고 글로벌 핸들러에 위임
2. **비즈니스 실패는 서비스/도메인에서 `BusinessException` 또는 `ValidationException`을 던진다.**
    - “기대한 실패”는 모두 ErrorCode 기반 예외로 표현
3. **모든 에러 응답은 `GlobalExceptionHandler`에서 `ApiResponse.error(...)`로 통일한다.**
    - 컨트롤러에서 `try/catch`로 에러 응답을 만들지 않는다.

---

## 2. 핵심 클래스 구조

### 2.1 `ApiResponse<T>`

위치: `com.cotato.itda.global.common.response.ApiResponse`

주요 팩토리 메서드:

```java
public static <T> ApiResponse<T> success(T data);
public static <T> ApiResponse<T> success(T data, HttpStatus status);

public static ApiResponse<Void> error(ErrorCode errorCode, String path);
public static ApiResponse<Void> error(ErrorCode errorCode, String path, Map<String, Object> reasons);
```

내부 필드:

- `boolean success`
- `int status`
- `String code`
- `String message`
- `T data`
- `String path`
- `LocalDateTime timestamp`
- `Map<String, Object> reasons`

> 응답 포맷에 대한 자세한 내용은
>
>
> `docs/api-response-spec.md` 참고.
>

### 2.2 `ErrorCode` 인터페이스

위치: `com.cotato.itda.global.error.constant.ErrorCode`

```java
public interface ErrorCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}

```

구현체:

- `GlobalErrorCode`
- `UserErrorCode`
- `KakaoErrorCode`
- (필요 시 추가 도메인별 ErrorCode Enum)

### 2.3 `BusinessException`

위치: `com.cotato.itda.global.error.exception.BusinessException`

```java
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;
    private final Map<String, Object> reasons;

    public BusinessException(ErrorCode errorCode) {
        this(errorCode, null);
    }

    public BusinessException(ErrorCode errorCode, Map<String, Object> reasons) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.reasons = reasons;
    }
}

```

역할:

- 비즈니스/도메인 계층에서 발생하는 **예상 가능한 실패**를 표현
- `errorCode`를 통해 HTTP 상태, 에러 코드, 메시지 관리
- `reasons`로 상세 정보를 전달할 수 있음

### 2.4 `ValidationException`

위치: `com.cotato.itda.global.error.exception.ValidationException`

```java
public class ValidationException extends BusinessException {

    public ValidationException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ValidationException(ErrorCode errorCode, Map<String, Object> reasons) {
        super(errorCode, reasons);
    }
}

```

역할:

- 도메인/비즈니스 레벨의 유효성 검증 실패를 표현하고 싶을 때 사용
- 지금 구조에서는 `BusinessException`과 동일하게 처리되지만,

  필요하면 별도의 핸들러를 추가해도 됨.


### 2.5 `GlobalExceptionHandler`

위치: `com.cotato.itda.global.error.handler.GlobalExceptionHandler`

주요 핸들러 메서드:

- `@ExceptionHandler(BusinessException.class)`
- `@ExceptionHandler({ MethodArgumentNotValidException.class, BindException.class })`
- `@ExceptionHandler(HttpMessageNotReadableException.class)`
- `@ExceptionHandler(HttpRequestMethodNotSupportedException.class)`
- `@ExceptionHandler(Exception.class)`

각 예외를 `ApiResponse.error(...)`로 변환하여 클라이언트에 응답한다.

---

## 3. 컨트롤러 작성 규칙

### 3.1 성공 응답만 직접 작성

컨트롤러는 **정상 플로우**에만 집중한다.

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(@PathVariable Long id) {
        UserResponse response = userService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
        @Valid @RequestBody UserCreateRequest request
    ) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, HttpStatus.CREATED));
    }
}

```

### 3.2 컨트롤러에서 하지 말 것

- 비즈니스 예외를 `try/catch`로 잡고 직접 응답 만들지 않는다.

    ```java
    // ❌ 이렇게 하지 말 것
    try {
        UserResponse response = userService.getUser(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    } catch (BusinessException e) {
        // 여기서 ApiResponse.error(...)를 만들지 않는다.
    }
    
    ```

- HTTP 상태 코드, message, code를 직접 문자열로 관리하지 않는다.

  → 모든 표준 에러 정보는 `ErrorCode` + `GlobalExceptionHandler`에서 관리한다.


---

## 4. 서비스/도메인에서 예외 던지는 패턴

### 4.1 존재하지 않는 사용자

```java
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new BusinessException(
                UserErrorCode.USER_NOT_FOUND,
                Map.of("userId", id)
            ));

        return new UserResponse(user.getId(), user.getEmail(), user.getNickname());
    }
}

```

### 4.2 이메일 중복 검사

```java
@Transactional
public UserResponse signUp(UserSignUpRequest request) {

    if (userRepository.existsByEmail(request.getEmail())) {
        throw new BusinessException(
            UserErrorCode.DUPLICATE_EMAIL,
            Map.of("email", request.getEmail())
        );
    }

    // 엔티티 생성/저장 로직 ...
}

```

### 4.3 도메인 검증 실패 (ValidationException 사용 예)

```java
if (loginAttemptCount > MAX_LOGIN_ATTEMPT) {
    throw new ValidationException(
        UserErrorCode.LOGIN_ATTEMPT_LIMIT_EXCEEDED,
        Map.of("attemptCount", loginAttemptCount, "maxAttempt", MAX_LOGIN_ATTEMPT)
    );
}

```

> ValidationException은 현재 BusinessException과 동일하게 처리되지만,
>
>
> 나중에 검증 실패만 따로 로깅/모니터링하고 싶다면
>
> `@ExceptionHandler(ValidationException.class)`를 추가하는 방식으로 확장 가능.
>

---

## 5. GlobalExceptionHandler 동작 요약

### 5.1 BusinessException

```java
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

```

- 서비스/도메인에서 `throw new BusinessException(...)`만 하면,

  컨트롤러는 신경 쓰지 않아도 됨.

- 최종 응답 예시는 `api-response-spec.md` 참고.

### 5.2 Bean Validation (@Valid) 검증 실패

```java
@ExceptionHandler({
    MethodArgumentNotValidException.class,
    BindException.class
})
public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(
    Exception ex,
    HttpServletRequest request
)

```

- `BindingResult`에서 `FieldError`를 꺼내 `reasons`에 넣는다.
    - key: 필드명
    - value: 검증 실패 메시지
- 에러 코드는 `GlobalErrorCode.VALIDATION_ERROR`로 통일.

### 5.3 기타 예외

- `HttpMessageNotReadableException`

  → 잘못된 JSON/바디 형식 → `GlobalErrorCode.INVALID_REQUEST`

- `HttpRequestMethodNotSupportedException`

  → 지원하지 않는 HTTP 메서드 → `GlobalErrorCode.INVALID_REQUEST` + `method`를 `reasons`에 포함

- 그 외 모든 `Exception`

  → `GlobalErrorCode.INTERNAL_SERVER_ERROR` (500)


---

## 6. ErrorCode 추가 가이드

1. 적절한 Enum 선택
    - 전역/공통: `GlobalErrorCode`
    - 유저 도메인: `UserErrorCode`
    - 카카오 OAuth/연동: `KakaoErrorCode`
    - 향후 도메인별로 필요 시 Enum 추가 가능 (예: `QuizErrorCode` 등)
2. Enum에 상수 추가 (예시)

```java
DUPLICATE_NICKNAME(
    HttpStatus.BAD_REQUEST,
    "USER_ERROR_400_DUPLICATE_NICKNAME",
    "이미 사용 중인 닉네임입니다."
),

```

1. 서비스/도메인에서 사용

```java
if (userRepository.existsByNickname(request.getNickname())) {
    throw new BusinessException(
        UserErrorCode.DUPLICATE_NICKNAME,
        Map.of("nickname", request.getNickname())
    );
}

```

`ErrorCode`를 추가·수정할 때는 **프론트와 약속된 값**이므로

가능하면 `code` 문자열은 쉽게 바꾸지 않는 것을 원칙으로 한다.

---

## 7. 간단 플로우 예시: 회원 가입

1. 요청 DTO 검증
    - `@Valid @RequestBody UserSignUpRequest`
    - 필수 필드 누락/형식 오류 → `GlobalExceptionHandler`에서 400 + `CLIENT_ERROR_400_VALIDATION_ERROR`
2. 서비스 로직
    - 이메일 중복 → `BusinessException(UserErrorCode.DUPLICATE_EMAIL, ...)`
    - 나머지 로직 진행 후 `UserResponse` 반환
3. 컨트롤러
    - `UserResponse`를 `ApiResponse.success(...)`로 감싸서 반환
4. 응답
    - 성공: 201 + `success=true` + `data=UserResponse`
    - 실패(중복 이메일): 400 + `success=false` + `code=USER_ERROR_400_DUPLICATE_EMAIL`

---

## 8. 요약

- **컨트롤러**
    - 성공: `ApiResponse.success(...)`
    - 실패: 예외 만들지 말고, 그냥 던지기
- **서비스/도메인**
    - 에러 상황: `BusinessException` / `ValidationException` + `ErrorCode` 사용
    - 추가 정보 필요 시 `reasons`에 Map으로 전달
- **예외 처리**
    - 모든 예외는 `GlobalExceptionHandler`를 거쳐 `ApiResponse.error(...)` 형식으로 통일

이 흐름만 지키면,

- API 응답 포맷은 항상 일정하고,
- 프론트는 `success / code / message / reasons`만 보고 판단할 수 있으며,
- 백엔드는 로직/예외를 도메인 관점에서만 고민하면 됩니다.