package com.cotato.itda.domain.chattopic.ai.gemini;

import java.util.List;

public record GeminiGenerateContentResponse (
	List<Candidate> candidates
){
	public record Candidate(Content content){}
	public record Content(String role, List<Part> parts){}
	public record Part(String text){}
}
