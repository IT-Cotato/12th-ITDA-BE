package com.cotato.itda.domain.diary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record WriterInfo(
    @Schema(description = "작성자 ID", example = "3")
    Long memberId,

    @Schema(description = "작성자 표시 이름 (별명 또는 본명)", example = "길동이")
    String nickname,

    @Schema(description = "작성자 프로필 사진 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/profile/uuid_example.jpg")
    String profileImageUrl,

    @Schema(description = "작성자가 본인인지 여부", example = "false")
    boolean isMe
) {
}
