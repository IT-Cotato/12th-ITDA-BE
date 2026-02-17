package com.cotato.itda.domain.garden.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PlantInviteReqDTO {

    public record CreateSharedPlantInviteReqDTO(
            @Schema(description = "초대받을 친구 회원 ID", example = "12") @NotNull(message = "초대받을 친구 ID는 필수입니다.") Long inviteeId,

            @Schema(description = "식물 이름", example = "lily") @NotBlank(message = "식물 이름은 필수입니다.") String plantName,

            @Schema(description = "함께 키울 식물 닉네임", example = "두쫀쿠") @NotBlank(message = "식물 닉네임은 필수입니다.") @Size(max = 20, message = "닉네임은 20자 이내여야 합니다.") String nickname,

            @Schema(description = "초대 메시지", example = "우리 같이 식물 키우자!") @NotBlank(message = "초대 메시지는 필수입니다.") @Size(max = 100, message = "초대 메시지는 100자 이내여야 합니다.") String message) {
    }

    public record UpdateSharedPlantInviteReqDTO(
            @Schema(description = "수락/거절 상태 (ACCEPTED, REJECTED)", example = "ACCEPTED") @NotNull(message = "상태값은 필수입니다.") com.cotato.itda.domain.garden.enums.InviteStatus status) {
    }
}
