package com.cotato.itda.domain.signup.repository;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.cotato.itda.domain.signup.dto.SignupDraftRedisValue;
import com.cotato.itda.global.error.constant.RedisErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@RequiredArgsConstructor(onConstructor_ = @__(@Autowired))
public class SignupDraftRedisRepository {

	private static final String KEY_PREFIX = "signup:draft:";

	private final StringRedisTemplate redis;

	private final ObjectMapper redisObjectMapper;

	/**
	 * Draft 데이터를 Redis에 저장
	 * ttl: 만료 시간
	 * 실제 메서드 호출 예시
	 * signupDraftRedisRepository.save(value, Duration.ofMinutes(30));
	 */
	public void save(SignupDraftRedisValue value, Duration ttl) {
		if (ttl == null || ttl.isZero() || ttl.isNegative()) {
			throw new BusinessException(RedisErrorCode.INVALID_TTL); // 없으면 IllegalArgumentException이라도
		}
		String redisKey = KEY_PREFIX + value.draftKey();

		String json = toJson(value);

		// Redis에 데이터 저장 및 만료 시간 설정
		// set(key, value, ttl)로 하면 ttl 이후에 자동으로 삭제됨
		redis.opsForValue().set(redisKey, json, ttl);
		Boolean exists = redis.hasKey(redisKey);
		Long seconds = redis.getExpire(redisKey);
		log.info("Redis Key Exists: {}, TTL(seconds): {}", exists, seconds);

	}

	public Optional<SignupDraftRedisValue> findByDraftKey(String draftKey) {
		String redisKey = KEY_PREFIX + draftKey;
		String json;

		json = redis.opsForValue().get(redisKey);
		Boolean exists = redis.hasKey(redisKey);
		Long seconds = redis.getExpire(redisKey);
		log.info("Redis Key Exists: {}, TTL(seconds): {}", exists, seconds);
		if (json == null) {
			return Optional.empty();
		}
		try {
			// JSON 문자열을 SignupDraftRedisValue 객체로 역직렬화
			return Optional.of(redisObjectMapper.readValue(json, SignupDraftRedisValue.class));
		} catch (Exception e) {
			throw new BusinessException(RedisErrorCode.SERIALIZATION_ERROR);
		}

	}

	/**
	 * 업데이트 시 기존 TTL 유지
	 *
	 * STEP 2~6은 draft 값을 계속 갱신하는데,
	 * 매번 TTL을 초기값으로 리셋하면 사용자가 요청만 반복해도 draft가 영구히 살아남을 수 있다.
	 * 그래서 남은 TTL을 읽고 그대로 유지하며 업데이트한다.
	 */
	public void updatePreserveTtl(SignupDraftRedisValue value){
		String redisKey = KEY_PREFIX+value.draftKey();
		Long ttlSeconds = redis.getExpire(redisKey, TimeUnit.SECONDS);
		if(ttlSeconds ==null || ttlSeconds==-1){
			// -2 : 키 없음
			throw new BusinessException(RedisErrorCode.DRAFT_NOT_FOUND);
		}
		String json = toJson(value);

		redis.opsForValue().set(redisKey, json, Duration.ofSeconds(ttlSeconds));
	}

	private String toJson(SignupDraftRedisValue value){
		try{
			return redisObjectMapper.writeValueAsString(value);
		}catch(JsonProcessingException e){
			throw new BusinessException(RedisErrorCode.SERIALIZATION_ERROR);
		}
	}
}
