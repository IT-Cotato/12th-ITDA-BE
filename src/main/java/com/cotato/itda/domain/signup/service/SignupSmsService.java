package com.cotato.itda.domain.signup.service;

import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cotato.itda.domain.signup.dto.request.SendOtpSmsRequest;
import com.cotato.itda.domain.signup.dto.response.SendOtpSmsResponse;
import com.cotato.itda.domain.signup.dto.SignupDraftRedisValue;
import com.cotato.itda.domain.signup.model.SignupStep;
import com.cotato.itda.domain.signup.repository.SignupDraftRedisRepository;
import com.cotato.itda.domain.sms.infra.solapi.SolapiFeignClient;
import com.cotato.itda.domain.sms.infra.solapi.config.SolapiProperties;
import com.cotato.itda.domain.sms.infra.solapi.dto.SolapiSendRequest;
import com.cotato.itda.global.error.constant.SignupErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupSmsService {

	private final SignupDraftRedisRepository signupDraftRedisRepository;

	@Value("${signup.otp.length:6}")
	private int otpLength;

	@Value("${signup.otp.ttl-seconds:180}")
	private long otpTtlSeconds;

	@Value("${signup.otp.resend-window-seconds:180}")
	private long resendWindowSeconds;

	private final SolapiProperties solapiProperties;
	private final SolapiFeignClient solapiFeignClient;
	// - 암호학적으로 안전한 난수 생성기
	// - OTP 생성 시 예측 불가능한 난수를 생성하여 보안성을 높임
	private final SecureRandom secureRandom = new SecureRandom();
	/**
	 * STEP3 동작 흐름
	 * <p>
	 * 1. draftKey 유효성/존재 확인
	 * 2. 현재 stetp 확인(OTP_REQUIRED에서만 허용)
	 * 3. 재전송 가능 시간 (resendAvailableAt) 체크
	 * 4. sms SendCound 상한 체크(무한 발송 방지)
	 * 5. phone 정규화(숫자만 남김) + 검증
	 * 6. OTP 생성
	 * 7. Redis에 OTP 해시/만료/카운트 저장
	 * 8. SOLAPI로 문자 발송
	 * 9. 응답 DTO 구성
	 */
	public SendOtpSmsResponse sendOtpSms(String draftKey, SendOtpSmsRequest request) {

		// 1. draftKey 유효성/존재 확인
		SignupDraftRedisValue draft = signupDraftRedisRepository.findByDraftKey(draftKey)
			.orElseThrow(() -> new BusinessException(SignupErrorCode.SIGNUP_DRAFT_NOT_FOUND));

		// 2. 현재 stetp 확인(OTP_REQUIRED에서만 허용)
		if (draft.step() != SignupStep.OTP_REQUIRED) {
			throw new BusinessException(SignupErrorCode.INVALID_SIGNUP_STEP);
		}

		draft = SignupDraftRedisValue.enableResendNowIfPossible(draft);

		// 3. 재전송 가능 시간 (resendAvailableAt) 체크
		// UTC 기준의 현재 시각 ( 예: 2026-01-01T12:00:00Z )
		// ZoneOffset.UTC를 붙인 이유
		// - 서버가 어느 지역에 있든지 상관없이 항상 UTC 기준으로 시간을 맞추기 위해
		// - 만약 인자가 없다면 서버 JVM의 기본 타임존 설정을 따름
		// - 서버가 특정 타임존에 설정되어 있을 경우, 의도치 않은 시간대 차이로 인해 시간 계산이 틀어질 수 있음
		OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

		// canSendSms가 false인 동안은 resendAvailableAt 이전에 재전송 불가
		if (!draft.otp().canSendSms() && draft.otp().resendAvailableAt() != null && now.isBefore(
			draft.otp().resendAvailableAt())) {
			throw new BusinessException(SignupErrorCode.OTP_RESEND_NOT_AVAILABLE_YET);
		}

		// 4. sms SendCound 상한 체크(무한 발송 방지)
		// remainingResendCount가 0 이하이면 재전송 불가
		if (draft.otp().remainingResendCount() <= 0) {
			throw new BusinessException(SignupErrorCode.OTP_RESEND_LIMIT_EXCEEDED);
		}

		// 5. phone 정규화(숫자만 남김) + 검증
		// SOLAPI는 특수문자 없이 숫자만 형태를 받아야 함
		String normalizedPhone = normalizePhone(request.phoneNumber());
		validatePhone(normalizedPhone);

		// OTP 코드 생성
		String otpCode = generateNumericOtp(otpLength);

		// todo: OTP 해시 저장
		// 현재는 "2341" 그대로 저장하지만, 실제로는 해시 변환 필요

		// 6. 만료/재전송 가능 시간 계산
		// OTP 만료 시간 = 현재 시각 + OTP TTL
		OffsetDateTime otpExpiresAt = now.plusSeconds(otpTtlSeconds); // 3분 후 만료
		log.info("OTP Expires At: {}", otpExpiresAt);
		// 현재는 만료 지나면 재전송 가능 => resendAvailableAt = otpExpiresAt
		OffsetDateTime resendAvailableAt = now.plusSeconds(resendWindowSeconds);
		log.info("OTP Resend Available At: {}", resendAvailableAt);

		//  실패 시 롤백용 이전 상태
		SignupDraftRedisValue beforeReserveDraft = draft;
		// 7. OTP 생성
		SignupDraftRedisValue updated = draft.onSmsSent(
			normalizedPhone,
			resendAvailableAt,
			otpExpiresAt,
			otpCode
		);

		// 7. Redis에 OTP 해시/만료/카운트 저장
		signupDraftRedisRepository.updatePreserveTtl(updated);
		// 8. SOLAPI로 문자 발송
		try {
			String text = "[심톡] 인증번호는 " + otpCode + " 입니다. " + (otpTtlSeconds / 60) + "분 내 입력해주세요.";

			log.info("Sending OTP SMS to {}: {}", normalizedPhone, text);
			SolapiSendRequest solapiReq = new SolapiSendRequest(
				List.of(new SolapiSendRequest.Message(
					solapiProperties.defaultFrom(),
					normalizedPhone,
					text
				))
			);
			log.info("SOLAPI Request: {}", solapiReq.toString());

			solapiFeignClient.sendManyDetail(solapiReq);
			log.info("OTP SMS sent successfully to {}", normalizedPhone);
		} catch (FeignException ex) {
			// 실패하면 사용자 입장에서 "보낸 적 없다"가 되어야 하므로,
			// 그래서 canSendSms를 true로 풀고, otp 정보를 무효화해 재시도 가능하게 만드는 전략을 쓴다.
			log.info("Failed to send OTP SMS to {}: {}", normalizedPhone, ex.getMessage());
			try {
				signupDraftRedisRepository.updatePreserveTtl(beforeReserveDraft);
			} catch (Exception rollbackEx) {
				// 롤백마저 실패하면 사용자는 쿨타임 걸린 것처럼 보일 수 있다.
				// 운영 대응을 위해 로그를 남긴다.
				log.error("OTP send failed and rollback also failed. draftKey={}", draftKey, rollbackEx);
			}

			throw new BusinessException(SignupErrorCode.OTP_SMS_SEND_FAILED, Map.of("solapiStatus", ex.status()));
		}

		// 9) 응답
		return new SendOtpSmsResponse(
			"OTP_REQUIRED",
			new SendOtpSmsResponse.Flags(
				updated.otp().canSendSms(),
				updated.otp().resendAvailableAt(),
				updated.otp().otpExpiresAt(),
				updated.otp().smsSendCount(),
				updated.otp().remainingResendCount(),
				updated.otp().remainingOtpAttempts()
			)
		);
	}

	public String normalizePhone(String phoneNumber) {
		if (phoneNumber == null) {
			return null;
		}
		// 숫자 이외의 문자 제거
		return phoneNumber.replaceAll("[^0-9]", "");
	}

	public void validatePhone(String phoneNumber) {
		if(phoneNumber == null || phoneNumber.isBlank()){
			//핸드폰 번호는 필수 입력 에러
			throw new BusinessException(SignupErrorCode.OTP_PHONE_NUMBER_REQUIRED);
		}
		// 한국 휴대폰 번호 형식 검증 (예: 01012345678)
		if(!phoneNumber.matches("^010\\d{8}$")){
			throw new BusinessException(SignupErrorCode.OTP_PHONE_NUMBER_REQUIRED, Map.of("유효한 한국 휴대폰 번호 형식이 아닙니다.", phoneNumber));
		}
	}

	/**
	 *  숫자 OTP (일회용 인증번호) 생성
	 *  - length=4  → 0000 ~ 9999 범위의 문자열
	 */
	private String generateNumericOtp(int length) {
		// length=4이면 0000~9999
		int bound = (int) Math.pow(10, length);
		// 난수 생성: 0 이상 bound 미만의 정수
		// length=4 라면 value가 0, 1, 2, ..., 9999 중 하나
		int value = secureRandom.nextInt(bound);

		// 0으로 시작하는 경우를 대비해, 고정 길이 문자열로 포맷팅
		// 예: length=4, value=7  → "0007"
		// 예: length=4, value=123 → "0123"
		// 예: length=4, value=9999 → "9999"
		return String.format("%0" + length + "d", value);
	}
}
