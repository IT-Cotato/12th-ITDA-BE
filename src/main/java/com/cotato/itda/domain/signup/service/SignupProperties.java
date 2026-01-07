package com.cotato.itda.domain.signup.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "signup")
public class SignupProperties {
	/**
	 * signup.draft-ttl-minutes 로 주입
	 */
	private long draftTtlMinutes;
}
