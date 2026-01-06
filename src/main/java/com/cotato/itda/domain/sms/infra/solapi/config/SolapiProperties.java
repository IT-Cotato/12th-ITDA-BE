package com.cotato.itda.domain.sms.infra.solapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "solapi")
public record SolapiProperties(
	String baseUrl,
	String apiKey,
	String apiSecret,
	String defaultFrom
) {
}
