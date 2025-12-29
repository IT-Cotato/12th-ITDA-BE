package com.cotato.itda.domain.signup.service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.cotato.itda.domain.signup.dto.CreateDraftResponse;
import com.cotato.itda.domain.signup.dto.Data;
import com.cotato.itda.domain.signup.dto.Flags;
import com.cotato.itda.domain.signup.dto.SignupDraftRedisValue;
import com.cotato.itda.domain.signup.dto.TermsItem;
import com.cotato.itda.domain.signup.model.SignupStep;
import com.cotato.itda.domain.signup.model.TermsBundle;
import com.cotato.itda.domain.signup.repository.SignupDraftRedisRepository;
import com.cotato.itda.domain.signup.repository.TermsPolicyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupDraftService {

	private final SignupDraftRedisRepository signupDraftRedisRepository;
	private final TermsPolicyRepository termsPolicyRepository;
	private final SignupProperties signupProperties;

	/**
	 * Step 1: Draft 생성
	 */
	public DraftCreationResult createSignupDraft() {
		// DraftKey 생성
		String draftKey = UUID.randomUUID().toString();
		log.info("Draft Key 생성: {}", draftKey);

		// 2) 약관 목록 조회
		TermsBundle bundle = termsPolicyRepository.getSignupTermsBundle();
		log.info("약관 번들 조회: type={}, version={}", bundle.bundleType(), bundle.bundleVersion());

		// 3) 약관 항목들 조회
		List<TermsItem> terms = new ArrayList<>(termsPolicyRepository.getSignupTerms(bundle.bundleId()));
		log.info("약관 항목 조회: {}개 항목", terms.size());

		// 4) 약관 항목들 정렬 (displayOrder 기준 오름차순)
		terms.sort(new Comparator<TermsItem>(){
			@Override
			public int compare(TermsItem o1, TermsItem o2){
				return Integer.compare(o1.displayOrder(), o2.displayOrder());
			}
		});
		log.info("약관 항목 정렬 완료");

		// 5) Redis에 저장할 값 만들기
		// step은 최초 생성이므로 TERMS_REQUIRED 고정
		SignupDraftRedisValue redisValue = new SignupDraftRedisValue(
			draftKey,
			SignupStep.TERMS_REQUIRED,
			bundle.bundleType(),
			bundle.bundleVersion(),
			OffsetDateTime.now()
		);
		log.info("Redis 저장용 Draft 값 생성 완료");

		// 6) TTL 설정
		Duration ttl = Duration.ofMinutes(signupProperties.getDraftTtlMinutes());
		log.info("Draft TTL 설정: {}분", signupProperties.getDraftTtlMinutes());

		// 7) Redis에 저장
		signupDraftRedisRepository.save(redisValue,ttl);
		log.info("Draft 정보를 Redis에 저장 완료: draftKey={}", draftKey);

		// 8) 응답 바디 구성
		CreateDraftResponse response = new CreateDraftResponse(
			SignupStep.TERMS_REQUIRED,
			new Flags(bundle),
			new Data(terms)
		);
		log.info("CreateDraftResponse 생성 완료");

		return new DraftCreationResult(draftKey, response);
	}

	public record DraftCreationResult(
		String draftKey,
		CreateDraftResponse response
	){}
}
