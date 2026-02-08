package com.cotato.itda.domain.chattopic.entity;

import com.cotato.itda.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(
	name = "chat_topic_template",
	indexes = {
		@Index(name = "idx_chat_topic_template_name", columnList = "topic_id, is_active")
	}
)
public class ChatTopicTemplate extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// FK로 topic 연결
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "topic_id", nullable = false)
	private ChatTopic topic;

	@Column(name = "template_text", length = 500, nullable = false)
	private String templateText;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Column(name = "priority", nullable = false)
	private int priority = 0;

	@Builder
	private ChatTopicTemplate(ChatTopic topic, String templateText, boolean active, int priority) {
		this.topic = topic;
		this.templateText = templateText;
		this.active = active;
		this.priority = priority;
	}
}
