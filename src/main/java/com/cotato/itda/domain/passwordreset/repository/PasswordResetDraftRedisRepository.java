package com.cotato.itda.domain.passwordreset.repository;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.cotato.itda.domain.passwordreset.dto.PasswordResetDraftRedisValue;
import com.cotato.itda.global.error.constant.RedisErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class PasswordResetDraftRedisRepository {

	private static final String KEY_PREFIX = "password-reset:draft:";

	private final StringRedisTemplate redis;

	private final ObjectMapper redisObjectMapper;

	/**
	 * Draft 데이터를 Redis에 저장
	 * ttl: 만료 시간
	 * 실제 메서드 호출 예시
	 * passwordResetDraftRedisRepository.save(value, Duration.ofMinutes(10));
	 */
	public void save(PasswordResetDraftRedisValue value, Duration ttl){
		if(ttl==null|| ttl.isZero() || ttl.isNegative()){
			throw new BusinessException(RedisErrorCode.INVALID_TTL);
		}
		String redisKey = KEY_PREFIX + value.draftKey();

		// 객체 -> JSON 변환
		String json = toJson(value);

		// Redis에 데이터 저장 및 만료 시간 설정
		// 동작 원리:
		// 1. redisKey와 json 문자열을 Redis에 저장
		// 2. ttl이 지나면 해당 키-값 쌍이 자동으로 삭제됨
		// 이 시점에 실제 Redis 서버에 데이터가 저장됨
		redis.opsForValue().set(redisKey, json, ttl);

		// 저장 확인
		Boolean exists = redis.hasKey(redisKey);

		Long seconds = redis.getExpire(redisKey);
		log.info("Redis Key Exists: {}, TTL(seconds): {}", exists, seconds);
	}

	public Optional<PasswordResetDraftRedisValue> findByDraftKey(String draftKey){
		String redisKey = KEY_PREFIX + draftKey;
		String json;

		json = redis.opsForValue().get(redisKey);
		Boolean exists = redis.hasKey(redisKey);
		Long seconds = redis.getExpire(redisKey);
		log.info("Redis Key Exists: {}, TTL(seconds): {}", exists, seconds);

		if(json==null){
			return Optional.empty();
		}
		try{
			// JSON 문자열을 PasswordResetDraftRedisValue 객체로 역직렬화
			return Optional.of(redisObjectMapper.readValue(json, PasswordResetDraftRedisValue.class));
		}catch(Exception e){
			throw new BusinessException(RedisErrorCode.SERIALIZATION_ERROR);
		}
	}

	/**
	 * 업데이트 시 기존 TTL 유지
	 */
	public void updatePreserveTtl(PasswordResetDraftRedisValue value){
		String redisKey = KEY_PREFIX + value.draftKey();
		Long ttlSeconds = redis.getExpire(redisKey, TimeUnit.SECONDS);
		if(ttlSeconds==null || ttlSeconds==-1){
			// -2 : 키 없음
			throw new BusinessException(RedisErrorCode.DRAFT_NOT_FOUND);
		}
		String json = toJson(value);
		// 기존 TTL 유지하며 값 업데이트
		// Duration.ofSeconds()는 초 단위 TTL 설정
		redis.opsForValue().set(redisKey, json, Duration.ofSeconds(ttlSeconds));
	}

	private String toJson(PasswordResetDraftRedisValue value){
		try {
			// Object -> JSON 변환
			// 동작원리:
			// 1. ObjectMapper가 PasswordResetDraftRedisValue 객체의 필드들을 읽음
			// 2. 각 필드의 값을 JSON 형식에 맞게 변환
			// 3. 변환된 JSON 문자열을 반환
			return redisObjectMapper.writeValueAsString(value);
		} catch (Exception e) {
			log.error("Redis Object to JSON 변환 실패", e);
			throw new BusinessException(RedisErrorCode.SERIALIZATION_ERROR);
		}
	}
}
