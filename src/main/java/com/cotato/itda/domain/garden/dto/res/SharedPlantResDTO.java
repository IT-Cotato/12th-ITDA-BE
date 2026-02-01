package com.cotato.itda.domain.garden.dto.res;

import com.cotato.itda.domain.garden.enums.PlantStage;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

public class SharedPlantResDTO {

    @Builder
    @Schema(description = "물 주기 후 식물 정보 응답 DTO")
    public record WaterInfoResDTO(
            @Schema(description = "공유 식물 ID", example = "1")
            Long sharedPlantId,
            @Schema(description = "성장 정도", example = "12")
            Integer growthValue,
            @Schema(description = "성장 단계", example = "SPROUT")
            PlantStage growthStage,
            @Schema(description = "마지막으로 물 준 회원 정보")
            LastWateredByDTO lastWateredBy,
            @Schema(description = "공유 식물의 현재 상태", example = "GROWING")
            SharedPlantStatus status,
            @JsonProperty("isSoloMode")
            @Schema(description = "혼자 돌봄 모드 여부", example = "false")
            boolean isSoloMode,
            @Schema(description = "현재 회원이 보유한 영양제 총 개수", example = "10")
            Integer nutrientCount
    ) {}

    @Builder
    @Schema(description = "마지막으로 물 준 회원 정보 DTO")
    public record LastWateredByDTO(
            @Schema(description = "회원 ID", example = "1")
            Long memberId,
            @JsonProperty("isMe")
            @Schema(description = "요청한 회원이 물을 주었는지 여부", example = "true")
            boolean isMe
    ) {}

    @Builder
    @Schema(description = "공유 식물 상세 정보 응답 DTO")
    public record SharedPlantInfoDTO(
            @Schema(description = "공유 식물 ID", example = "1")
            Long sharedPlantId,
            @Schema(description = "멤버 A ID (초대자)", example = "1")
            Long memberAId,
            @Schema(description = "멤버 B ID (초대받은 자)", example = "2")
            Long memberBId,
            @Schema(description = "식물 ID", example = "1")
            Long plantId,
            @Schema(description = "식물 닉네임", example = "두쫀쿠")
            String nickname,
            @Schema(description = "성장 정도", example = "0")
            int growthValue,
            @Schema(description = "성장 단계", example = "SEED")
            PlantStage growthStage, // String -> PlantStage Enum으로 변경
            @Schema(description = "상태", example = "GROWING")
            SharedPlantStatus status, // String -> SharedPlantStatus Enum으로 변경
            @Schema(description = "생성 일시", example = "2025-12-28 12:34:56")
            String createdAt
    ) {}
}