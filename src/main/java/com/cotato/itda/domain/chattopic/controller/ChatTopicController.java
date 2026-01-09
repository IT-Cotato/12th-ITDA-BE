package com.cotato.itda.domain.chattopic.controller;

import com.cotato.itda.domain.chattopic.dto.res.ChatTopicResDTO;
import com.cotato.itda.domain.chattopic.service.query.ChatTopicQueryService;
import com.cotato.itda.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-topics")
@Tag(name = "ChatTopic", description = "대화 주제")
public class ChatTopicController implements ChatTopicControllerDocs {

    private final ChatTopicQueryService chatTopicQueryService;

    @GetMapping
    @Override
    public ApiResponse<ChatTopicResDTO.ChatTopicListDTO> getChatTopics() {
        return ApiResponse.success(chatTopicQueryService.getChatTopics());
    }
}
