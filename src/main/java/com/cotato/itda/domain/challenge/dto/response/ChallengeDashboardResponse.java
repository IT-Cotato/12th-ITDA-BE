package com.cotato.itda.domain.challenge.dto.response;

import com.cotato.itda.domain.challenge.enums.ChallengeStatus;
import com.cotato.itda.domain.mission.enums.MissionCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record ChallengeDashboardResponse(

        @Schema(description = "오늘 날짜", example = "2026-01-10")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate todayDate,

        @Schema(description = "오늘의 미션 정보")
        TodayMissionInfo mission,

        @Schema(description = "나의 오늘 챌린지 정보(참여 안 한 경우 null)")
        MyChallengeInfo myChallenge,

        @Schema(description = "이번 주 요일별 참여 현황(월~일)")
        List<WeeklyStatus> weeklyStatus

) {
    @Builder
    public record TodayMissionInfo(

            @Schema(description = "미션 ID")
            Long missionId,

            @Schema(description = "미션 날짜", example = "2026-01-10")
            LocalDate missionDate,

            @Schema(description = "미션 카테고리", example = "FOOD")
            MissionCategory category,

            @Schema(description = "미션 키워드", example = "점심")
            String keyword,

            @Schema(description = "미션 전체 내용", example = "오늘의 점심을 공유해주세요.")
            String content
    ) {
    }

    @Builder
    public record MyChallengeInfo(

            @Schema(description = "챌린지 ID")
            Long challengeId,

            @Schema(description = "챌린지 사진 전체 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/challenge/uuid_example.jpg")
            String imageUrl
    ) {
    }

    @Builder
    public record WeeklyStatus(

            @Schema(description = "날짜", example = "2026-01-10")
            @JsonFormat(pattern = "yyyy-MM-dd")
            LocalDate date,

            @Schema(description = "미션 참여 상태 (DONE: 참여 완료, MISSED: 미참여, WAITING: 오늘이며 미참여, FUTURE: 미래) ", example = "DONE")
            ChallengeStatus status
    ) {
    }
}
