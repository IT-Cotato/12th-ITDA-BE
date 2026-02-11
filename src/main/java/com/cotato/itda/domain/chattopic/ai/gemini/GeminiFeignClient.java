package com.cotato.itda.domain.chattopic.ai.gemini;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
	name = "geminiFeignClient",
	url = "${gemini.base-url}"
)
public interface GeminiFeignClient {

	@PostMapping(
		// 실제 호출 엔드포인트 경로
		value = "/v1beta/models/{model}:generateContent",
		// 요청과 응답이 JSON 형식임을 명시
		consumes = "application/json"
	)
	GeminiGenerateContentResponse generateContent(
		// ex) "gemini-1.5-pro"와 같은 모델 이름을 경로 변수로 전달
		@PathVariable("model") String model,
		// API 키를 쿼리 매개변수로 전달
		@RequestParam("key") String apiKey,
		// 요청 본문에 GeminiGenerateContentRequest 객체를 JSON 형식으로 전달
		@RequestBody GeminiGenerateContentRequest request
	);
}
