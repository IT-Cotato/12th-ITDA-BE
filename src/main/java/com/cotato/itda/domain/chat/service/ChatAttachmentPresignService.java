package com.cotato.itda.domain.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cotato.itda.domain.chat.entity.ChatRoomMember;
import com.cotato.itda.domain.chat.enums.MemberRoomStatus;
import com.cotato.itda.domain.chat.exception.code.ChatErrorCode;
import com.cotato.itda.domain.chat.repository.ChatMessageAttachmentRepository;
import com.cotato.itda.domain.chat.repository.ChatRoomMemberRepository;
import com.cotato.itda.domain.image.service.S3Service;
import com.cotato.itda.global.error.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatAttachmentPresignService {

	private final ChatMessageAttachmentRepository chatMessageAttachmentRepository;
	private final ChatRoomMemberRepository chatRoomMemberRepository;
	private final S3Service s3Service;

	@Transactional(readOnly = true)
	public S3Service.PresignedGetUrlResponse presignGet(Long memberId, String objectKey) {

		log.info("[PRESIGNED_GET][IN] memberId={}, objectKey={}", memberId, objectKey);

		objectKey = objectKey == null ? null : objectKey.trim();

		// 1) objectKey로 attachment가 DB에 존재하는지 확인 + roomId/messageSeq 가져오기
		var view = chatMessageAttachmentRepository.findAccessViewByObjectKey(objectKey)
			.orElseThrow(() -> new BusinessException(ChatErrorCode.INVALID_ATTACHMENT_OBJECT_KEY));

		log.info("[PRESIGNED_GET][CHECK] attachment found for objectKey={}, roomId={}, messageSeq={}",
			objectKey, view.getRoomId(), view.getMessageSeq());
		// 2) memberId가 그 roomId의 ACTIVE 멤버인지 확인
		ChatRoomMember membership = chatRoomMemberRepository
			.findByRoomIdAndMemberId(view.getRoomId(), memberId)
			.orElseThrow(() -> new BusinessException(ChatErrorCode.CHAT_ROOM_MEMBER_CREATE_FORBIDDEN));

		log.info("[PRESIGNED_GET][CHECK] membership found for memberId={}, roomId={}, status={}",
			memberId, view.getRoomId(), membership.getStatus());

		if (membership.getStatus() != MemberRoomStatus.ACTIVE) {
			throw new BusinessException(ChatErrorCode.CHAT_ROOM_MEMBER_CREATE_FORBIDDEN);
		}
		log.info("[PRESIGNED_GET][CHECK] membership status is ACTIVE for memberId={}, roomId={}", memberId, view.getRoomId());

		// 3) 재입장 정책
		Long joinSeq = membership.getJoinSeq();
		if (joinSeq != null && view.getMessageSeq() < joinSeq) {
			throw new BusinessException(ChatErrorCode.CHAT_ROOM_MEMBER_CREATE_FORBIDDEN);
		}
		// 4) 통과하면 S3 presigned GET 발급
		return s3Service.getPresignedGetUrl(objectKey);
	}
}