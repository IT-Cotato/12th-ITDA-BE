package com.cotato.itda.domain.signup.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.cotato.itda.domain.signup.dto.SignupDraftRedisValue;
import com.cotato.itda.domain.signup.dto.request.SubmitTermsRequest;
import com.cotato.itda.domain.signup.dto.response.SubmitTermsResponse;
import com.cotato.itda.domain.signup.model.SignupStep;
import com.cotato.itda.domain.signup.repository.SignupDraftRedisRepository;
import com.cotato.itda.domain.signup.repository.TermsPolicyRepository;
import com.cotato.itda.global.error.constant.SignupErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * STEP 2

 * 1) Redis draft 조회
 * 2) Step 검증(TERMS_REQUIRED인지)
 * 3) MySQL 약관 정책 조회(정책 버전 고정)
 * 4) 필수 약관 동의 검증
 * 5) DB terms 기준으로 consents 정규화(누락/변조 방지)
 * 6) draft.onTermsSubmitted(...)로 OTP_REQUIRED 전이
 * 7) Redis 업데이트(TTL 유지)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignupTermsService {

	private final TermsPolicyRepository termsPolicyRepository;
	private final SignupDraftRedisRepository signupDraftRedisRepository;

	public SubmitTermsResponse submitTerms(String draftKey, SubmitTermsRequest request){

		// 1. Redis에서 Draft 조회
		SignupDraftRedisValue draft = signupDraftRedisRepository.findByDraftKey(draftKey)
			.orElseThrow(()->new BusinessException(SignupErrorCode.SIGNUP_DRAFT_NOT_FOUND));

		log.info("레디스 에서 조회된 Signup Draft: {}", draft);
		// 2. step 검증
		if(draft.step()!=SignupStep.TERMS_REQUIRED){
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}
		log.info("step 검증 통과, 현재 step: {}", draft.step());

		// 3. 요청 contents를 검증하고 정규화된 목록으로 변환
		List<SignupDraftRedisValue.TermsState.TermsConsent> normalizedConsents =
			normalizeAndValidateConsents(draft, request);
		log.info("약관 동의 항목 검증 및 정규화 완료: {}", normalizedConsents);

		SignupDraftRedisValue.TermsState updatedTermsState = new SignupDraftRedisValue.TermsState(normalizedConsents);
		log.info("업데이트된 TermsState 생성 완료: {}", updatedTermsState);

		//4. draft 상태 전이
		SignupDraftRedisValue updatedDraft = draft.onTermsSubmitted(updatedTermsState);
		log.info("Draft 상태 전이 완료, 새로운 step: {}", updatedDraft.step());

		//5. Redis 업데이트 (TTL 유지)
		signupDraftRedisRepository.updatePreserveTtl(updatedDraft);
		log.info("Redis Draft 업데이트 완료: {}", updatedDraft);

		return responseFromOptInit();

	}

	// STEP2 성공 응답은 항상 OTP 초기 플래그로 고정
	private SubmitTermsResponse responseFromOptInit(){
		return new SubmitTermsResponse(
			SignupStep.OTP_REQUIRED,
			new SubmitTermsResponse.Flags(
				true, // canSendSms
				0,   // smsSendCount
				3   // remainingOptAttempts
			)
		);
	}

	private List<SignupDraftRedisValue.TermsState.TermsConsent> normalizeAndValidateConsents(
		SignupDraftRedisValue draft,
		SubmitTermsRequest request
	){
		// 1) Draft에 고정된 버전으로 약관 항목들 조회
		String bundleType = draft.policy().bundleType();
		String bundleVersion = draft.policy().bundleVersion();

		List<TermsPolicyRepository.TermsPolicyItem> dbTermsItems =
			termsPolicyRepository.getTermsByBundle(bundleType, bundleVersion);

		if(dbTermsItems==null|| dbTermsItems.isEmpty()){
			throw new BusinessException(SignupErrorCode.TERMS_ITEM_NOT_FOUND);
		}
		// 2) 요청 consents Map으로 변환 (key:code ,value: Consent)
		/**
		 * 예시:
		 * {"TERMS_OF_SERVICE": Consent{code='TERMS_OF_SERVICE', version='v1.0', agreed=true},
		 *  "PRIVACY_POLICY": Consent{code='PRIVACY_POLICY', version='v1.0', agreed=true}
		 *  }
		 */
		Map<String, SubmitTermsRequest.Consent> requestByCode = new HashMap<>();
		for(SubmitTermsRequest.Consent consent:request.consents()){
			if(requestByCode.containsKey(consent.code())){
				throw new BusinessException(SignupErrorCode.DUPLICATE_CONSENT_CODE);
			}
			requestByCode.put(consent.code(),consent);
		}

		// 3) DB 약관 항목들 Set으로 변환
		/**
		 * 예시:
		 * {"TERMS_OF_SERVICE", "PRIVACY_POLICY"}
		 */
		Set<String> dbCodes = new HashSet<>();
		for(TermsPolicyRepository.TermsPolicyItem item:dbTermsItems){
			dbCodes.add(item.code());
		}

		// 개수 비교
		if(requestByCode.size()>dbCodes.size()){
			throw new BusinessException(SignupErrorCode.EXTRA_CONSENT_ITEMS);
		}
		if(requestByCode.size()<dbCodes.size()){
			throw new BusinessException(SignupErrorCode.MISSING_CONSENT_ITEMS);
		}

		// 4) 요청한 약관 항목 코드들이 DB에 존재하는지 검증
		for(String code: requestByCode.keySet()){
			if(!dbCodes.contains(code)){
				throw new BusinessException(SignupErrorCode.TERMS_ITEM_NOT_FOUND);
			}
		}

		// 5) DB 목록 기준으로 정규화 + 필수 검증 + version 검증
		List<SignupDraftRedisValue.TermsState.TermsConsent> normalized = new ArrayList<>();

		for(TermsPolicyRepository.TermsPolicyItem term : dbTermsItems){
			SubmitTermsRequest.Consent received = requestByCode.get(term.code());

			// agreed 결정:
			// - 요청에 있고 agreed=true이면 true
			// - 없거나 false 이면 false
			boolean agreed = received!=null && received.agreed();

			// 필수 약관 검증
			if(term.required()&&!agreed){
				throw new BusinessException(SignupErrorCode.MISSING_REQUIRED_CONSENT);
			}

			normalized.add(
				new SignupDraftRedisValue.TermsState.TermsConsent(
					term.id(),
					term.code(),
					term.version(),
					agreed
				)
			);
		}
		return normalized;
	}


}
