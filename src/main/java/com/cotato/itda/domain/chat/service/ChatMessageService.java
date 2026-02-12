
package com.cotato.itda.domain.chat.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.chat.controller.dto.ChatAttachmentRequest;
import com.cotato.itda.domain.chat.controller.dto.ChatMessageItemDto;
import com.cotato.itda.domain.chat.controller.dto.ChatMessageSliceResponse;
import com.cotato.itda.domain.chat.entity.ChatMessage;
import com.cotato.itda.domain.chat.entity.ChatMessageAttachment;
import com.cotato.itda.domain.chat.entity.ChatRoom;
import com.cotato.itda.domain.chat.entity.ChatRoomMember;
import com.cotato.itda.domain.chat.enums.AttachmentStatus;
import com.cotato.itda.domain.chat.enums.LastMessageType;
import com.cotato.itda.domain.chat.enums.MemberRoomStatus;
import com.cotato.itda.domain.chat.enums.MessageType;
import com.cotato.itda.domain.chat.exception.code.ChatErrorCode;
import com.cotato.itda.domain.chat.repository.ChatMessageAttachmentRepository;
import com.cotato.itda.domain.chat.repository.ChatRoomMemberRepository;
import com.cotato.itda.domain.chat.repository.ChatRoomQueryRepository;
import com.cotato.itda.domain.chat.repository.ChatRoomRepository;
import com.cotato.itda.domain.chat.repository.dto.ChatMessageRepository;
import com.cotato.itda.domain.chat.repository.dto.MessageRow;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.websocket.dto.ChatEventEnvelope;
import com.cotato.itda.domain.websocket.dto.ChatEventType;
import com.cotato.itda.domain.websocket.dto.ChatRoomMessageDto;
import com.cotato.itda.domain.websocket.dto.ChatSendMessageRequest;
import com.cotato.itda.domain.websocket.dto.RoomCreatedData;
import com.cotato.itda.domain.websocket.dto.RoomListUpdatedData;
import com.cotato.itda.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

	private final ChatRoomRepository chatRoomRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomQueryRepository chatRoomQueryRepository;
	private final ChatMessageAttachmentRepository chatMessageAttachmentRepository;
	private final MemberRepository memberRepository;

	private final SimpMessagingTemplate messagingTemplate;

	@Transactional
	public void sendMessage(Long senderMemberId, ChatSendMessageRequest req) {

		// ===== [0] 요청 들어옴 =====
		log.info(
			"[채팅 SEND 시작] senderMemberId={}, req.roomId={}, req.opponentMemberId={}, req.messageType={}, contentLen={}",
			senderMemberId,
			req.roomId(),
			req.opponentMemberId(),
			req.messageType(),
			(req.content() == null ? 0 : req.content().length())
		);

		// 요청 검증: roomId, opponentMemberId 둘 다 null 불가
		if (req.roomId() == null && req.opponentMemberId() == null) {
			log.warn("[채팅 SEND 거절] roomId/opponentMemberId 둘 다 null 입니다. senderMemberId={}", senderMemberId);
			throw new BusinessException(ChatErrorCode.INVALID_REQUEST_BOTH_ROOMID_OPPONENTID_NULL);
		}

		validateSendRequest(req);
		// ===== [1] 발신자 조회 =====
		log.info("[발신자 조회] senderMemberId={} 조회 시작", senderMemberId);
		Member sender = memberRepository.findById(senderMemberId)
			.orElseThrow(() -> {
				log.warn("[발신자 조회 실패] senderMemberId={} 없음", senderMemberId);
				return new BusinessException(ChatErrorCode.CHAT_MEMBER_NOT_FOUND);
			});
		log.info("[발신자 조회 완료] senderMemberId={}, nicknameOrId={}",
			senderMemberId,
			sender.getId()
		);

		// ===== [2] 채팅방 조회 or 생성 =====
		boolean isRoomCreatedNow = false;

		Long roomId = req.roomId();
		Long opponentMemberId = req.opponentMemberId();

		log.info("[방 결정 시작] 입력 roomId={}, opponentMemberId={}", roomId, opponentMemberId);

		if (roomId == null) {
			log.info("[방 resolve 시작] senderMemberId={}, opponentMemberId={}", senderMemberId, opponentMemberId);

		Long resolvedRoomId = chatRoomQueryRepository.resolveDirectRoomId(senderMemberId, opponentMemberId)
				.orElse(null);

			if (resolvedRoomId != null) {
				roomId = resolvedRoomId;
				isRoomCreatedNow = false;
				log.info("[방 resolve 성공] 기존 방 발견. roomId={}", roomId);
			} else {
				log.info("[방 resolve 실패] 기존 방 없음 -> 신규 방 생성 시작");

				ChatRoom newRoom = chatRoomRepository.save(
					ChatRoom.createDirectRoom(senderMemberId, opponentMemberId)
				);

				roomId = newRoom.getId();
				isRoomCreatedNow = true;

				log.info("[신규 방 생성 완료] roomId={}", roomId);

				Member opponent = memberRepository.findById(opponentMemberId)
					.orElseThrow(() -> {
						log.warn("[상대 조회 실패] opponentMemberId={} 없음 (방 생성 중)", opponentMemberId);
						return new BusinessException(ChatErrorCode.CHAT_MEMBER_NOT_FOUND);
					});

				chatRoomMemberRepository.save(ChatRoomMember.create(sender, newRoom));
				chatRoomMemberRepository.save(ChatRoomMember.create(opponent, newRoom));

				log.info("[방 멤버십 생성 완료] roomId={}, senderMemberId={}, opponentMemberId={}",
					roomId, senderMemberId, opponentMemberId
				);
			}
		} else {
			log.info("[방 결정 완료] 요청에 roomId 포함. roomId={}", roomId);
		}


		final Long lockedRoomId = roomId; // 람다 내에서 사용하기 위한 final 변수

		ChatRoomMember myMembership = chatRoomMemberRepository
			.findByRoomIdAndMemberId(roomId, senderMemberId)
			.orElseThrow(() -> new BusinessException(ChatErrorCode.CHAT_MEMBER_NOT_FOUND));

		if (myMembership.getStatus() == MemberRoomStatus.KICKED) {
			throw new BusinessException(ChatErrorCode.CHAT_ROOM_MEMBER_CREATE_FORBIDDEN);
		}


		log.info("[권한 체크 성공] senderMemberId={} 는 roomId={} ACTIVE 멤버", senderMemberId, roomId);

		// ===== [4] seq 발급 + 메시지 저장 (비관적 락) =====
		log.info("[방 row 락 획득 시도] roomId={}", roomId);

		ChatRoom room = chatRoomRepository.findByIdForUpdate(roomId)
			.orElseThrow(() -> {
				log.warn("[방 조회 실패] roomId={} 없음", lockedRoomId);
				return new BusinessException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
			});

		Long lastMessageSeq = room.getLastMessageSeq();
		long lastSeq = (lastMessageSeq == null) ? 0L : lastMessageSeq;
		long nextSeq = lastSeq + 1;

		if (myMembership.getStatus() == MemberRoomStatus.LEFT) {
			myMembership.rejoin(nextSeq);
		}
		log.info("[seq 발급] roomId={}, lastSeq={}, nextSeq={}", roomId, lastSeq, nextSeq);

		ChatMessage message = ChatMessage.createChatMessage(
			sender,
			room,
			nextSeq,
			req.messageType(),
			req.content()
		);

		chatMessageRepository.save(message);
		log.info("[메시지 저장 완료] roomId={}, messageId={}, messageSeq={}, senderMemberId={}, messageType={}",
			roomId, message.getId(), message.getMessageSeq(), senderMemberId, req.messageType()
		);

		ChatMessageAttachment savedAttachment = null;
		if (req.messageType() == MessageType.ATTACHMENT) {

			ChatAttachmentRequest a = req.attachment();

			ChatMessageAttachment attachment = ChatMessageAttachment.builder()
				.message(message)
				.attachmentType(a.attachmentType())
				.objectKey(a.objectKey())
				.mimeType(a.mimeType())
				.sizeBytes(a.sizeBytes())
				.durationMs(a.durationMs())
				.status(AttachmentStatus.READY) // 초기 상태는 READY
				.build();

			savedAttachment = chatMessageAttachmentRepository.save(attachment);
		}
		// ===== [5] 내 메시지 자동 읽음 처리 =====
		myMembership.markRead(nextSeq, message.getId());
		log.info("[읽음 처리] senderMemberId={} 가 보낸 메시지 자동 읽음 처리. roomId={}, readSeq={}, readMessageId={}",
			senderMemberId, roomId, nextSeq, message.getId()
		);

		// ===== [6] room last_message 갱신 =====
		LastMessageType lastType = switch (req.messageType()) {
			case TEXT -> LastMessageType.TEXT;
			case ATTACHMENT -> LastMessageType.ATTACHMENT;
		};

		String preview = (req.content() == null || req.content().isBlank())
			? (req.messageType() == MessageType.ATTACHMENT ? "[첨부파일]" : "")
			: req.content();

		LocalDateTime now = LocalDateTime.now();

		room.updateLastMessageCache(
			message.getId(),
			nextSeq,
			now,
			preview,
			lastType
		);

		log.info("[방 캐시 갱신] roomId={}, lastMessageId={}, lastMessageSeq={}, lastMessageAt={}, lastType={}, preview={}",
			roomId, message.getId(), nextSeq, now, lastType, preview
		);

		// ===== [7] 상대 ID 추출 =====
		Long opponentId = chatRoomMemberRepository
			.findOpponentMemberId(roomId, senderMemberId, MemberRoomStatus.ACTIVE)
			.orElse(null);

		log.info("[상대 조회] roomId={}, senderMemberId={}, opponentId={}",
			roomId, senderMemberId, opponentId
		);
		ChatMessageItemDto.AttachmentMeta meta = null;

		if (savedAttachment != null) {
			meta = ChatMessageItemDto.AttachmentMeta.builder()
				.attachmentType(savedAttachment.getAttachmentType())
				.objectKey(savedAttachment.getObjectKey())
				.mimeType(savedAttachment.getMimeType())
				.sizeBytes(savedAttachment.getSizeBytes())
				.status(savedAttachment.getStatus())
				.durationMs(savedAttachment.getDurationMs())
				.build();
		}


		// ===== [8] 방 토픽 발행 =====
		ChatRoomMessageDto roomMessageDto = ChatRoomMessageDto.builder()
			.roomId(roomId)
			.messageId(message.getId())
			.messageSeq(message.getMessageSeq())
			.senderMemberId(senderMemberId)
			.messageType(req.messageType())
			.content(req.content())
			.createdAt(now)
			.attachment(meta)
			.build();

		String roomTopicDest = "/topic/chat/rooms/" + roomId;

		messagingTemplate.convertAndSend(roomTopicDest, roomMessageDto);
		log.info("[방 토픽 발행] dest={}, roomId={}, messageId={}, messageSeq={}",
			roomTopicDest, roomId, message.getId(), nextSeq
		);

		// ===== [9] 개인 큐 이벤트 발행 =====
		String userQueueDest = "/queue/chat/events";

		// 9-1. 방을 방금 만든 경우: sender에게 ROOM_CREATED
		if (isRoomCreatedNow) {
			ChatEventEnvelope<RoomCreatedData> created = ChatEventEnvelope.<RoomCreatedData>builder()
				.type(ChatEventType.ROOM_CREATED)
				.roomId(roomId)
				.opponentMemberId(opponentMemberId)
				.eventAt(now)
				.data(RoomCreatedData.builder()
					.firstMessageId(message.getId())
					.firstMessageSeq(nextSeq)
					.lastMessagePreview(preview)
					.build())
				.build();

			messagingTemplate.convertAndSendToUser(
				String.valueOf(senderMemberId),
				userQueueDest,
				created
			);

			log.info(
				"[개인 이벤트 발행] (ROOM_CREATED) toUser={}, dest=/user{}, roomId={}, opponentMemberId={}, firstMessageId={}, firstSeq={}",
				senderMemberId, userQueueDest, roomId, opponentMemberId, message.getId(), nextSeq
			);
		} else {
			log.info("[ROOM_CREATED 생략] 이번 요청은 신규 방 생성이 아님. roomId={}", roomId);
		}

		// 9-2. 목록 갱신 이벤트는 sender/상대에게 발행
		ChatEventEnvelope<RoomListUpdatedData> listUpdated = ChatEventEnvelope.<RoomListUpdatedData>builder()
			.type(ChatEventType.ROOM_LIST_UPDATED)
			.roomId(roomId)
			.opponentMemberId(opponentId)
			.eventAt(now)
			.data(RoomListUpdatedData.builder()
				.lastMessagePreview(preview)
				.lastMessageType(lastType)
				.lastMessageAt(now)
				.lastMessageSeq(nextSeq)
				.lastMessageId(message.getId())
				.build())
			.build();

		messagingTemplate.convertAndSendToUser(
			String.valueOf(senderMemberId),
			userQueueDest,
			listUpdated
		);

		log.info("[개인 이벤트 발행] (ROOM_LIST_UPDATED) toUser={}, dest=/user{}, roomId={}, lastMessageId={}, lastSeq={}",
			senderMemberId, userQueueDest, roomId, message.getId(), nextSeq
		);

		if (opponentId != null) {
			messagingTemplate.convertAndSendToUser(
				String.valueOf(opponentId),
				userQueueDest,
				listUpdated
			);

			log.info("[개인 이벤트 발행] (ROOM_LIST_UPDATED) toUser={}, dest=/user{}, roomId={}, lastMessageId={}, lastSeq={}",
				opponentId, userQueueDest, roomId, message.getId(), nextSeq
			);
		} else {
			log.warn("[상대 없음] 1:1방에서 상대가 나갔거나 ACTIVE 아님. roomId={}, senderMemberId={}", roomId, senderMemberId);
		}

		log.info("[채팅 SEND 완료] senderMemberId={}, roomId={}, messageId={}, messageSeq={}, isRoomCreatedNow={}",
			senderMemberId, roomId, message.getId(), nextSeq, isRoomCreatedNow
		);
	}

	@Transactional(readOnly = true)
	public ChatMessageSliceResponse getRoomMessages(Long memberId, Long roomId, Integer limit, Long cursorSeq) {

		log.info("[채팅 히스토리 조회 시작] memberId={}, roomId={}, limit={}, cursorSeq={}",
			memberId, roomId, limit, cursorSeq
		);

		// 권한 체크
		chatRoomMemberRepository
			.findOneByRoomMemberStatus(roomId, memberId, MemberRoomStatus.ACTIVE)
			.orElseThrow(() -> {
				log.warn("[히스토리 권한 실패] memberId={} 는 roomId={} ACTIVE 멤버 아님", memberId, roomId);
				return new BusinessException(ChatErrorCode.CHAT_ROOM_MEMBER_CREATE_FORBIDDEN);
			});

		int safeLimit = (limit == null) ? 30 : Math.min(Math.max(limit, 1), 100);
		int limitPlusOne = safeLimit + 1;

		log.info("[히스토리 조회 파라미터] safeLimit={}, limitPlusOne={}, cursorSeq={}", safeLimit, limitPlusOne, cursorSeq);
		ChatRoomMember myMembership = chatRoomMemberRepository
			.findByRoomIdAndMemberId(roomId, memberId)
			.orElseThrow(() -> new BusinessException(ChatErrorCode.CHAT_MEMBER_NOT_FOUND));
		if (myMembership.getStatus() != MemberRoomStatus.ACTIVE) {
			throw new BusinessException(ChatErrorCode.CHAT_ROOM_MEMBER_CREATE_FORBIDDEN);
		}
		// 내가 볼 수 있는 최초 메시지 시퀀스
		// 재입장 이후 메시지부터 보임
		// null이면 처음부터 다 보여줌
		// null이면 한 번도 나가지 않은 것
		Long visibleFromSeq = myMembership.getJoinSeq();

		List<MessageRow> fetched = chatRoomQueryRepository.findRoomMessagesSlice(roomId, cursorSeq,visibleFromSeq, limitPlusOne);

		boolean hasNext = fetched.size() > safeLimit;
		List<MessageRow> page = hasNext ? fetched.subList(0, safeLimit) : fetched;

		log.info("[히스토리 조회 결과] roomId={}, fetchedSize={}, pageSize={}, hasNext={}",
			roomId, fetched.size(), page.size(), hasNext
		);

		List<ChatMessageItemDto> items = page.stream()
			.map(row -> ChatMessageItemDto.builder()
				.messageId(row.messageId())
				.messageSeq(row.messageSeq())
				.senderMemberId(row.senderId())
				.messageType(row.messageType())
				.content(row.content())
				.createdAt(row.createdAt())
				.attachment(row.attachmentType() == null ? null : ChatMessageItemDto.AttachmentMeta.builder()
					.attachmentType(row.attachmentType())
					.objectKey(row.objectKey())
					.mimeType(row.mimeType())
					.sizeBytes(row.sizeBytes())
					.status(row.attachmentStatus())
					.durationMs(row.durationMs())
					.build())
				.build())
			.toList();

		Long nextCursor = null;
		if (hasNext && !page.isEmpty()) {
			nextCursor = page.get(page.size() - 1).messageSeq();
		}

		log.info("[채팅 히스토리 조회 완료] roomId={}, itemsSize={}, nextCursorSeq={}",
			roomId, items.size(), nextCursor
		);

		return ChatMessageSliceResponse.builder()
			.items(items)
			.hasNext(hasNext)
			.nextCursorSeq(nextCursor)
			.build();
	}

	private void validateSendRequest(ChatSendMessageRequest req) {
		if (req.messageType() == MessageType.TEXT) {
			if (req.content() == null || req.content().isBlank()) {
				throw new BusinessException(ChatErrorCode.INVALID_MESSAGE_CONTENT);
			}
			if (req.attachment() != null) {
				throw new BusinessException(ChatErrorCode.INVALID_ATTACHMENT_FOR_TEXT);
			}
			return;
		}

		// ATTACHMENT
		if (req.attachment() == null) {
			throw new BusinessException(ChatErrorCode.INVALID_ATTACHMENT_REQUIRED);
		}
		if (req.attachment().objectKey() == null || req.attachment().objectKey().isBlank()) {
			throw new BusinessException(ChatErrorCode.INVALID_ATTACHMENT_OBJECT_KEY);
		}

		// 보안/검증: objectKey 접두어 제한 추천
		// 예: attachments/ 로 시작하는 것만 허용
		if (!req.attachment().objectKey().startsWith("attachments/")) {
			throw new BusinessException(ChatErrorCode.INVALID_ATTACHMENT_OBJECT_KEY);
		}
	}
}
