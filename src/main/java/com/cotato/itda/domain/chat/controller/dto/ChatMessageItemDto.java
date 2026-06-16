package com.cotato.itda.domain.chat.controller.dto;

import java.time.LocalDateTime;

import com.cotato.itda.domain.chat.enums.AttachmentStatus;
import com.cotato.itda.domain.chat.enums.AttachmentType;
import com.cotato.itda.domain.chat.enums.MessageType;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record ChatMessageItemDto(
	@Schema(description = "메시지 ID", example = "9001")
	Long messageId,

	@Schema(description = "방 내 seq", example = "31")
	long messageSeq,

	@Schema(description = "보낸 사람 ID", example = "10")
	Long senderMemberId,

	@Schema(description = "메시지 타입(TEXT/ATTACHMENT)", example = "TEXT")
	MessageType messageType,

	@Schema(description = "텍스트 내용(첨부만이면 null 가능)", example = "안녕!")
	String content,

	@Schema(description = "전송 시각", example = "2026-02-03T18:10:12.123")
	LocalDateTime createdAt,

	@Schema(description = "첨부 메타(없으면 null)")
	AttachmentMeta attachment,

    @Schema(description = "답장 대상 메시지 정보 (일반 메시지면 null)")
    ReplyTargetInfo replyTarget
) {
	/**
	 * 첨부 메타(voice/image/file 공통)
	 */
	@Builder
	public record AttachmentMeta(
		@Schema(description = "첨부 타입", example = "IMAGE")
		AttachmentType attachmentType,

		@Schema(description = "S3 object key", example = "attachments/2026/02/03/uuid.png")
		String objectKey,

		@Schema(description = "mime type", example = "image/png")
		String mimeType,

		@Schema(description = "파일 크기(bytes)", example = "842133")
		Long sizeBytes,

		@Schema(description = "업로드 상태", example = "READY")
		AttachmentStatus status,

		@Schema(description = "음성 길이(ms) - 음성이 아니면 null", example = "4300")
		Integer durationMs
	) {}

    /**
     * 답장 대상 메시지 정보
     */
    @Builder
    public record ReplyTargetInfo(
            @Schema(description = "원본 메시지 ID", example = "1")
            Long messageId,

            @Schema(description = "원본 메시지 발신자 ID", example = "1")
            Long senderId,

            @Schema(description = "원본 메시지 발신자 닉네임", example = "홍길동")
            String senderNickname,

            @Schema(description = "원본 메시지 타입(TEXT/ATTACHMENT)", example = "TEXT")
            MessageType messageType,

            @Schema(description = "원본 메시지 내용 미리보기 (텍스트면 본문 내용, 사진이면 '[사진]')", example = "오늘 회의 몇 시야?")
            String contentPreview
    ) {}
}