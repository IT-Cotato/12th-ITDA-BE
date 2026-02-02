package com.cotato.itda.domain.chat.controller;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.itda.domain.chat.controller.dto.ChatRoomSliceResponse;
import com.cotato.itda.domain.chat.exception.code.ChatErrorCode;
import com.cotato.itda.domain.chat.service.ChatRoomService;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

	private final ChatRoomService chatRoomService;

	/**
	 * GET /api/chat/rooms?cursorAt=2026-02-02T14:30:15.123&cursorRoomId=50&limit=20
	 * -> 사용자가 속한 채팅방들을 커서 기반 페이지네이션으로 조회
	 * @DateTimeFormat: ISO DATE TIME 포맷으로 파싱 -> LocalDateTime 으로 변환
	 */
	@GetMapping()
	public ChatRoomSliceResponse getMyRooms(
		@AuthenticationPrincipal JwtPrincipal jwtPrincipal,
		@RequestParam(required = false) Integer limit,
		@RequestParam(required = false)
		@DateTimeFormat( iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime cursorAt,
		@RequestParam(required = false) Long cursorRoomId
	){
		// 둘중 하나만 null로 오면 에러
		if((cursorAt==null)!=(cursorRoomId==null)){
			throw new BusinessException(ChatErrorCode.INVALID_CURSOR);
		}
		Long memberId=  jwtPrincipal.memberId();
		return chatRoomService.getMyRooms(
			memberId,
			limit,
			cursorAt,
			cursorRoomId
		);
	}
}
