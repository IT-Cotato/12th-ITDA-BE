# api-response-spec.md

# API 공통 응답 스펙 (`ApiResponse<T>`)

> 프론트엔드 · 백엔드 공통으로 사용하는 단일 응답 포맷에 대한 문서입니다.
>
>
> 모든 내부 API는 `ApiResponse<T>` 형태의 JSON으로 응답하는 것을 원칙으로 합니다.
>

---

## 1. 공통 응답 구조

### 1.1 JSON 구조

백엔드에서 사용하는 공통 응답 DTO는 아래와 같은 구조를 갖습니다.
- 성공 시에는 200 OK 상태 고정합니다.

```json
{
  "success": true,
  "status": 200,
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 1,
    "email": "gimin1463@naver.com",
    "nickname": "기민"
  },
  "path": null,
  "timestamp": "2025-11-28T11:23:45.123",
  "reasons": null
}

```

### 1.2 필드 설명

- `success` (boolean)
    - 요청 처리 성공 여부.
    - `true` : 비즈니스 로직이 정상 처리된 경우
    - `false` : 비즈니스 예외 / 검증 실패 / 시스템 예외 등 실패한 경우
- `status` (number)
    - HTTP 상태 코드 숫자 값.
    - 예: `200`, `400`, `401`, `403`, `404`, `500` 등.
- `code` (string)
    - 우리 서비스에서 정의한 에러/상태 코드 문자열.
    - 성공 응답: `"SUCCESS"`
    - 실패 응답: `ErrorCode` Enum에 정의된 값
        - 예: `CLIENT_ERROR_400_VALIDATION_ERROR`
        - 예: `USER_ERROR_404_NOT_FOUND`
        - 예: `KAKAO_ERROR_502_USERINFO_REQUEST_FAILED`
- `message` (string)
    - 사용자/클라이언트에게 노출할 메시지.
    - 성공: `"요청이 성공적으로 처리되었습니다."` (또는 필요한 경우 별도 메시지)
    - 실패: 에러 상황을 설명하는 한국어 문구 (ErrorCode에서 정의)
- `data` (object or null)
    - 실제 비즈니스 데이터 페이로드.
    - 성공 응답에서만 사용되며, 도메인별 DTO가 이 위치에 들어간다.
    - 실패(`success = false`)인 경우에는 항상 `null`.
- `path` (string or null)
    - 요청 경로(URI).
    - 성공 응답에서는 null이며, 에러 응답에서는 요청 URL를 포함한다.
    - 예: `/api/users/1`, `/api/auth/kakao/login`
    - 주로 에러 응답에서 어떤 요청에서 에러가 났는지 디버깅용으로 활용.
- `timestamp` (string)
    - 응답이 생성된 시각.
    - 서버 기준 `LocalDateTime`을 ISO-8601 포맷 문자열로 직렬화한 값.
- `reasons` (object or null)
    - 에러의 추가 상세 정보.
    - 검증 실패, 도메인별 실패 원인 등을 key–value 형태로 담는다.
    - 예시:
        - `{"email": "이메일 형식이 올바르지 않습니다."}`
        - `{"userId": 123}`
        - `{"attemptCount": 6, "maxAttempt": 5}`

---

## 2. 성공 응답 예시

### 성공 응답은 모두 200 OK 상태이며, `success`가 `true`로 설정됩니다.

```json
{
  "success": true,
  "status": 200,
  "code": "SUCCESS",
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": {
    "id": 1,
    "email": "gimin1463@naver.com",
    "nickname": "기민"
  },
  "path": null,
  "timestamp": "2025-11-28T11:23:45.123",
  "reasons": null
}

```

---

## 3. 실패 응답 예시

### 3.1 비즈니스 에러 (존재하지 않는 사용자)

```json
{
  "success": false,
  "status": 404,
  "code": "USER_ERROR_404_NOT_FOUND",
  "message": "사용자를 찾을 수 없습니다.",
  "data": null,
  "path": "/api/users/999",
  "timestamp": "2025-11-28T11:35:00.000",
  "reasons": {
    "userId": 999
  }
}

```

- `code`는 백엔드의 `UserErrorCode.USER_NOT_FOUND`에서 온 값.
- `reasons`에 어떤 userId로 실패했는지 추가 정보 제공.

### 3.2 Bean Validation (@Valid) 검증 실패

```json
{
  "success": false,
  "status": 400,
  "code": "CLIENT_ERROR_400_VALIDATION_ERROR",
  "message": "요청 값이 유효하지 않습니다.",
  "data": null,
  "path": "/api/users",
  "timestamp": "2025-11-28T11:40:00.000",
  "reasons": {
    "email": "이메일 형식이 올바르지 않습니다.",
    "nickname": "닉네임은 필수입니다."
  }
}

```

- DTO에 선언한 `@NotBlank`, `@Email` 등의 메시지가 `reasons`에 매핑된다.
- `reasons`의 key = 필드명, value = 에러 메시지.

### 3.3 카카오 로그인/연동 에러

```json
{
  "success": false,
  "status": 502,
  "code": "KAKAO_ERROR_502_USERINFO_REQUEST_FAILED",
  "message": "카카오 사용자 정보 조회 중 오류가 발생했습니다.",
  "data": null,
  "path": "/api/auth/kakao/login",
  "timestamp": "2025-11-28T11:45:00.000",
  "reasons": {
    "kakaoError": "server_error"
  }
}

```

---

## 4. 참고

- 공통 응답 DTO

  `com.cotato.itda.global.common.response.ApiResponse`

- 에러 코드

  `com.cotato.itda.global.error.constant.GlobalErrorCode`

  `com.cotato.itda.global.error.constant.UserErrorCode`

  `com.cotato.itda.global.error.constant.KakaoErrorCode`

- 예외 처리 핸들러

  `com.cotato.itda.global.error.handler.GlobalExceptionHandler`


백엔드 내부 구현 및 예외 처리 패턴은

`docs/backend-error-handling-guide.md` 문서를 참고하세요.