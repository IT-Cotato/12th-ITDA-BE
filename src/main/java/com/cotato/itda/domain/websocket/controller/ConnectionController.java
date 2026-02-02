package com.cotato.itda.domain.websocket.controller;

import java.security.Principal;
import java.time.Instant;

import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

import com.cotato.itda.domain.websocket.dto.ConnectionAckPayload;
import com.cotato.itda.global.error.constant.JwtErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ConnectionController {

	// SimpMessagingTemplate은 Spring에서 제공하는 메시징 템플릿으로,
	// WebSocket을 통해 클라이언트에게 메시지를 보내는 데 사용된다.
	// 이 템플릿을 사용하면 특정 사용자나 주제(topic)로 메시지를 전송할 수 있다.
	private final SimpMessagingTemplate simpMessagingTemplate;

	/**
	 * 클라이언트가 SEND "/app/connection/ack"로 메시지를 보낼 때 호출되는 메서드
	 * 서버가 클라이언트의 연결을 확인하고, 연결 성공 메시지를 해당 클라이언트에게 전송한다.
	 * /user/queue/connection/ack 로 메시지를 보내므로, 클라이언트는 /user/queue/connection/ack 을 구독해야 한다.
	 * <p>
	 * Param principal 현재 인증된 사용자의 정보를 담고 있는 Principal 객체
	 * Param sessionId 현재 WebSocket 세션의 ID
	 */
	@MessageMapping("/connection/ack")
	public void ack(
		Principal principal,
		@Header("simpSessionId") String sessionId
	) {
		if (principal == null) {
			// 인증되지 않은 사용자 처리
			log.info("인증되지 않은 사용자가 연결 확인 메시지를 보냈습니다. 세션 ID: {}", sessionId);
			throw new BusinessException(JwtErrorCode.UNAUTHORIZED);
		}

		String principalName = principal.getName();
		log.info("사용자 '{}'가 연결 확인 메시지를 보냈습니다. 세션 ID: {}", principalName, sessionId);
		ConnectionAckPayload payload = new ConnectionAckPayload(
			Instant.now().toString(),
			sessionId,
			principalName,
			"WS&STOMP 연결이 성공적으로 설정되었습니다."
		);

		/**
		 * 특정 사용자에게 메시지를 보내는 메서드
		 * @param principalName: 메시지를 받을 사용자의 이름(또는 ID)
		 * destination: "/queue/connection/ack" - 사용자의 개인 큐로 메시지를 보냄
		 * 클라이언트는 "/user/queue/connection/ack"을 구독하면 된다
		 */
		simpMessagingTemplate.convertAndSendToUser(
			principalName,
			"/queue/connection/ack",
			payload
		);
	}
}
