package com.cotato.itda.domain.websocket.listener;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * WebSocket 핸드셰이크(HTTP UPGRADE 요청) 시 클라이언트의 IP 주소와 User-Agent 정보를 추출하여
 * 세션 속성에 저장하는 인터셉터입니다.
 */
public class IpHandshakeInterceptor implements HandshakeInterceptor {

	@Override
	public boolean beforeHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Map<String, Object> attributes
	) {
		// User-Agent 헤더 추출
		// User-Agent는 클라이언트의 종류 및 버전을 나타내는 문자열
		HttpHeaders headers = request.getHeaders();
		String userAgent = headers.getFirst("User-Agent");

		// 클라이언트 IP 주소 추출
		String clientIp = "unknown";
		if (request instanceof ServletServerHttpRequest servletRequest) {
			clientIp = servletRequest.getServletRequest().getRemoteAddr();
		}

		// 세션 attributes에 저장 -> 나중에 connect/disconnect 이벤트에서 꺼내씀
		attributes.put("clientIp", clientIp);
		attributes.put("userAgent", userAgent);
		return true; // 핸드셰이크 계속 진행
	}

	@Override
	public void afterHandshake(
		ServerHttpRequest request,
		ServerHttpResponse response,
		WebSocketHandler wsHandler,
		Exception exception
	) {
		// 핸드셰이크 후 처리할 내용이 없으므로 비워둠
	}
}
