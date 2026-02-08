package com.cotato.itda.domain.chattopic.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cotato.itda.domain.chattopic.entity.ChatTopicTemplate;

public interface ChatTopicTemplateRepository extends JpaRepository<ChatTopicTemplate,Long> {
	List<ChatTopicTemplate> findAllByTopic_IdAndActiveTrueOrderByPriorityAsc(Long chatTopicId);
}
