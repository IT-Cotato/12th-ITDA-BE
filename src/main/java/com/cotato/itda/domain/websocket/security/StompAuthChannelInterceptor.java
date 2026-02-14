package com.cotato.itda.domain.websocket.security;

import java.util.List;
import java.util.Map;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.cotato.itda.global.error.constant.JwtErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.cotato.itda.global.security.jwt.config.JwtPurpose;
import com.cotato.itda.global.security.jwt.token.JwtTokenProvider;
import com.cotato.itda.global.security.jwt.token.JwtTokenValidator;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

	private final JwtTokenValidator jwtTokenValidator;
	private final JwtTokenProvider jwtTokenProvider;


	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message); // 핵심: wrap

		StompCommand command = accessor.getCommand();
		String sessionId = accessor.getSessionId();
		String destination = accessor.getDestination();

		log.info("[WS][수신] command={}, sessionId={}, destination={}", command, sessionId, destination);

		try {
			if (command == StompCommand.CONNECT) {
				String authHeader = accessor.getFirstNativeHeader("Authorization");
				if (authHeader == null || authHeader.isBlank()) {
					authHeader = accessor.getFirstNativeHeader("authorization");
				}

				if (authHeader == null || authHeader.isBlank()) {
					log.warn("[WS][CONNECT][차단] Authorization 헤더 없음. sessionId={}", sessionId);
					throw new BusinessException(JwtErrorCode.MISSING_TOKEN);
				}

				String token = extractBearerToken(authHeader);

				Claims claims = jwtTokenValidator.validateAndGetClaims(token, JwtPurpose.ACCESS);
				Authentication authentication = jwtTokenProvider.getAuthentication(claims);

				accessor.setUser(authentication);

				Map<String, Object> attrs = accessor.getSessionAttributes();
				if (attrs != null) attrs.put("wsUsername", authentication.getName());

				log.info("[WS][CONNECT] 인증 완료. username={}, sessionId={}", authentication.getName(), sessionId);

				// CONNECT에서 변경했으니 새 메시지로 리턴
				return org.springframework.messaging.support.MessageBuilder
					.createMessage(message.getPayload(), accessor.getMessageHeaders());
			}

			if (command != null && command != StompCommand.CONNECT) {
				if (accessor.getUser() == null) {
					log.warn("[WS][차단] {} 거부: 사용자 정보 없음. sessionId={}, destination={}",
						command, sessionId, destination);
					throw new BusinessException(JwtErrorCode.UNAUTHORIZED);
				}
			}

			return message;

		} catch (BusinessException e) {
			log.warn("[WS][예외] 차단됨. command={}, sessionId={}, destination={}, errorCode={}",
				command, sessionId, destination, e.getErrorCode());
			throw e;
		}
	}



	public String extractBearerToken(String authHeader) {
		String prefix = "Bearer ";
		if (authHeader.startsWith(prefix)) {
			return authHeader.substring(prefix.length()).trim();
		}
		return authHeader.trim();
	}
}
