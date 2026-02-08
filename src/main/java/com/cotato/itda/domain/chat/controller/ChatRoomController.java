package com.cotato.itda.domain.chat.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.itda.domain.chat.controller.dto.ChatMessageSliceResponse;
import com.cotato.itda.domain.chat.controller.dto.ChatRoomSliceResponse;
import com.cotato.itda.domain.chat.controller.dto.DirectRoomResolveResponse;
import com.cotato.itda.domain.chat.exception.code.ChatErrorCode;
import com.cotato.itda.domain.chat.service.ChatMessageService;
import com.cotato.itda.domain.chat.service.ChatRoomService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

	private final ChatRoomService chatRoomService;
	private final ChatMessageService chatMessageService;
	/**
	 * GET /api/chat/rooms?cursorAt=2026-02-02T14:30:15.123&cursorRoomId=50&limit=20
	 * -> 사용자가 속한 채팅방들을 커서 기반 페이지네이션으로 조회
	 *
	 * @DateTimeFormat: ISO DATE TIME 포맷으로 파싱 -> LocalDateTime 으로 변환
	 */
	@Operation(summary = "내 채팅방 목록 조회 (커서 페이지네이션)", description = "내가 속한 채팅방 목록을 커서 기반 페이지네이션으로 조회합니다.")
	@GetMapping()
	public ApiResponse<ChatRoomSliceResponse> getMyRooms(
		@AuthenticationPrincipal JwtPrincipal jwtPrincipal,

		@Parameter(description = "페이지 크기 (기본 10, 최대 20)", example = "10")

		@RequestParam(required = false)
		Integer limit,

		@Parameter(
			description = "커서 시간 (ISO-8601). cursorRoomId와 함께 전달해야 함, 첫 페이지면 null",
			example = "2026-02-02T14:30:15.123"
		)
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime cursorAt,

		@Parameter(
			description = "커서 방 ID. cursorAt과 함께 전달해야 함, 첫 페이지면 null",
			example = "50"
		)
		@RequestParam(required = false)
		Long cursorRoomId
	) {
		// 둘중 하나만 null로 오면 에러
		if ((cursorAt == null) != (cursorRoomId == null)) {
			throw new BusinessException(ChatErrorCode.INVALID_CURSOR);
		}
		Long memberId = jwtPrincipal.memberId();
		ChatRoomSliceResponse response= chatRoomService.getMyRooms(
			memberId,
			limit,
			cursorAt,
			cursorRoomId
		);
		return ApiResponse.success(response);
	}

	@Operation(
		summary = "DIRECT 방 존재 여부 resolve",
		description = """
			친구 선택 후 채팅 화면 진입 전에 호출.
			exists=true면 roomId로 /topic/chat/rooms/{roomId} 구독 + 히스토리 로딩.
			exists=false면 임시 화면(roomId 없음) 진입 후 첫 SEND에서 방 생성.
		"""
	)
	@GetMapping("/direct/resolve")
	public ApiResponse<DirectRoomResolveResponse> resolveDirectRoom(
		@AuthenticationPrincipal JwtPrincipal jwtPrincipal,
		@RequestParam Long opponentMemberId
	) {
		Long memberId = jwtPrincipal.memberId();
		DirectRoomResolveResponse response = chatRoomService.resolveDirectRoom(memberId, opponentMemberId);
		return ApiResponse.success(response);
	}

	@Operation(
		summary = "채팅 히스토리 조회(커서 페이지네이션)",
		description = """
			- roomId가 있는 채팅 화면에서 HTTP로 과거 메시지를 로딩하는 API
			- cursorSeq가 null이면 최신부터
			- cursorSeq가 있으면 message_seq < cursorSeq로 과거로 내려감
		"""
	)
	@GetMapping("/{roomId}/messages")
	public ApiResponse<ChatMessageSliceResponse> getRoomMessages(
		@AuthenticationPrincipal JwtPrincipal jwtPrincipal,
		@PathVariable Long roomId,
		@RequestParam(required = false) Integer limit,
		@RequestParam(required = false) Long cursorSeq
	) {
		Long memberId = jwtPrincipal.memberId();
		ChatMessageSliceResponse response = chatMessageService.getRoomMessages(memberId, roomId, limit, cursorSeq);
		return ApiResponse.success(response);
	}

	@PostMapping("/left/{roomId}")
	public ApiResponse<Void> leaveRoom(
		@AuthenticationPrincipal JwtPrincipal jwtPrincipal,
		@PathVariable Long roomId
	){
		Long memberId = jwtPrincipal.memberId();
		chatRoomService.leaveRoom(memberId, roomId);
		return ApiResponse.success(null);
	}
}
