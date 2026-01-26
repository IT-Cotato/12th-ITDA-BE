package com.cotato.itda.domain.passwordreset.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cotato.itda.domain.passwordreset.config.PasswordResetProperties;
import com.cotato.itda.domain.passwordreset.dto.PasswordResetDraftRedisValue;
import com.cotato.itda.domain.signup.repository.SignupDraftRedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetDraftService {

	private final PasswordResetProperties passwordResetProperties;
	private final SignupDraftRedisRepository passwordResetDraftRedisRepository;
	// STEP1 : Draft 생성
	public String createDraft() {
		// Draft Key 생성
		String draftKey = UUID.randomUUID().toString();
		log.info("Password Reset Draft Key 생성: {}", draftKey);
		// PasswordResetDraftRedisValue 생성
		PasswordResetDraftRedisValue redisValue =
			PasswordResetDraftRedisValue.newDraft(draftKey);
		log.info("Password Reset Redis 저장용 Draft 값 생성 완료");

		// TTL 설정
		Duration ttl = Duration.ofMinutes(passwordResetProperties.getDraftTtlMinutes()); // 10분
		// Redis에 저장
		passwordResetDraftRedisRepository.save(redisValue, ttl);
		// Draft Key 반환
		return null;
	}
}
