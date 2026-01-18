package com.cotato.itda.domain.diary.dto.response;

import com.cotato.itda.domain.diary.enums.EmojiCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.time.LocalDate;
import java.util.List;

@Builder
public record MonthlyDiaryListResponse(

        @Schema(description = "작성자 정보 (친구 일기 조회 시에만 포함)", nullable = true)
        WriterInfo writerInfo,

        @Schema(description = "조회 연도", example = "2026")
        int year,

        @Schema(description = "조회 월", example = "1")
        int month,

        @Schema(description = "일기 개수", example = "15")
        Integer diaryCount,

        @Schema(description = "일기 목록")
        List<DiaryItem> diaries

) {
    @Builder
    public record DiaryItem(

        @Schema(description = "일기 ID", example = "1")
        Long diaryId,

        @Schema(description = "일기 날짜", example = "2026-01-10")
        LocalDate date,

        @Schema(description = "이모지 코드", example = "HAPPY_GLAD")
        EmojiCode emojiCode

    ) {}

}
