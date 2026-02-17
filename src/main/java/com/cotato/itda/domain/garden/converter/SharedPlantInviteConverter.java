package com.cotato.itda.domain.garden.converter;

import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.garden.dto.res.PlantInviteResDTO.*;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantInvite;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SharedPlantInviteConverter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static InviteCandidateInfoDTO toInviteCandidateInfoDTO(Friendship friendship) {
        return InviteCandidateInfoDTO.builder()
                .friendshipId(friendship.getId())
                .nickname(friendship.getDisplayName())
                .profileImageUrl(friendship.getFriend().getProfileImageUrl())
                .friendId(friendship.getFriend().getId())
                .build();
    }

    public static CreateSharedPlantInviteResDTO toCreateSharedPlantInviteResDTO(SharedPlantInvite invite) {
        return CreateSharedPlantInviteResDTO.builder()
                .inviteId(invite.getId())
                .status(invite.getStatus().name())
                .createdAt(invite.getCreatedAt().format(FORMATTER))
                .build();
    }

    public static MyPlantInviteResDTO toMyPlantInviteResDTO(SharedPlantInvite invite, String inviterName) {
        return MyPlantInviteResDTO.builder()
                .inviteId(invite.getId())
                .plantName(invite.getPlant().getName())
                .inviterId(invite.getInviter().getId())
                .inviterName(inviterName)
                .nickname(invite.getNickname())
                .message(invite.getMessage())
                .status(invite.getStatus().name())
                .createdAt(invite.getCreatedAt().format(FORMATTER))
                .build();
    }

    public static PlantInviteCandidatesResDto toPlantInviteCandidatesResDto(List<Friendship> friendships) {
        List<InviteCandidateInfoDTO> candidateInfos = friendships.stream()
                .map(SharedPlantInviteConverter::toInviteCandidateInfoDTO)
                .toList();

        return PlantInviteCandidatesResDto.builder()
                .count(candidateInfos.size())
                .friends(candidateInfos)
                .build();
    }

    public static MyPlantInviteListResDTO toMyPlantInviteListResDTO(List<SharedPlantInvite> i, List<Friendship> f) {
        Map<Long, String> inviterNames = f.stream()
                .collect(Collectors.toMap(
                        Friendship::getFriendId,
                        Friendship::getDisplayName,
                        (existing, replacement) -> existing // 중복 방지
                ));

        List<MyPlantInviteResDTO> inviteInfos = i.stream()
                .map(invite -> toMyPlantInviteResDTO(invite,
                        inviterNames.getOrDefault(invite.getInviter().getId(), invite.getInviter().getName())))
                .toList();

        return MyPlantInviteListResDTO.builder()
                .count(inviteInfos.size())
                .invitations(inviteInfos)
                .build();
    }

    public static UpdateSharedPlantInviteResDTO toUpdateSharedPlantInviteResDTO(SharedPlantInvite invite, SharedPlant sharedPlant) {
        return UpdateSharedPlantInviteResDTO.builder()
                .inviteId(invite.getId())
                .status(invite.getStatus().name())
                .sharedPlant(sharedPlant != null ? SharedPlantConverter.toAcceptedSharedPlantDTO(sharedPlant) : null)
                .build();
    }
}
