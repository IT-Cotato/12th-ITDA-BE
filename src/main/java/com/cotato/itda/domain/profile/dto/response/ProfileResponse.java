package com.cotato.itda.domain.profile.dto.response;

import com.cotato.itda.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ProfileResponse(

        @Schema(description = "멤버 ID", example = "1")
        Long memberId,

        @Schema(description = "프로필 이미지 URL", example = "https://...amazonaws.com/profile/uuid_filename.png")
        String profileImageUrl,

        @Schema(description = "회원 이름", example = "김영자")
        String name,

        @Schema(description = "전화번호", example = "01012345678")
        String phoneNumber,

        @Schema(description = "생년월일", example = "19801201")
        LocalDate birthDate
) {
    public static ProfileResponse from(Member member) {
        return new ProfileResponse(
                member.getId(),
                member.getProfileImageUrl(),
                member.getName(),
                member.getPhoneNumber(),
                member.getBirthDate()
        );
    }
}
