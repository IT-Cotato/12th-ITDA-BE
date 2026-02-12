package com.cotato.itda.domain.chat.controller.dto;

import com.cotato.itda.domain.chat.enums.AttachmentType;

import jakarta.validation.constraints.NotNull;

public record ChatAttachmentRequest (
	@NotNull AttachmentType attachmentType,
	@NotNull String objectKey,
	String mimeType,
	Long sizeBytes,
	Integer durationMs
){
}
