package com.cotato.itda.domain.diary.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record WriterInfo(
    @Schema(description = "작성자 ID", example = "3")
    Long writerId,

    @Schema(description = "작성자 별명", example = "길동이")
    String nickname,

    @Schema(description = "작성자 프로필 사진 URL", example = "https://example-bucket.s3.ap-northeast-2.amazonaws.com/profile/uuid_example.jpg")
    String profileImageUrl,

    @Schema(description = "작성자가 본인인지 여부")
    boolean isMe
) {
}
