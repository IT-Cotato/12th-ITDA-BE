package com.cotato.itda.domain.auth.controller;

import static com.cotato.itda.global.security.jwt.filter.RefreshTokenFilter.*;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.itda.domain.auth.dto.LoginRequest;
import com.cotato.itda.domain.auth.dto.Tokens;
import com.cotato.itda.domain.auth.service.AuthService;
import com.cotato.itda.domain.auth.service.TokenReissueService;
import com.cotato.itda.global.common.response.ApiResponse;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증(로그인/토큰) API")
public class AuthController {

	private final AuthService authService;
	private final TokenReissueService tokenReissueService;

	@Operation(
		summary = "로그인",
		description = "전화번호/비밀번호로 로그인하고 Access/Refresh 토큰을 발급한다.",
		security = {}
	)
	@io.swagger.v3.oas.annotations.responses.ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "로그인 성공"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "401",
			description = "전화번호 또는 비밀번호가 올바르지 않음"
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "요청 값 검증 실패(예: phoneNumber, password 누락)"
		)
	})
	@PostMapping("/login")
	public ApiResponse<Tokens> login(
		@RequestBody @Valid LoginRequest loginRequest
	) {
		Tokens tokens = authService.login(loginRequest);
		return ApiResponse.success(tokens);
	}

	@Operation(
		summary = "Access 토큰 재발급",
		description = "Refresh 토큰을 검증해 Access(및 필요 시 Refresh) 토큰을 재발급한다."
	)
	@SecurityRequirement(name = "RefreshToken")
	@PostMapping("/refresh")
	public ApiResponse<Tokens.AccessTokenOnly> refresh(
		@Parameter(hidden = true)
		@RequestAttribute(ATTR_REFRESH_CLAIMS) Claims refreshClaims
	) {
		Tokens.AccessTokenOnly response = tokenReissueService.reissue(refreshClaims);
		return ApiResponse.success(response);
	}
}