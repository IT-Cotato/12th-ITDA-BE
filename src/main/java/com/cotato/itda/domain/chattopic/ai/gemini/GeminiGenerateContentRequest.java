package com.cotato.itda.domain.chattopic.ai.gemini;

import java.util.List;

public record GeminiGenerateContentRequest(
	List<Content> contents,
	GenerationConfig generationConfig
) {
	public record Content(
		// 모델에게 전달되는 프롬프트 텍스트의 역할이 들어간다 (보통 "user") (예: "user", "system", "assistant" 등)
		String role,
		List<Part> parts
	){}
	// 최종적으로 모델에게 전달되는 프롬프트 텍스트가 들어간다
	public record Part(
		String text
	){}

	public record GenerationConfig(
		// 모델이 생성할 텍스트의 다양성을 제어하는 매개변수 (예: 0.8, 1.0 등)
		// - 낮을수록 더 결정적이고 일관된 출력을 생성하며, 높을수록 더 창의적이고 다양한 출력을 생성한다.
		Double temperature,
		// 모델이 생성할 텍스트의 최대 토큰 수를 제한하는 매개변수 (예: 512, 1024 등)
		Integer maxOutputTokens
	){}
}

/**
 * {
 *   "contents": [
 *     {
 *       "role": "user",
 *       "parts": [
 *         {
 *           "text": "주제: WEATHER\n기존 문장과 동일/유사한 문장은 제외하고 새로운 문구 8개를 추천해줘.\n- 한 문장으로 짧게\n- 질문형/공감형/상황공유형 섞기\n\n기존 문장 목록:\n- 오늘 비 올까?\n- 지금 기온 어때?\n- 우산 챙겨야 하나?\n\n출력은 문장만 줄바꿈으로 제공해줘."
 *         }
 *       ]
 *     }
 *   ],
 *   "generationConfig": {
 *     "temperature": 0.8,
 *     "maxOutputTokens": 512
 *   }
 * }
 */
