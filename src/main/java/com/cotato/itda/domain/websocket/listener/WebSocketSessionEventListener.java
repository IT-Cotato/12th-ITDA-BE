package com.cotato.itda.domain.websocket.listener;

import java.util.Map;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import lombok.extern.slf4j.Slf4j;

/**
 * - SessionConnectEvent: CONNECT 프레임이 들어온 시점
 * - SessionConnectedEvent: CONNECT 처리 후 CONNECTED가 나간 시점
 * - SessionSubscribeEvent: SUBSCRIBE 들어온 시점(채널 추적 가능)
 * - SessionDisconnectEvent: 연결 종료(정상/비정상 포함)
 * @EventListener 애노테이션을 사용하여 WebSocket 세션 이벤트를 처리하는 리스너 클래스
 * - 인자에 해당하는 이벤트가 발생할 때마다 메서드가 호출된다.
 * 예를 들어, onConnect 메서드는 SessionConnectEvent가 발생할 때 호출된다.
 */
@Slf4j
@Component
public class WebSocketSessionEventListener {

	//@EventListener는
	@EventListener
	public void onConnect(SessionConnectEvent event){
		StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
		logCommon("[WS][CONNECT_EVENT]", acc);
	}

	@EventListener
	public void onConnected(SessionConnectedEvent event) {
		StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
		logCommon("[WS][CONNECTED_EVENT]", acc);
	}

	@EventListener
	public void onSubscribe(SessionSubscribeEvent event) {
		StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
		String dest = acc.getDestination();
		logCommon("[WS][SUBSCRIBE_EVENT] dest=" + dest, acc);
	}

	@EventListener
	public void onDisconnect(SessionDisconnectEvent event) {
		StompHeaderAccessor acc = StompHeaderAccessor.wrap(event.getMessage());
		String closeStatus = (event.getCloseStatus() != null) ? event.getCloseStatus().toString() : "unknown";
		logCommon("[WS][DISCONNECT_EVENT] closeStatus=" + closeStatus, acc);
	}

	private void logCommon(String prefix, StompHeaderAccessor acc) {
		String sessionId = acc.getSessionId();

		// CONNECT 인증이 성공했다면 여기서 user가 잡힌다.
		String principalName = (acc.getUser() != null) ? acc.getUser().getName() : "anonymous";

		Map<String, Object> attrs = acc.getSessionAttributes();
		String clientIp = (attrs != null && attrs.get("clientIp") != null) ? attrs.get("clientIp").toString() : "unknown";
		String userAgent = (attrs != null && attrs.get("userAgent") != null) ? String.valueOf(attrs.get("userAgent")) : "unknown";

		log.info("{} sessionId={} principalName={} clientIp={} userAgent={}",
			prefix, sessionId, principalName, clientIp, userAgent
		);
	}
}
