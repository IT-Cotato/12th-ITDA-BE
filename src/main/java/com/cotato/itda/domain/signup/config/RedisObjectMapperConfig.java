package com.cotato.itda.domain.signup.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RedisObjectMapperConfig {

	/**
	 * Redis에 저장할 ObjectMapper 빈 등록
	 * Redis에 자바 객체를 그대로 저장할 수 없고 JSON 문자열로 변환해서 저장함
	 * - 그 변환(직렬화/역직렬화)을 담당하는 엔진이 Jackson의 objectMapper
	 *
	 * [문제]
	 * - 자바의 LocalDateTime 같은 날짜/시간 관련 객체를 JSON으로 변환할 때 문제가 발생할 수 있음
	 * - 기본 ObjectMapper는 LocalDateTime을 제대로 처리하지 못함
	 * [해결]
	 * - JavaTimeModule 모듈을 ObjectMapper에 등록하면
	 *   LocalDateTime을 포함한 날짜/시간 관련 객체를 올바르게 처리할 수 있음
	 * - 따라서 Redis에 저장할 ObjectMapper 빈을 생성할 때 JavaTimeModule을 등록ㅎ마
	 * @return
	 */
	@Bean
	public ObjectMapper redisObjectMapper(){
		ObjectMapper om = new ObjectMapper();
		om.registerModule(new JavaTimeModule());
		return om;
	}
}
