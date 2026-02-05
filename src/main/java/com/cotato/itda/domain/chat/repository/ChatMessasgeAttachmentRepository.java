package com.cotato.itda.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cotato.itda.domain.chat.entity.ChatMessageAttachment;

public interface ChatMessasgeAttachmentRepository extends JpaRepository<ChatMessageAttachment,Long> {
}
