package com.cotato.itda.global.security.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
	private PurposeProps access = new PurposeProps();
	private PurposeProps refresh = new PurposeProps();

	@Getter
	@Setter
	public static class PurposeProps {
		private JwtMacAlgorithm algorithm = JwtMacAlgorithm.HS256;
		private String secretKey; // Base64
		private long validityInSeconds;
	}

	public PurposeProps get(JwtPurpose purpose) {
		return switch (purpose) {
			case ACCESS -> access;
			case REFRESH -> refresh;
		};
	}

}

