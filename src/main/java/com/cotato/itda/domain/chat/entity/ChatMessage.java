package com.cotato.itda.domain.chat.entity;

import java.time.LocalDateTime;

import com.cotato.itda.domain.chat.enums.MessageType;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
	name = "chat_message",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_chat_message_room_seq", columnNames = {"chat_room_id", "message_seq"})
	}
)
public class ChatMessage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// ChatMessage의 여러 행이 같은 sender_id를 가질 수 있으므로 ManyToOne
	// FK : sender_id -> member.id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sender_id", nullable = false)
	private Member sender;

	// ChatMessage의 여러 행이 같은 chat_room_id를 가질 수 있으므로 ManyToOne
	// FK : chat_room_id -> chat_room.id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom room;

	// 채팅방 내에서 메시지의 순서를 나타내는 시퀀스 번호
	@Column(name = "message_seq", nullable = false)
	private long messageSeq;

	@Enumerated(EnumType.STRING)
	@Column(name = "message_type", nullable = false, length = 20)
	private MessageType messageType;

	//@Lob: 큰 텍스트 데이터를 저장할 때 사용
	// 첨부면 content null가능
	@Lob
	@Column(name = "message_content")
	private String content;

	@Column(name = "edited_at")
	private LocalDateTime editedAt;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private ChatMessage parentMessage;

	@Builder
	private ChatMessage(Member sender, ChatRoom room, long messageSeq, MessageType messageType, String content, ChatMessage parentMessage) {
		this.sender = sender;
		this.room = room;
		this.messageSeq = messageSeq;
		this.messageType = messageType;
		this.content = content;
        this.parentMessage = parentMessage;
	}

	public void markAsEdited(String newContent, LocalDateTime editedAt) {
		this.content = newContent;
		this.editedAt = editedAt;
	}

	public void markAsDeleted(LocalDateTime deletedAt) {
		this.deletedAt = deletedAt;
	}

	// 채팅 메시지 생성
	public static ChatMessage createChatMessage(Member sender, ChatRoom room, long messageSeq, MessageType messageType,
		String content, ChatMessage parentMessage) {
		return ChatMessage.builder()
			.sender(sender)
			.room(room)
			.messageSeq(messageSeq)
			.messageType(messageType)
			.content(content)
            .parentMessage(parentMessage)
			.build();
	}

}

