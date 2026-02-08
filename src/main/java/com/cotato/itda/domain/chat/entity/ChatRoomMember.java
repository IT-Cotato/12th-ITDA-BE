package com.cotato.itda.domain.chat.entity;

import java.time.LocalDateTime;

import com.cotato.itda.domain.chat.enums.ChatSpeech;
import com.cotato.itda.domain.chat.enums.MemberRoomStatus;
import com.cotato.itda.domain.chat.exception.code.ChatErrorCode;
import com.cotato.itda.domain.chattopic.entity.ChatTopic;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.global.entity.BaseTimeEntity;
import com.cotato.itda.global.error.exception.BusinessException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
	name = "chat_room_member",
	uniqueConstraints = {
		@UniqueConstraint(name = "uq_chat_room_member_room", columnNames = {"member_id", "room_id"})
	},
	indexes = {
		@Index(name = "idx_chat_room_member_status_room", columnList = "member_id,status,room_id")
	}
)
public class ChatRoomMember extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// FK : member_id -> member.id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	// FK : room_id -> chat_room.id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "room_id", nullable = false)
	private ChatRoom room;

	@Column(name = "last_read_message_id")
	private Long lastReadMessageId;

	// 채팅방에 들어왔을 때 마지막 메시지의 시퀀스 번호
	@Column(name = "join_seq", nullable = false)
	private long joinSeq = 0L;

	// 마지막으로 읽은 메시지의 시퀀스 번호
	@Column(name = "last_read_seq", nullable = false)
	private long lastReadSeq = 0L;

	@Enumerated(EnumType.STRING)
	@Column(name = "chat_speech", length = 20)
	private ChatSpeech chatSpeech;

	//0~7, default=0 (선택하지 않으)
	@Min(0)
	@Max(7)
	@Column(name = "chat_frequency", nullable = false)
	private int chatFrequency = 0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_topic_id")
	private ChatTopic chatTopic;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private MemberRoomStatus status = MemberRoomStatus.ACTIVE;

	@Column(name = "inactive_at")
	private LocalDateTime inactiveAt;

	@Column(name = "notification_enabled", nullable = false)
	private boolean notificationEnabled = true;

	@Column(name = "background_image_url", length = 500)
	private String backgroundImageUrl;

	@Column(name = "ai_mode_enabled", nullable = false)
	private boolean aiModeEnabled = false;

	@Builder
	private ChatRoomMember(Member member, ChatRoom room) {
		this.member = member;
		this.room = room;
	}

	public static ChatRoomMember create(Member member, ChatRoom room) {
		if (member == null || room == null) {
			throw new BusinessException(ChatErrorCode.CHAT_ROOM_MEMBER_CREATE_FAIL);
		}
		return ChatRoomMember.builder()
			.member(member)
			.room(room)
			.build();
	}

	// 현재 멤버가 인지하는 마지막 읽은 메시지의 시퀀스 번호
	public long effectiveReadSeq() {
		return Math.max(this.joinSeq, this.lastReadSeq);
	}

	// 읽음 처리
	// 내가 메시지를 읽었거나/ 내가 보낸 메시지는 자동 읽음 처리해야 함
	// readSeq: 읽은 메시지의 시퀀스 번호
	// readMessageId: 읽은 메시지의 ID
	public void markRead(long readSeq, Long readMesssageId) {
		this.lastReadSeq = Math.max(this.lastReadSeq, readSeq);
		this.lastReadMessageId = readMesssageId;
	}

	public void kick(){
		this.status = MemberRoomStatus.KICKED;
		this.inactiveAt = LocalDateTime.now();
	}
	public void leave() {
		this.status = MemberRoomStatus.LEFT;
		this.inactiveAt = LocalDateTime.now();
	}

	// 채팅 주제 변경
	public void changeTopic(ChatTopic chatTopic) {
		this.chatTopic = chatTopic;
	}

	public void changeFrequency(int frequency) {
		if (frequency < 0 || frequency > 7) {
			throw new BusinessException(ChatErrorCode.CHAT_ROOM_MEMBER_CREATE_FAIL);
		}
		this.chatFrequency = frequency;
	}

	public void setAiModeEnabled(boolean enabled){
		if(this.status  != MemberRoomStatus.ACTIVE){
			throw new BusinessException(ChatErrorCode.CHAT_ROOM_MEMBER_CREATE_FORBIDDEN);
		}
		this.aiModeEnabled = enabled;
	}

	public void setNotificationEnabled(boolean enabled) {
		if (this.status != MemberRoomStatus.ACTIVE) {
			throw new BusinessException(ChatErrorCode.CHAT_ROOM_MEMBER_STATUS_INVALID);
		}
		this.notificationEnabled = enabled;
	}

	public void rejoin(long newJoinSeq) {
		if (this.status == MemberRoomStatus.KICKED) {
			throw new BusinessException(ChatErrorCode.CHAT_ROOM_MEMBER_CREATE_FORBIDDEN);
		}
		this.status = MemberRoomStatus.ACTIVE;
		this.inactiveAt = null;
		this.joinSeq = newJoinSeq;
	}
}
