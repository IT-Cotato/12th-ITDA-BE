package com.cotato.itda.domain.chattopic.controller;

import com.cotato.itda.domain.chattopic.dto.res.ChatTopicResDTO;
import com.cotato.itda.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;

public interface ChatTopicControllerDocs {

    @Operation(
            summary = "대화 주제 조회 API By 정원",
            description = "활성화된 대화 주제 목록만 조회합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
    })
    @GetMapping
    ApiResponse<ChatTopicResDTO.ChatTopicListDTO> getChatTopics();
}
