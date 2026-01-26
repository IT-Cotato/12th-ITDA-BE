package com.cotato.itda.domain.friendship.converter;

import com.cotato.itda.domain.friendship.dto.res.FriendshipResDTO;
import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.member.entity.Member;
import java.util.List;

public class FriendshipConverter {

    // Entity -> DTO
    public static FriendshipResDTO.CreateDTO toCreateDTO(Friendship friendship) {

        FriendshipResDTO.FriendInfoDTO friendInfo = FriendshipResDTO.FriendInfoDTO.builder()
                .id(friendship.getFriend().getId())
                .name(friendship.getFriend().getName())
                .profileImageUrl(friendship.getFriend().getProfileImageUrl())
                .build();

        return FriendshipResDTO.CreateDTO.builder()
                .friendshipId(friendship.getId())
                .friendInfo(friendInfo)
                .status(friendship.getStatus())
                .createdAt(friendship.getCreatedAt())
                .build();
    }

    public static Friendship toFriendship(Member member, Member friend) {
        return Friendship.builder()
                .member(member)
                .friend(friend)
                .build();
    }

    // Entity -> DTO
    public static FriendshipResDTO.UpdateDTO toUpdateDTO(Friendship friendship) {
        return FriendshipResDTO.UpdateDTO.builder()
                .friendshipId(friendship.getId())
                .status(friendship.getStatus())
                .updatedAt(friendship.getUpdatedAt())
                .build();
    }

    public static FriendshipResDTO.FriendshipItemDTO toFriendshipItemDTO(Friendship friendship) {
        return FriendshipResDTO.FriendshipItemDTO.builder()
                .friendshipId(friendship.getId())
                .friendId(friendship.getFriend().getId())
                .showName(friendship.getDisplayName())
                .profileImageUrl(friendship.getFriend().getProfileImageUrl())
                .status(friendship.getStatus())
                .lastInteractedAt(friendship.getLastInteractedAt())
                .build();
    }

    public static FriendshipResDTO.FriendshipListDTO toFriendshipListDTO(List<Friendship> friendships) {
        List<FriendshipResDTO.FriendshipItemDTO> items = friendships.stream()
                .map(FriendshipConverter::toFriendshipItemDTO)
                .toList();

        return FriendshipResDTO.FriendshipListDTO.builder()
                .count(friendships.size())
                .friendshipList(items)
                .build();
    }

    public static FriendshipResDTO.FriendshipSettingsDTO toFriendshipSettingsDTO(Friendship friendship) {

        List<String> topicCodes = friendship.getFriendshipTopics().stream()
                .map(ft -> ft.getChatTopic().getCode())
                .toList();

        return FriendshipResDTO.FriendshipSettingsDTO.builder()
                .friendshipId(friendship.getId())
                .nickname(friendship.getNickname())
                .speechStyle(friendship.getSpeechStyle())
                .chatGoal(friendship.getChatGoal())
                .topicCodes(topicCodes)
                .build();
    }
}
