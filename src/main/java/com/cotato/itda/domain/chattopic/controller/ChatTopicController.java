package com.cotato.itda.domain.chattopic.controller;

import com.cotato.itda.domain.chattopic.ai.gemini.AiPhraseRecommendService;
import com.cotato.itda.domain.chattopic.dto.req.AiPhraseRecommendRequest;
import com.cotato.itda.domain.chattopic.dto.res.AiPhraseRecommendResponse;
import com.cotato.itda.domain.chattopic.dto.res.ChatTopicResDTO;
import com.cotato.itda.domain.chattopic.dto.res.ChatTopicTemplateResponse;
import com.cotato.itda.domain.chattopic.service.query.ChatTopicQueryService;
import com.cotato.itda.domain.chattopic.service.query.ChatTopicTemplateService;
import com.cotato.itda.global.common.response.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat-topics")
@Tag(name = "ChatTopic", description = "대화 주제")
public class ChatTopicController implements ChatTopicControllerDocs {

    private final ChatTopicQueryService chatTopicQueryService;
    private final ChatTopicTemplateService chatTopicTemplateService;
    private final AiPhraseRecommendService aiPhraseRecommendService;
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


    @Operation(
        summary = "AI 문구 추천",
        requestBody =  @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "날씨 예시",
                    value = """
					{
					  "existingPhrases": [
					    "오늘 날씨가 한결 따뜻하대요",
					    "오늘은 포근한 하루래요",
					    "오늘은 옷 따뜻하게 입으세요",
					    "오늘은 비가 오네요. 우산챙기세요",
					    "오늘은 날씨가 더워요. 물 자주 드세요",
					    "오늘은 하늘이 흐려요"
					  ],
					  "topic": "날씨",
					  "count": 5
					}
					"""
                )
            )
        )
    )
    @PostMapping("ai-phrases")
    public ApiResponse<AiPhraseRecommendResponse> recommend(@RequestBody @Valid AiPhraseRecommendRequest request){
        AiPhraseRecommendResponse response =aiPhraseRecommendService.recommend(request);
        return ApiResponse.success(response);
    }
}
