package com.cotato.itda.domain.garden.converter;

import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantInvite;
import com.cotato.itda.domain.member.entity.Member;

import java.time.format.DateTimeFormatter;

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

    public static SharedPlantResDTO.SharedPlantInfoDTO toSharedPlantInfoDTO(SharedPlant sharedPlant) {
        return SharedPlantResDTO.SharedPlantInfoDTO.builder()
                .sharedPlantId(sharedPlant.getId())
                .memberAId(sharedPlant.getMemberA().getId())
                .memberBId(sharedPlant.getMemberB().getId())
                .plantId(sharedPlant.getPlant().getId())
                .nickname(sharedPlant.getNickname())
                .growthValue(sharedPlant.getGrowthValue())
                .growthStage(sharedPlant.getGrowthStage())
                .status(sharedPlant.getStatus())
                .createdAt(sharedPlant.getCreatedAt().format(FORMATTER))
                .build();
    }

    public static SharedPlantResDTO.LastWateredByDTO toLastWateredByDTO(Member member) {
        return SharedPlantResDTO.LastWateredByDTO.builder()
                .memberId(member.getId())
                .isMe(true)
                .build();
    }

    public static SharedPlantResDTO.WaterInfoResDTO toWaterInfoResDTO(SharedPlant sharedPlant, Member currentMember) {
        SharedPlantResDTO.LastWateredByDTO lastWateredBy = toLastWateredByDTO(currentMember);

        return SharedPlantResDTO.WaterInfoResDTO.builder()
                .sharedPlantId(sharedPlant.getId())
                .growthValue(sharedPlant.getGrowthValue())
                .growthStage(sharedPlant.getGrowthStage())
                .lastWateredBy(lastWateredBy)
                .status(sharedPlant.getStatus())
                .isSoloMode(sharedPlant.getIsSoloMode())
                .nutrientCount(currentMember.getNutrientCount())
                .build();
    }
}
