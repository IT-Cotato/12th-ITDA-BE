package com.cotato.itda.domain.chattopic.dto.res;

import java.util.List;

public record AiPhraseRecommendResponse(
	String topicCode,
	List<String> phrases
) {
}
