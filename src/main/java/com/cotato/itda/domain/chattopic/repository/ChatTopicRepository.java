package com.cotato.itda.domain.chattopic.repository;

import com.cotato.itda.domain.chattopic.entity.ChatTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatTopicRepository extends JpaRepository<ChatTopic, Long> {
    List<ChatTopic> findAllByCodeIn(List<String> codes);

    List<ChatTopic> findAllByIsActiveTrue();
}
