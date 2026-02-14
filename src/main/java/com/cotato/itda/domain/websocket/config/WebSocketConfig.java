package com.cotato.itda.domain.websocket.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import com.cotato.itda.domain.websocket.listener.IpHandshakeInterceptor;
import com.cotato.itda.domain.websocket.security.StompAuthChannelInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

	private final String[] allowedOriginPatterns;
	private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

	public WebSocketConfig(
		@Value("${app.websocket.allowed-origins}") String allowedOrigins,
		StompAuthChannelInterceptor stompAuthChannelInterceptor
	) {
		this.allowedOriginPatterns = parseAllowedOrigins(allowedOrigins);
		this.stompAuthChannelInterceptor = stompAuthChannelInterceptor;
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// 클라이언트가 실제로 접속하는 WS 엔드포인트
		registry.addEndpoint("/ws")
			// 브라우저 CORS
			// setAllowedOriginPatterns를 쓰면 와일드카드(*) 사용 가능+ 여러 도메인 허용 가능
			//.setAllowedOriginPatterns(allowedOriginPatterns)
			.setAllowedOriginPatterns("*")
			//핸드셰이크에서 IP정보 뽑아오는 인터셉터 추가
			// IP/User-Agent를 session attributes에 저장해두면
			// 이후에 connect/disconnect 이벤트에서 참조 가능
			.addInterceptors(new IpHandshakeInterceptor());
	}

	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// 1) 메시지 브로커 (서버 -> 클라이언트 publish)를 단순 브로커로 활성화
		// - /topic: 1:N 다중 구독 채널용 prefix
		// - /queue: 1:1 단독 구독 채널용 prefix
		registry.enableSimpleBroker("/topic", "/queue")
			// heartbeat 설정: 클라이언트가 10초마다 서버에 ping, 서버는 10초마다 클라이언트에 pong
			.setHeartbeatValue(new long[] {10000L, 10000L})
			.setTaskScheduler(heartBeatTaskScheduler());

		// 클라이언트가 SEND 시 사용하는 애플리케이션 prefix
		registry.setApplicationDestinationPrefixes("/app");
		registry.setUserDestinationPrefix("/user");
	}

	@Override
	public void configureClientInboundChannel(ChannelRegistration registration) {
		// CONNECT/SUBSCRIBE/SEND 프레임에서 JWT 인증 처리
		registration.interceptors(stompAuthChannelInterceptor);
	}

	@Bean
	public TaskScheduler heartBeatTaskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
		scheduler.setPoolSize(1);
		scheduler.setThreadNamePrefix("ws-heartbeat-");
		scheduler.initialize();
		return scheduler;
	}

	private String[] parseAllowedOrigins(String allowedOrigins) {
		return Arrays.stream(allowedOrigins.split(","))
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.toArray(String[]::new);
	}
}
