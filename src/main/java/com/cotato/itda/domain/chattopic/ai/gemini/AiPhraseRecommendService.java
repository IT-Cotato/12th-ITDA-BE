package com.cotato.itda.domain.chattopic.ai.gemini;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cotato.itda.domain.chattopic.dto.req.AiPhraseRecommendRequest;
import com.cotato.itda.domain.chattopic.dto.res.AiPhraseRecommendResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiPhraseRecommendService {
	private final GeminiFeignClient geminiFeignClient;
	private final ObjectMapper objectMapper;

	@Value("${gemini.model}")
	private String model;

	@Value("${gemini.api-key}")
	private String apiKey;

	@Value("${gemini.temperature:0.8}")
	private double temperature;

	@Value(("${gemini.max-output-tokens:512}"))
	private int maxOutputTokens;

	public AiPhraseRecommendResponse recommend(AiPhraseRecommendRequest request){
		int desired = request.desiredCount();

		Set<String> existingNormalized = request.existingPhrases().stream()
			.filter(Objects::nonNull)
			.map(this::normalize)
			.collect(Collectors.toSet());

		String prompt = buildPrompt(request.topic(), request.existingPhrases(),desired);

		GeminiGenerateContentRequest body = new GeminiGenerateContentRequest(
			List.of(new GeminiGenerateContentRequest.Content(
				"USER",
				List.of(new GeminiGenerateContentRequest.Part(prompt))
			)),
			new GeminiGenerateContentRequest.GenerationConfig(temperature, maxOutputTokens)
		);

		GeminiGenerateContentResponse response = geminiFeignClient.generateContent(
			model,
			apiKey,
			body
		);
		log.info("Gemini response: {}", response);


		String text =extractFirstText(response);
		List<String> parsed = parseJsonArrayOrFallback(text);

		// 기존 문장 제외 + 중복 제거 + 길이/형식 간단 정리
		List<String> filtered = new ArrayList<>();
		Set<String> seen = new HashSet<>();

		for (String p : parsed) {
			if (p == null) continue;
			String trimmed = p.trim();
			if (trimmed.isEmpty()) continue;

			String n = normalize(trimmed);
			if (existingNormalized.contains(n)) continue;
			if (!seen.add(n)) continue;

			filtered.add(trimmed);
			if (filtered.size() >= desired) break;
		}

		return new AiPhraseRecommendResponse(request.topic(), filtered);
	}

	private String buildPrompt(String topic, List<String> existing, int desired){

		StringBuilder banned = new StringBuilder();
		for(int i=0;i<existing.size();i++){
			banned.append(i+1).append(") ").append(existing.get(i)).append("\n");
		}

		//json 배열만 출력 강제
		int ask = Math.max(desired+6,desired); // 여유분 확보
		return ""
			+ "너는 한국어 모바일 채팅에서 쓸 '짧은 추천 문구' 생성기야.\n"
			+ "주제: " + topic + "\n\n"
			+ "아래 '기존 문구'는 이미 사용 중이야. 절대로 그대로 포함하지 말고,\n"
			+ "의미가 거의 같은 말투/단어만 바꾼 변형도 만들지 마.\n"
			+ "[기존 문구]\n"
			+ banned
			+ "\n"
			+ "[요구사항]\n"
			+ "- 완전히 새로운 문구 " + ask + "개 생성\n"
			+ "- 각 문구는 12~25자 내외의 존댓말(부드럽게)\n"
			+ "- 이모지는 문구당 최대 1개\n"
			+ "- 안전하고 일상적인 내용만\n"
			+ "- 결과는 반드시 JSON 배열만 출력 (예: [\"문장1\",\"문장2\",...])\n";
	}
	private String extractFirstText(GeminiGenerateContentResponse res) {
		// 안전하게 널 체크
		if (res == null || res.candidates() == null || res.candidates().isEmpty()) return "";
		// 첫 번째 후보 가져오기
		GeminiGenerateContentResponse.Candidate c = res.candidates().get(0);
		// 콘텐츠와 파트가 널인지 체크
		if (c == null || c.content() == null || c.content().parts() == null || c.content().parts().isEmpty()) return "";
		// 첫 번째 파트의 텍스트 반환
		String t = c.content().parts().get(0).text();
		return (t == null) ? "" : t.trim();
	}

	/**
	 * Gemini 응답을 List<String>로 안정적으로 파싱하는 유틸 메서드
	 *
	 * 1) 모델이 JSON 배열 형태로 주면 그대로 파싱한다.
	 * 2) 설명/코드블럭이 섞여 있으면, '[' ~ ']' 구간만 잘라 JSON 배열로 재시도한다.
	 * 3) 그래도 안되면, 줄바꿈과 불릿 기반으로 파싱한다.
	 */
	private List<String> parseJsonArrayOrFallback(String text) {
		if (text == null) return List.of();

		// 1) JSON 배열 파싱 시도
		try {
			return objectMapper.readValue(text, new TypeReference<List<String>>() {});
		} catch (Exception ignored) {
		}

		// 2) 혹시 코드블록/설명 섞인 경우: JSON 배열로 보이는 구간만 잘라 재시도
		int s = text.indexOf('[');
		int e = text.lastIndexOf(']');
		if (s >= 0 && e > s) {
			String cut = text.substring(s, e + 1);
			try {
				return objectMapper.readValue(cut, new TypeReference<List<String>>() {});
			} catch (Exception ignored) {
			}
		}

		// 3) fallback: 줄바꿈/불릿 기반
		return Arrays.stream(text.split("\n"))
			.map(line -> line.replaceFirst("^[-*\\d+.\\s]+", "").trim())
			.filter(line -> !line.isEmpty())
			.collect(Collectors.toList());
	}

	private String normalize(String str){
		// 모든 공백 문자를 단일 공백으로 대체하고, 앞뒤 공백 제거 후, 소문자로 변환
		// Locale.ROOT를 사용하여 언어에 독립적인 소문자 변환 수행
		// 예를 들어 "  Hello   World  " -> "hello world"
		return str.replaceAll("\\s+", " ").trim().toLowerCase(Locale.ROOT);
	}
}
