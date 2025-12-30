package com.cotato.itda.domain.friendship.service.query;

import com.cotato.itda.domain.friendship.dto.res.FriendshipResDTO;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface FriendshipQueryService {
    FriendshipResDTO.FriendshipListDTO getFriendshipList(Long memberId, List<FriendshipStatus> statuses, Sort sort);

    FriendshipResDTO.FriendshipSettingsDTO getFriendshipSettings(Long friendshipId, Long memberId);
}
