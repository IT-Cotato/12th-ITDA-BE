package com.cotato.itda.domain.passwordreset.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cotato.itda.domain.passwordreset.config.PasswordResetProperties;
import com.cotato.itda.domain.passwordreset.dto.PasswordResetDraftRedisValue;
import com.cotato.itda.domain.passwordreset.dto.PasswordResetStep;
import com.cotato.itda.domain.passwordreset.dto.response.PasswordResetCreateDraftResponse;
import com.cotato.itda.domain.passwordreset.repository.PasswordResetDraftRedisRepository;
import com.cotato.itda.domain.signup.repository.SignupDraftRedisRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetDraftService {

	private final PasswordResetProperties passwordResetProperties;
	private final PasswordResetDraftRedisRepository passwordResetDraftRedisRepository;
	// STEP1 : Draft 생성
	public DraftCreationResult createDraft() {
		// Draft Key 생성
		String draftKey = UUID.randomUUID().toString();
		log.info("Password Reset Draft Key 생성: {}", draftKey);

		// PasswordResetDraftRedisValue 생성
		PasswordResetDraftRedisValue redisValue =
			PasswordResetDraftRedisValue.newDraft(draftKey);
		log.info("Password Reset Redis 저장용 Draft 값 생성 완료");

		// TTL 설정
		Duration ttl = Duration.ofMinutes(passwordResetProperties.getDraftTtlMinutes()); // 10분
		log.info("Password Reset Draft TTL 설정: {}분", ttl.toMinutes());

		// Redis에 저장
		passwordResetDraftRedisRepository.save(redisValue, ttl);
		log.info("Password Reset Draft Redis 저장 완료: draftKey={}, TTL={}분", draftKey, ttl.toMinutes());

		PasswordResetCreateDraftResponse response = new PasswordResetCreateDraftResponse(
			PasswordResetStep.OTP_REQUIRED
		);
		log.info("Password Reset Draft 생성 응답 준비 완료: step={}", response.step());

		return new DraftCreationResult(draftKey, response);
	}

	public record DraftCreationResult (
		String draftKey,
		PasswordResetCreateDraftResponse response
	){}
}
