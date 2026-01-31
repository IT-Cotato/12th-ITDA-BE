package com.cotato.itda.domain.diary.dto.response;

import com.cotato.itda.domain.diary.enums.EmojiCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DiaryListResponse(
        @Schema(description = "조회된 일기 목록")
        List<DiaryItem> diaries,

        @Schema(
                description = "현재 응답된 목록 중 가장 마지막 일기 ID, 다음 데이터 요청 시 이 값을 요청 파라미터로 포함해야 합니다.",
                example = "10")
        Long lastId,

        @Schema(
                description = "다음 일기 존재 여부, 현재 반환된 리스트 이후 불러올 데이터가 더 남아있는지 나타냅니다.\n" +
                            "true인 경우, lastId 값을 파라미터에 포함해 다음 데이터를 요청해야 합니다.",
                example = "true"
        )
        boolean hasNext
) {

    @Builder
    public record DiaryItem(
            @Schema(description = "작성자 정보 (ID, 닉네임, 프로필 사진 URL)")
            WriterInfo writerInfo,

            @Schema(description = "일기 ID", example = "1")
            Long diaryId,

            @Schema(description = "작성한 일기 날짜", example = "2026-01-10")
            LocalDate date,

            @Schema(description = "이모지 코드", example = "HAPPY_GLAD")
            EmojiCode emojiCode,

            @Schema(description = "텍스트 내용", example = "여행 왔다~~")
            String content,

            @Schema(description = "일기 사진 전체 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/diary/uuid_example.jpg")
            String imageUrl,

            @Schema(description = "좋아요 수", example = "1")
            int likeCount,

            @Schema(description = "댓글 수", example = "1")
            int commentCount,

            @Schema(description = "내가 좋아요 눌렀는지 여부", example = "true")
            boolean isLiked,

            @Schema(description = "작성 시간", example = "2026-01-10T10:00:00")
            LocalDateTime createdAt

    ) {}
}
