package com.cotato.itda.domain.chattopic.service.query;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.chattopic.dto.res.ChatTopicTemplateResponse;
import com.cotato.itda.domain.chattopic.entity.ChatTopic;
import com.cotato.itda.domain.chattopic.entity.ChatTopicTemplate;
import com.cotato.itda.domain.chattopic.exception.code.ChatTopicErrorCode;
import com.cotato.itda.domain.chattopic.repository.ChatTopicRepository;
import com.cotato.itda.domain.chattopic.repository.ChatTopicTemplateRepository;
import com.cotato.itda.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatTopicTemplateService {

	private final ChatTopicRepository chatTopicRepository;
	private final ChatTopicTemplateRepository chatTopicTemplateRepository;

	public ChatTopicTemplateResponse getChatTopicTemplates(String topicCode) {
		ChatTopic chatTopic = chatTopicRepository.findByCodeAndIsActiveTrue(topicCode)
			.orElseThrow(() -> new BusinessException(ChatTopicErrorCode.CHAT_TOPIC_NOT_FOUND));

		Long topicId = chatTopic.getId();

		List<ChatTopicTemplate> items = chatTopicTemplateRepository.findAllByTopic_IdAndActiveTrueOrderByPriorityAsc(
			topicId);

		List<String> templateContents = items.stream()
			.map(template -> template.getTemplateText())
			.toList();
		return new ChatTopicTemplateResponse(templateContents);
	}
}
