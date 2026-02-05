package com.cotato.itda.domain.chat.entity;

import java.time.LocalDateTime;

import com.cotato.itda.domain.chat.enums.LastMessageType;
import com.cotato.itda.domain.chat.enums.RoomType;
import com.cotato.itda.global.entity.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "chat_room",
	indexes = {
		@Index(name = "idx_chat_room_last_message_at", columnList = "last_message_at")

	}
)
public class ChatRoom extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "room_name", length = 255)
	private String roomName;

	@Column(name = "last_message_id")
	private Long lastMessageId;

	@Column(name = "last_message_seq")
	private Long lastMessageSeq;

	// 최근 메시지 생성 시각(DATETIME)
	@Column(name = "last_message_at")
	private LocalDateTime lastMessageAt;

	@Column(name = "last_message_preview", length = 255)
	private String lastMessagePreview;

	@Enumerated(EnumType.STRING)
	@Column(name = "last_message_type", length = 20)
	private LastMessageType lastMessageType;

	@Enumerated(EnumType.STRING)
	@Column(name = "room_type", nullable = false, length = 20)
	private RoomType roomType;

	@Column(name = "direct_member_low_id")
	private Long directMemberLowId;

	@Column(name = "direct_member_high_id")
	private Long directMemberHighId;

	@Builder
	private ChatRoom(String roomName, RoomType roomType, Long directMemberLowId, Long directMemberHighId) {
		this.directMemberLowId = directMemberLowId;
		this.directMemberHighId = directMemberHighId;
		this.roomName = roomName;
		this.roomType = roomType;
	}

	public static ChatRoom createDirectRoom(Long myId, Long opponentId) {
		Long lowId = Math.min(myId, opponentId);
		Long highId = Math.max(myId, opponentId);
		return ChatRoom.builder()
			.roomType(RoomType.DIRECT)
			.directMemberLowId(lowId)
			.directMemberHighId(highId)
			.build();
	}

	public void updateLastMessageCache(
		Long lastMessageId,
		Long lastMessageSeq,
		LocalDateTime lastMessageAt,
		String lastMessagePreview,
		LastMessageType lastMessageType
	) {
		this.lastMessageId = lastMessageId;
		this.lastMessageSeq = lastMessageSeq;
		this.lastMessageAt = lastMessageAt;
		this.lastMessagePreview = lastMessagePreview;
		this.lastMessageType = lastMessageType;
	}
}
