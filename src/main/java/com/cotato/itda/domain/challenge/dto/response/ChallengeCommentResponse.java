package com.cotato.itda.domain.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ChallengeCommentResponse(

        @Schema(description = "댓글 ID")
        Long commentId,

        @Schema(description = "작성자 정보")
        WriterInfo writerInfo,

        @Schema(description = "댓글 내용", example = "재밌었겠다")
        String content,

        @Schema(description = "작성 시간", example = "2026-01-10T10:00:00")
        LocalDateTime createdAt

) {
    @Builder
    public record WriterInfo(

            @Schema(description = "작성자 ID", example = "1")
            Long memberId,

            @Schema(description = "작성자 이름", example = "홍길동")
            String name,

            @Schema(description = "작성자 프로필 사진 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/profile/uuid_example.jpg")
            String profileImageUrl

    ) {
    }
}
