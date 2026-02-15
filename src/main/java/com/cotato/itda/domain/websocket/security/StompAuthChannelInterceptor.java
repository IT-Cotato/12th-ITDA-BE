package com.cotato.itda.domain.websocket.security;

import java.util.Map;
import java.util.Set;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
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

	private static final String SESSION_AUTH_KEY = "WS_AUTHENTICATION";
	private static final String SESSION_USERNAME_KEY = "wsUsername";

	private final JwtTokenValidator jwtTokenValidator;
	private final JwtTokenProvider jwtTokenProvider;

	// 인증이 반드시 필요한 STOMP 커맨드들
	private static final Set<StompCommand> AUTH_REQUIRED = Set.of(
		StompCommand.SUBSCRIBE,
		StompCommand.UNSUBSCRIBE,
		StompCommand.SEND
		// 필요하면 ACK/NACK/BEGIN/COMMIT/ABORT도 추가 가능
		// StompCommand.ACK, StompCommand.NACK, StompCommand.BEGIN, StompCommand.COMMIT, StompCommand.ABORT
	);

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaderAccessor accessor =
			MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		// STOMP 메시지가 아니면 그냥 통과
		if (accessor == null) return message;

		StompCommand command = accessor.getCommand();
		String sessionId = accessor.getSessionId();
		String destination = accessor.getDestination();

		log.info("[WS][IN] command={}, sessionId={}, destination={}", command, sessionId, destination);

		// heartbeat 프레임은 command == null 로 들어올 수 있음 → 무조건 통과
		if (command == null) return message;

		// DISCONNECT는 정리 단계에서 principal 없을 수 있음 → 절대 막지 말기
		if (command == StompCommand.DISCONNECT) return message;

		// headers 수정 가능하게
		accessor.setLeaveMutable(true);

		boolean mutated = false;
		Map<String, Object> attrs = accessor.getSessionAttributes();

		try {
			// 1) CONNECT: 토큰 검증 후 Authentication 생성 → user 세팅 + 세션에 저장
			if (command == StompCommand.CONNECT) {
				String authHeader = firstNativeHeader(accessor, "Authorization", "authorization");
				log.info("[WS][CONNECT] Authorization header={}", authHeader);

				if (authHeader == null || authHeader.isBlank()) {
					log.warn("[WS][CONNECT][BLOCK] missing Authorization header. sessionId={}", sessionId);
					throw new BusinessException(JwtErrorCode.MISSING_TOKEN);
				}

				String token = extractBearerToken(authHeader);
				Claims claims = jwtTokenValidator.validateAndGetClaims(token, JwtPurpose.ACCESS);
				Authentication authentication = jwtTokenProvider.getAuthentication(claims);

				accessor.setUser(authentication);
				mutated = true;

				if (attrs != null) {
					attrs.put(SESSION_AUTH_KEY, authentication);
					attrs.put(SESSION_USERNAME_KEY, authentication.getName());
				}

				log.info("[WS][CONNECT] authenticated user={}, sessionId={}",
					authentication.getName(), sessionId);

				return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
			}

			// 2) CONNECT 이후 프레임(SUBSCRIBE/SEND 등):
			//    accessor.getUser()가 null로 들어올 수 있으니 세션 attrs에서 복구해서 붙여줌
			if (accessor.getUser() == null && attrs != null) {
				Object stored = attrs.get(SESSION_AUTH_KEY);
				if (stored instanceof Authentication auth) {
					accessor.setUser(auth);
					mutated = true;
					log.debug("[WS][{}] attach auth from session. user={}, sessionId={}",
						command, auth.getName(), sessionId);
				}
			}

			// 3) 필요한 커맨드에만 인증 강제
			if (AUTH_REQUIRED.contains(command) && accessor.getUser() == null) {
				log.warn("[WS][BLOCK] {} denied: no user. sessionId={}, destination={}",
					command, sessionId, destination);
				throw new BusinessException(JwtErrorCode.UNAUTHORIZED);
			}

			// 4) 헤더 바꿨으면 새 메시지로 리턴
			return mutated
				? MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders())
				: message;

		} catch (BusinessException e) {
			log.warn("[WS][BLOCKED] command={}, sessionId={}, destination={}, errorCode={}",
				command, sessionId, destination, e.getErrorCode());
			throw e;
		}
	}

	private String firstNativeHeader(StompHeaderAccessor accessor, String... keys) {
		for (String key : keys) {
			String v = accessor.getFirstNativeHeader(key);
			if (v != null && !v.isBlank()) return v;
		}
		return null;
	}

	public String extractBearerToken(String authHeader) {
		String prefix = "Bearer ";
		if (authHeader.startsWith(prefix)) {
			return authHeader.substring(prefix.length()).trim();
		}
		return authHeader.trim();
	}
}