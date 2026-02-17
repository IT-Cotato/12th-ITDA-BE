package com.cotato.itda.domain.garden.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.List;

public class PlantInviteResDTO {

        @Builder
        public record PlantInviteCandidatesResDto(
                        @Schema(description = "후보 친구 수", example = "3") Integer count,
                        @Schema(description = "후보 친구 목록") List<InviteCandidateInfoDTO> friends) {
        }

        @Builder
        public record InviteCandidateInfoDTO(
                        @Schema(description = "친구 관계 ID", example = "10") Long friendshipId,
                        @Schema(description = "친구 닉네임 (별명 또는 본명)", example = "길동이") String nickname,
                        @Schema(description = "친구 프로필 이미지 URL", example = "https://example.com/profile.jpg") String profileImageUrl,
                        @Schema(description = "친구 회원 ID", example = "12") Long friendId) {
        }

        @Builder
        public record CreateSharedPlantInviteResDTO(
                        @Schema(description = "초대 ID", example = "12") Long inviteId,
                        @Schema(description = "초대 상태", example = "PENDING") String status,
                        @Schema(description = "생성 일시", example = "2025-12-28 12:23") String createdAt) {
        }

        @Builder
        public record MyPlantInviteListResDTO(
                        @Schema(description = "초대 수", example = "1") Integer count,
                        @Schema(description = "초대 목록") List<MyPlantInviteResDTO> invitations) {
        }

        @Builder
        public record MyPlantInviteResDTO(
                        @Schema(description = "초대 ID", example = "12") Long inviteId,
                        @Schema(description = "식물 이름", example = "lily") String plantName,
                        @Schema(description = "초대자 회원 ID", example = "1") Long inviterId,
                        @Schema(description = "초대자 보여지는 이름", example = "콩순이") String inviterName,
                        @Schema(description = "식물 닉네임", example = "두쫀쿠") String nickname,
                        @Schema(description = "초대 메시지", example = "같이 키워용") String message,
                        @Schema(description = "초대 상태", example = "PENDING") String status,
                        @Schema(description = "생성 일시", example = "2025-12-28 12:23") String createdAt) {
        }

        @Builder
        public record UpdateSharedPlantInviteResDTO(
                        @Schema(description = "초대 ID", example = "1") Long inviteId,
                        @Schema(description = "초대 상태", example = "ACCEPTED") String status,
                        @Schema(description = "공유 식물 정보 (수락 시에만 포함)") SharedPlantResDTO.AcceptedSharedPlantDTO sharedPlant) {
        }
}
