package com.cotato.itda.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.itda.domain.member.dto.WithdrawRequest;
import com.cotato.itda.domain.member.service.MemberService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
@Tag(name = "Member", description = "멤버(사용자) 서비스 API")
public class MemberController {

	private final MemberService memberService;

	@SecurityRequirement(name = "AccessToken")
	@GetMapping("/health")
	@Operation(summary = "멤버 서비스 헬스체크", description = "멤버 서비스가 정상 작동 중인지 확인합니다."
		+ "accessToken 필요")
	public String healthCheck(@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal) {
		Long memberId = jwtPrincipal.memberId();
		log.info("멤버 서비스 헬스체크 요청, memberId: {}", memberId);
		return "멤버 서비스 정상 작동 중";
	}

	@SecurityRequirement(name = "AccessToken")
	@DeleteMapping("/me")
	@Operation(summary = "회원 탈퇴", description = "현재 로그인한 회원을 탈퇴 처리하고 refresh token 재사용을 차단합니다.")
	public ApiResponse<Void> withdraw(
		@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
		@RequestBody @Valid WithdrawRequest request
	) {
		memberService.withdraw(jwtPrincipal.memberId(), request);
		return ApiResponse.success(null);
	}
}
