package com.cotato.itda.domain.member.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/member")
@Tag(name = "Member", description = "멤버(사용자) 서비스 API")
public class MemberController {

	@SecurityRequirement(name = "AccessToken")
	@GetMapping("/health")
	@Operation(summary = "멤버 서비스 헬스체크", description = "멤버 서비스가 정상 작동 중인지 확인합니다."
		+ "accessToken 필요")
	public String healthCheck(@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal) {
		Long memberId = jwtPrincipal.memberId();
		log.info("멤버 서비스 헬스체크 요청, memberId: {}", memberId);
		return "멤버 서비스 정상 작동 중";
	}

}
