package com.cotato.itda.domain.notification.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.itda.domain.notification.dto.NotificationListResponse;
import com.cotato.itda.domain.notification.dto.UnreadNotificationCountResponse;
import com.cotato.itda.domain.notification.service.command.NotificationCommandService;
import com.cotato.itda.domain.notification.service.query.NotificationQueryService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "인앱 알림 API")
public class NotificationController {

	private final NotificationQueryService notificationQueryService;
	private final NotificationCommandService notificationCommandService;

	@Operation(
		summary = "내 알림 목록 조회",
		description = """
			로그인한 사용자의 인앱 알림 목록을 최신순으로 조회합니다.
			- 첫 조회: lastId 없이 요청합니다.
			- 추가 조회: 응답의 hasNext가 true인 경우 lastId를 다음 요청에 포함합니다.
			"""
	)
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 파라미터")
	})
	@SecurityRequirement(name = "AccessToken")
	@GetMapping
	public ApiResponse<NotificationListResponse> getNotifications(
		@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,

		@Parameter(description = "직전 조회 응답의 lastId 값 (첫 조회 요청 시 null)", example = "10")
		@RequestParam(required = false) Long lastId,

		@Parameter(description = "조회할 알림 개수 (기본 20, 최대 50)", example = "20")
		@RequestParam(required = false, defaultValue = "20")
		@Min(value = 1, message = "limit은 1 이상이어야 합니다.")
		@Max(value = 50, message = "limit은 50 이하이어야 합니다.")
		int limit
	) {
		NotificationListResponse response = notificationQueryService.getNotifications(jwtPrincipal.memberId(), lastId, limit);
		return ApiResponse.success(response);
	}

	@Operation(summary = "내 미읽음 알림 개수 조회", description = "로그인한 사용자의 읽지 않은 알림 개수를 조회합니다.")
	@SecurityRequirement(name = "AccessToken")
	@GetMapping("/unread-count")
	public ApiResponse<UnreadNotificationCountResponse> getUnreadCount(
		@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal
	) {
		UnreadNotificationCountResponse response = notificationQueryService.getUnreadCount(jwtPrincipal.memberId());
		return ApiResponse.success(response);
	}

	@Operation(summary = "알림 단건 읽음 처리", description = "로그인한 사용자의 특정 알림을 읽음 처리합니다.")
	@ApiResponses({
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "타인의 알림 접근"),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 알림")
	})
	@SecurityRequirement(name = "AccessToken")
	@PatchMapping("/{notificationId}/read")
	public ApiResponse<Void> markAsRead(
		@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
		@Parameter(description = "읽음 처리할 알림 ID", required = true, example = "1")
		@PathVariable Long notificationId
	) {
		notificationCommandService.markAsRead(jwtPrincipal.memberId(), notificationId);
		return ApiResponse.success(null);
	}

	@Operation(summary = "내 알림 전체 읽음 처리", description = "로그인한 사용자의 모든 미읽음 알림을 읽음 처리합니다.")
	@SecurityRequirement(name = "AccessToken")
	@PatchMapping("/read-all")
	public ApiResponse<Void> markAllAsRead(
		@Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal
	) {
		notificationCommandService.markAllAsRead(jwtPrincipal.memberId());
		return ApiResponse.success(null);
	}
}
