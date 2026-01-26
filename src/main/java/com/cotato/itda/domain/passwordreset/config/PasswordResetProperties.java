package com.cotato.itda.domain.passwordreset.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix="password-reset")
public class PasswordResetProperties {
	private long draftTtlMinutes;
}
