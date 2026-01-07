package com.cotato.itda.domain.sms.infra.solapi;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import com.cotato.itda.domain.sms.infra.solapi.config.SolapiFeignConfig;
import com.cotato.itda.domain.sms.infra.solapi.dto.SolapiSendRequest;
import com.cotato.itda.domain.sms.infra.solapi.dto.SolapiSendResponse;

@FeignClient(
	name = "solapiClient",
	url = "${solapi.base-url}",
	configuration = SolapiFeignConfig.class
)
public interface SolapiFeignClient {

	/**
	 * SOLAPI 메시지 발송 API
	 * POST /messages/v4/send-many/detail :contentReference[oaicite:6]{index=6}
	 * <p>
	 * 최소 필수: messages[].from, messages[].to, messages[].text :contentReference[oaicite:7]{index=7}
	 */
	@PostMapping("/messages/v4/send-many/detail")
	SolapiSendResponse sendManyDetail(SolapiSendRequest request);
}