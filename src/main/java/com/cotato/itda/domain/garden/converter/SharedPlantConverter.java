package com.cotato.itda.domain.garden.converter;

import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantInvite;
import com.cotato.itda.domain.garden.enums.GardenState;
import com.cotato.itda.domain.member.entity.Member;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class SharedPlantConverter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static SharedPlant toSharedPlant(SharedPlantInvite invite) {
        Member inviter = invite.getInviter();
        Member invitee = invite.getInvitee();

        Member memberA = inviter.getId() < invitee.getId() ? inviter : invitee;
        Member memberB = inviter.getId() < invitee.getId() ? invitee : inviter;

        return SharedPlant.builder()
                .plant(invite.getPlant())
                .memberA(memberA)
                .memberB(memberB)
                .nickname(invite.getNickname())
                .build();
    }

    public static SharedPlantResDTO.AcceptedSharedPlantDTO toAcceptedSharedPlantDTO(SharedPlant sharedPlant) {
        return SharedPlantResDTO.AcceptedSharedPlantDTO.builder()
                .sharedPlantId(sharedPlant.getId())
                .plantId(sharedPlant.getPlant().getId())
                .nickname(sharedPlant.getNickname())
                .createdAt(sharedPlant.getCreatedAt().format(FORMATTER))
                .build();
    }

    public static SharedPlantResDTO.SharedPlantInfoDTO toSharedPlantInfoDTO(
            SharedPlant sharedPlant,
            Friendship friendship,
            Long currentMemberId,
            GardenState gardenState,
            int percentage
    ) {
        SharedPlantResDTO.LastWateredByDTO lastWateredBy = null;
        if (sharedPlant.getLastWateredBy() != null) {
            lastWateredBy = SharedPlantResDTO.LastWateredByDTO.builder()
                    .memberId(sharedPlant.getLastWateredBy())
                    .isMe(sharedPlant.getLastWateredBy().equals(currentMemberId))
                    .build();
        }

        return SharedPlantResDTO.SharedPlantInfoDTO.builder()
                .sharedPlantId(sharedPlant.getId())
                .friendId(friendship.getFriendId())
                .friendNickname(friendship.getDisplayName())
                .plantId(sharedPlant.getPlant().getId())
                .nickname(sharedPlant.getNickname())
                .growthValue(sharedPlant.getGrowthValue())
                .percentage(percentage)
                .growthStage(sharedPlant.getGrowthStage())
                .gardenState(gardenState)
                .status(sharedPlant.getStatus())
                .isSoloMode(sharedPlant.getIsSoloMode())
                .lastWateredBy(lastWateredBy)
                .lastWateredAt(sharedPlant.getLastWateredAt())
                .createdAt(sharedPlant.getCreatedAt().format(FORMATTER))
                .build();
    }

    public static SharedPlantResDTO.SharedPlantInfoListDTO toSharedPlantInfoListDTO(
            List<SharedPlantResDTO.SharedPlantInfoDTO> sharedPlantInfoDTOs,
            int nutrientCount
    ) {
        return SharedPlantResDTO.SharedPlantInfoListDTO.builder()
                .totalCount(sharedPlantInfoDTOs.size())
                .nutrientCount(nutrientCount)
                .sharedPlants(sharedPlantInfoDTOs)
                .build();
    }

    public static SharedPlantResDTO.LastWateredByDTO toLastWateredByDTO(Member member) {
        return SharedPlantResDTO.LastWateredByDTO.builder()
                .memberId(member.getId())
                .isMe(true)
                .build();
    }

    public static SharedPlantResDTO.PlantActionResDTO toPlantActionResDTO(
            SharedPlant sharedPlant,
            Member currentMember,
            GardenState gardenState,
            int percentage
    ) {
        SharedPlantResDTO.LastWateredByDTO lastWateredBy = null;
        if (sharedPlant.getLastWateredBy() != null) {
            lastWateredBy = SharedPlantResDTO.LastWateredByDTO.builder()
                    .memberId(sharedPlant.getLastWateredBy())
                    .isMe(sharedPlant.getLastWateredBy().equals(currentMember.getId()))
                    .build();
        }

        return SharedPlantResDTO.PlantActionResDTO.builder()
                .sharedPlantId(sharedPlant.getId())
                .growthValue(sharedPlant.getGrowthValue())
                .percentage(percentage)
                .growthStage(sharedPlant.getGrowthStage())
                .gardenState(gardenState)
                .status(sharedPlant.getStatus())
                .isSoloMode(sharedPlant.getIsSoloMode())
                .lastWateredBy(lastWateredBy)
                .nutrientCount(currentMember.getNutrientCount())
                .build();
    }
}
