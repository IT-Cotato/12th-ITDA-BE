package com.cotato.itda.domain.websocket.controller;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.cotato.itda.domain.chat.service.ChatMessageService;
import com.cotato.itda.domain.websocket.dto.ChatSendMessageRequest;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * STOMP SEND 엔드포인트
 * - destination: /app/chat/messages/send
 *
 * 임시 화면(roomId 없음)에서도 SEND가 가능해야 하므로
 * roomId=null일 수 있고, 그때 opponentMemberId가 필요하다.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageWsController {

	private final ChatMessageService chatMessageService;

	@MessageMapping("/chat/messages/send")
	public void sendMessage(
		Principal principal,
		@Valid ChatSendMessageRequest req) {



		log.info ("principal={}", principal);
		Long senderMemberId = extractMemberId(principal);

		log.info("사용자 '{}'가 채팅 메시지 전송 요청을 보냈습니다. 요청 내용: {}", senderMemberId, req);
		chatMessageService.sendMessage(senderMemberId, req);

		// 여기서 return을 굳이 하지 않는 이유:
		// - 응답은 개인 큐 이벤트로 내려주는 구조
		// - 실시간 타임라인은 /topic/chat/rooms/{roomId}로 발행
	}

	private Long extractMemberId(Principal principal) {

		if (principal == null) {
			throw new IllegalStateException("WS Principal이 null입니다. CONNECT에서 accessor.setUser(...)가 안 됐습니다.");
		}

		// WS에서는 principal이 대부분 Authentication(토큰)으로 들어옴
		if (principal instanceof org.springframework.security.core.Authentication auth) {
			Object p = auth.getPrincipal();

			// auth.getPrincipal()이 JwtPrincipal인 경우
			if (p instanceof com.cotato.itda.global.security.jwt.principal.JwtPrincipal jp) {
				return jp.memberId();
			}

			// fallback: name이 숫자라면 (너는 getName을 memberId로 해놨으니 보통 여기로도 됨)
			return Long.parseLong(auth.getName());
		}

		// Authentication이 아닌 경우는 거의 없지만, 최후 fallback
		return Long.parseLong(principal.getName());
	}
}