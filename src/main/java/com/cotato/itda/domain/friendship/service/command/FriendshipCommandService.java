package com.cotato.itda.domain.friendship.service.command;

import com.cotato.itda.domain.friendship.dto.req.FriendshipReqDTO;
import com.cotato.itda.domain.friendship.dto.res.FriendshipResDTO;
import org.springframework.transaction.annotation.Transactional;

public interface FriendshipCommandService {
    FriendshipResDTO.CreateDTO createFriendship(Long friendId, Long memberId);

    FriendshipResDTO.UpdateDTO updateFriendship(FriendshipReqDTO.UpdateDTO dto, Long friendshipId, Long memberId);

    void deleteFriendship(Long friendshipId, Long memberId);
}
