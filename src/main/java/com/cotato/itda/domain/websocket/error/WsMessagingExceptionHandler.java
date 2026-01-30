package com.cotato.itda.domain.websocket.error;

import java.time.Instant;

import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

import com.cotato.itda.domain.websocket.dto.WsErrorPayload;

/**
 * WebSocket 메시지 처리 중 발생하는 예외를 처리하는 전역 예외 처리기
 * 모든 예외를 잡아 WsErrorPayload 형태로 클라이언트에게 전송
 *
 * @ControllerAdvice 어노테이션을 사용하여 전역 예외 처리기로 지정
 * @MessageExceptionHandler 어노테이션을 사용하여 메시지 처리 중 발생하는 예외를 처리
 * @SendToUser 어노테이션을 사용하여 특정 사용자에게 에러 메시지를 전송
 */
@ControllerAdvice
public class WsMessagingExceptionHandler {

	@MessageExceptionHandler(Exception.class)
	@SendToUser("/queue/errors")
	public WsErrorPayload handle(Exception ex) {
		return new WsErrorPayload(
			Instant.now().toString(),
			"WS_MESSAGE_ERROR",
			ex.getMessage()
		);
	}
}
