package com.cotato.itda.domain.sms.infra.solapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cotato.itda.domain.sms.infra.solapi.SolapiAuthHeaderGenerator;

import feign.RequestInterceptor;

@Configuration
public class SolapiFeignConfig {
	@Bean
	public SolapiAuthHeaderGenerator solapiAuthHeaderGenerator(SolapiProperties props) {
		return new SolapiAuthHeaderGenerator(props);
	}

	@Bean
	public RequestInterceptor solapiAuthInterceptor(SolapiAuthHeaderGenerator generator) {
		return template -> {
			template.header("Authorization", generator.createAuthHeader());
			template.header("Content-Type", "application/json");
		};
	}
}
