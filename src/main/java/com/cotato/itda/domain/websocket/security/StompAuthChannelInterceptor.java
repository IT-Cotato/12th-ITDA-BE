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

		StompHeaderAccessor accessor =
			MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

		if (accessor == null) {
			log.warn("[WS][디버그] StompHeaderAccessor를 가져올 수 없음. messageHeaders={}", message.getHeaders());
			return message;
		}

		StompCommand command = accessor.getCommand();
		String sessionId = accessor.getSessionId();
		String destination = accessor.getDestination();

		String authHeader = accessor.getFirstNativeHeader("Authorization"); // 없으면 null

		// ===== 0) 들어온 프레임 공통 로그 =====
		// SEND/CONNECT/SUBSCRIBE가 아예 들어오는지부터 확인
		log.info("[WS][수신] command={}, sessionId={}, destination={}", command, sessionId, destination);

		// Authorization 유무만 표시 (토큰 전문은 절대 로그로 남기지 말기)
		log.info("[WS][헤더] Authorization 존재 여부={}",
			(authHeader != null && !authHeader.isBlank())
		);

		try {
			// ===== 1) CONNECT 인증 =====
			if (command == StompCommand.CONNECT) {
				log.info("[WS][CONNECT] STOMP CONNECT 프레임 인증 시작. sessionId={}", sessionId);

				if (authHeader == null || authHeader.isBlank()) {
					log.warn("[WS][CONNECT][차단] Authorization 헤더가 없음. sessionId={}", sessionId);
					throw new BusinessException(JwtErrorCode.MISSING_TOKEN);
				}

				String token = extractBearerToken(authHeader);

				// 토큰 길이만 로그 (내용 X)
				log.info("[WS][CONNECT] Bearer 토큰 추출 완료. tokenLength={}, sessionId={}",
					(token != null ? token.length() : -1), sessionId
				);

				Claims claims = jwtTokenValidator.validateAndGetClaims(token, JwtPurpose.ACCESS);
				log.info("[WS][CONNECT] 토큰 검증 성공. claimsSubject={}, sessionId={}",
					(claims != null ? claims.getSubject() : "null"), sessionId
				);

				Authentication authentication = jwtTokenProvider.getAuthentication(claims);
				log.info("[WS][CONNECT] Authentication 생성 성공. username={}, sessionId={}",
					authentication.getName(), sessionId
				);

				accessor.setUser(authentication);

				Map<String, Object> attrs = accessor.getSessionAttributes();
				if (attrs != null) {
					attrs.put("wsUsername", authentication.getName());
				}

				log.info("[WS][CONNECT] STOMP 세션 사용자 설정 완료. username={}, sessionId={}",
					authentication.getName(), sessionId
				);
			}

			// ===== 2) CONNECT 이후 프레임(SUBSCRIBE/SEND 등) 인증 확인 =====
			if (command != null && command != StompCommand.CONNECT) {
				if (accessor.getUser() == null) {
					// 여기 걸리면: "CONNECT 때 setUser가 안 됐거나",
					// "클라이언트가 CONNECT 없이 바로 SEND/SUBSCRIBE를 보냈거나",
					// "프레임이 다른 세션으로 들어왔거나" 중 하나
					log.warn("[WS][차단] STOMP {} 거부: 사용자 정보 없음(인증 안 됨). sessionId={}, destination={}",
						command, sessionId, destination
					);
					throw new BusinessException(JwtErrorCode.UNAUTHORIZED);
				}

				log.info("[WS][통과] STOMP {} 인증된 사용자 확인. username={}, sessionId={}, destination={}",
					command, accessor.getUser().getName(), sessionId, destination
				);
			}

			return message;

		} catch (BusinessException e) {
			// 비즈니스 예외는 왜 막혔는지 명확히 로그
			log.warn("[WS][예외] STOMP 처리 중 차단됨. command={}, sessionId={}, destination={}, errorCode={}",
				command, sessionId, destination, e.getErrorCode()
			);
			throw e;
		} catch (Exception e) {
			// 예상치 못한 예외
			log.error("[WS][예외] STOMP 처리 중 알 수 없는 오류. command={}, sessionId={}, destination={}",
				command, sessionId, destination, e
			);
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
