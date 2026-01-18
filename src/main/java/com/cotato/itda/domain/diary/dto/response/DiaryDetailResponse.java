package com.cotato.itda.domain.diary.dto.response;

import com.cotato.itda.domain.diary.enums.EmojiCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record DiaryDetailResponse(
        WriterInfo writerInfo,

        @Schema(description = "일기 ID", example = "1")
        Long diaryId,

        @Schema(description = "일기 날짜", example = "2026-01-10")
        LocalDate date,

        @Schema(description = "이모지 코드", example = "HAPPY_GLAD")
        EmojiCode emojiCode,

        @Schema(description = "이모지 설명(형용사)", example = "기쁜")
        String emojiDescription,

        @Schema(description = "텍스트 내용", example = "여행 왔다~~")
        String content,

        @Schema(description = "일기 사진 전체 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/diary/uuid_example.jpg")
        String imageUrl,

        @Schema(description = "좋아요 수", example = "10")
        int likeCount,

        @Schema(description = "댓글 수", example = "5")
        int commentCount,

        @Schema(description = "좋아요 여부", example = "true")
        boolean isLiked,

        @Schema(description = "작성 시간", example = "2026-01-10T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "수정 시간", example = "2026-01-10T10:00:00")
        LocalDateTime updatedAt
) {

}
