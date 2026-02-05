package com.cotato.itda.domain.chat.repository.dto;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cotato.itda.domain.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
}
