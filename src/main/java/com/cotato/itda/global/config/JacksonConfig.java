package com.cotato.itda.global.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
public class JacksonConfig {
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
		return builder -> builder.featuresToDisable(
			SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
			SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
		);
	}
}
