package com.cotato.itda.domain.chattopic.controller;

import com.cotato.itda.domain.chattopic.dto.res.ChatTopicResDTO;
import com.cotato.itda.domain.chattopic.dto.res.ChatTopicTemplateResponse;
import com.cotato.itda.domain.chattopic.service.query.ChatTopicQueryService;
import com.cotato.itda.domain.chattopic.service.query.ChatTopicTemplateService;
import com.cotato.itda.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-topics")
@Tag(name = "ChatTopic", description = "대화 주제")
public class ChatTopicController implements ChatTopicControllerDocs {

    private final ChatTopicQueryService chatTopicQueryService;
    private final ChatTopicTemplateService chatTopicTemplateService;
    @GetMapping
    @Override
    public ApiResponse<ChatTopicResDTO.ChatTopicListDTO> getChatTopics()
     {
        return ApiResponse.success(chatTopicQueryService.getChatTopics());
    }

    
    @GetMapping("/{topicCode}/templates")
    @Operation(summary="채팅 주제에 해당하는 템플릿 조회 API", description="채팅 주제에 해당하는 템플릿을 조회합니다.")
    public ApiResponse<ChatTopicTemplateResponse> getChatTopicTemplates(
        @PathVariable String topicCode
    ) {
        return ApiResponse.success(
            chatTopicTemplateService.getChatTopicTemplates(topicCode)
        );
    }
}
