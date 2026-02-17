package com.cotato.itda.domain.friendship.dto.req;

import com.cotato.itda.domain.friendship.enums.ChatGoal;
import com.cotato.itda.domain.friendship.enums.SpeechStyle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class FriendshipReqDTO {

    public record UpdateDTO(
            @Schema(description = "친구 별명 (선택)", example = "어마마마")
            String nickname,
            @Schema(description = "대화 말투 (필수)", example = "존댓말", allowableValues = {"존댓말", "반말"})
            @NotNull(message = "대화 말투는 필수입니다.")
            SpeechStyle speechStyle,
            @Schema(description = "대화 목표 (필수)", example = "주 1일")
            @NotNull(message = "대화 목표는 필수입니다.")
            ChatGoal chatGoal,
            @Schema(description = "대화 주제 코드 리스트 (필수)", example = "[\"DAILY_LIFE\", \"WEATHER\"]")
            @NotEmpty(message = "대화 주제는 필수입니다.")
            List<String> topicCodes
    ) {
    }
}
