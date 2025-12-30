package com.cotato.itda.domain.chattopic.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.List;

public class ChatTopicResDTO {

    @Builder
    public record ChatTopicListDTO(
            @Schema(description = "채팅 주제 개수", example = "10")
            Integer count,
            @Schema(description = "채팅 주제 목록")
            List<ChatTopicDTO> chatTopicList
    ) {}

    @Builder
    public record ChatTopicDTO(
            @Schema(description = "주제 이름", example = "일상")
            String name,
            @Schema(description = "주제 코드", example = "DAILY_LIFE")
            String code
    ) {
    }
}
