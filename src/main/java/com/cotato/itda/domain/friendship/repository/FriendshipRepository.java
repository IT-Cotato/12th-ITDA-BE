package com.cotato.itda.domain.friendship.repository;

import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findAllByMemberIdAndStatusIn(Long memberId, List<FriendshipStatus> statuses, Sort sort);

    boolean existsByMemberIdAndFriendId(Long memberId, Long friendId);

    boolean existsByMemberIdAndFriendIdAndStatus(Long memberId, Long friendId, FriendshipStatus status);

    Optional<Friendship> findByMemberIdAndFriendIdAndStatus(Long memberId, Long friendId, FriendshipStatus status);

    List<Friendship> findAllByMemberIdAndFriendIdInAndStatus(Long memberId, List<Long> friendIds, FriendshipStatus friendshipStatus);
}
