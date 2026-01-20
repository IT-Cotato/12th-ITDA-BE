package com.cotato.itda.domain.diary.dto.request;

import com.cotato.itda.domain.diary.enums.EmojiCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DiaryRequest(

        @Schema(description = "일기 날짜 (필수, yyyy-MM-dd 형식)", example = "2026-01-10", pattern = "yyyy-MM-dd")
        @NotNull(message = "날짜는 필수입니다.")
        @JsonFormat(pattern = "yyyy-MM-dd")
        @PastOrPresent(message = "미래 날짜의 일기는 작성할 수 없습니다.")
        LocalDate date,

        @Schema(description = "이모지 코드 (필수)", example = "HAPPY_GLAD")
        @NotNull(message = "이모지 선택은 필수입니다.")
        EmojiCode emojiCode,

        @Schema(description = "일기 텍스트 내용 (필수)", example = "여행 왔다~~")
        @NotBlank(message = "일기 내용은 필수입니다.")
        @Size(min = 1, max = 5000, message = "일기 내용은 1자 이상 5000자 이하여야 합니다.")
        String content,

        @Schema(description = "일기 사진 전체 URL (선택)", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/diary/uuid_example.jpg")
        String imageUrl
) {
}
