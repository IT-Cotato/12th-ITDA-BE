package com.cotato.itda.domain.signup.repository;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.cotato.itda.domain.signup.dto.SignupDraftRedisValue;
import com.cotato.itda.global.error.constant.RedisErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class SignupDraftRedisRepository {

	private static final String KEY_PREFIX = "signup:draft:";

	private final StringRedisTemplate redis;

	// @Qualifier 어노테이션을 사용하여 특정 ObjectMapper 빈을 주입
	@Qualifier("redisObjectMapper")
	private final ObjectMapper redisObjectMapper;

	/**
	 * Draft 데이터를 Redis에 저장
	 * ttl: 만료 시간
	 * 실제 메서드 호출 예시
	 * signupDraftRedisRepository.save(value, Duration.ofMinutes(30));
	 */
	public void save(SignupDraftRedisValue value, Duration ttl){
		String redisKey =  KEY_PREFIX + value.draftKey();

		String json;
		try{
			json = redisObjectMapper.writeValueAsString(value);
		}catch (JsonProcessingException e){
			// JSON 직렬화 실패 시 예외 처리
			throw new BusinessException(RedisErrorCode.SERIALIZATION_ERROR);
		}

		// Redis에 데이터 저장 및 만료 시간 설정
		// set(key, value, ttl)로 하면 ttl 이후에 자동으로 삭제됨
		redis.opsForValue().set(redisKey, json, ttl);
	}

	public SignupDraftRedisValue find(String draftKey){
		String redisKey = KEY_PREFIX+draftKey;
		String json;
		try {
			json = redis.opsForValue().get(redisKey);
			if (json == null)
				return null;

			// JSON 문자열을 SignupDraftRedisValue 객체로 역직렬화
			return redisObjectMapper.readValue(json, SignupDraftRedisValue.class);
		}catch(JsonProcessingException e){
			throw new BusinessException(RedisErrorCode.SERIALIZATION_ERROR);
		}

	}
}
