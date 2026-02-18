package com.cotato.itda.domain.chat.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cotato.itda.domain.chat.service.ChatAttachmentPresignService;
import com.cotato.itda.domain.image.service.S3Service;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/chat/attachments")
public class ChatAttachmentController {

	private final ChatAttachmentPresignService chatAttachmentPresignService;

	@GetMapping("/presigned-get")
	public S3Service.PresignedGetUrlResponse presignedGet(
		@RequestParam String objectKey,
		@AuthenticationPrincipal JwtPrincipal principal
	) {
		Long memberId = principal.memberId();
		return chatAttachmentPresignService.presignGet(memberId, objectKey);
	}
}
