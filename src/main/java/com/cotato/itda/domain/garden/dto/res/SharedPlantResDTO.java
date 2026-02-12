package com.cotato.itda.domain.garden.dto.res;

import com.cotato.itda.domain.garden.enums.GardenState;
import com.cotato.itda.domain.garden.enums.PlantStage;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class SharedPlantResDTO {

    @Builder
    @Schema(description = "식물 액션 후 공통 응답 DTO")
    public record PlantActionResDTO(
            @Schema(description = "공유 식물 ID", example = "1")
            Long sharedPlantId,
            @Schema(description = "성장 정도", example = "12")
            Integer growthValue,
            @Schema(description = "성장 퍼센티지", example = "45")
            Integer percentage,
            @Schema(description = "성장 단계", example = "SPROUT")
            PlantStage growthStage,
            @Schema(description = "정원 상태", example = "WATERED_RECENTLY")
            GardenState gardenState,
            @Schema(description = "DB 상태", example = "GROWING")
            SharedPlantStatus status,
            @JsonProperty("isSoloMode")
            @Schema(description = "혼자 돌봄 모드 여부", example = "false")
            boolean isSoloMode,
            @Schema(description = "마지막으로 물 준 회원 정보")
            LastWateredByDTO lastWateredBy,
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
    @Schema(description = "초대 수락 시 생성된 공유 식물 정보 응답 DTO")
    public record AcceptedSharedPlantDTO(
            @Schema(description = "공유 식물 ID", example = "1")
            Long sharedPlantId,
            @Schema(description = "식물 ID", example = "1")
            Long plantId,
            @Schema(description = "식물 닉네임", example = "두쫀쿠")
            String nickname,
            @Schema(description = "생성 일시", example = "2025-12-28 12:34")
            LocalDateTime createdAt
    ) {}

    @Builder
    @Schema(description = "공유 식물 상세 정보 응답 DTO")
    public record SharedPlantInfoDTO(
            @Schema(description = "공유 식물 ID", example = "1")
            Long sharedPlantId,
            @Schema(description = "친구 ID", example = "2")
            Long friendId,
            @Schema(description = "친구 닉네임", example = "콩순이")
            String friendNickname,
            @Schema(description = "식물 ID", example = "1")
            Long plantId,
            @Schema(description = "식물 닉네임", example = "두쫀쿠")
            String nickname,
            @Schema(description = "성장 정도", example = "0")
            int growthValue,
            @Schema(description = "성장 퍼센티지", example = "45")
            int percentage,
            @Schema(description = "성장 단계", example = "SEED")
            PlantStage growthStage,
            @Schema(description = "정원 상태", example = "WATERABLE")
            GardenState gardenState,
            @Schema(description = "DB 상태", example = "GROWING")
            SharedPlantStatus status,
            @JsonProperty("isSoloMode")
            @Schema(description = "혼자 돌봄 모드 여부", example = "false")
            boolean isSoloMode,
            @Schema(description = "마지막으로 물 준 회원 정보")
            LastWateredByDTO lastWateredBy,
            @Schema(description = "마지막으로 물 준 시간")
            LocalDateTime lastWateredAt,
            @Schema(description = "생성 일시", example = "2025-12-28 12:34")
            LocalDateTime createdAt
    ) {}

    @Builder
    @Schema(description = "공유 식물 상세 정보 응답 리스트 DTO")
    public record SharedPlantInfoListDTO(
            @Schema(description = "공유 식물 총 개수", example = "3")
            int totalCount,
            @Schema(description = "개인 영양제 보유량", example = "2")
            int nutrientCount,
            @Schema(description = "공유 식물 상세 정보")
            List<SharedPlantInfoDTO> sharedPlants
    ) {}
}