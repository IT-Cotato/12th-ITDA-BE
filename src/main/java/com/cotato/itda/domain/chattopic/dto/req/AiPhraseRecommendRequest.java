package com.cotato.itda.domain.chattopic.dto.req;

import java.util.List;

public record AiPhraseRecommendRequest (
	List<String> existingPhrases,
	String topic,
	Integer count
){
	public int desiredCount(){
		return count != null ? count : 6;
	}
}
