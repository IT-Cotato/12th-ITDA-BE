package com.cotato.itda.domain.friendship.repository;

import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findAllByMemberIdAndStatusIn(Long memberId, List<FriendshipStatus> statuses, Sort sort);

    boolean existsByMemberIdAndFriendId(Long memberId, Long friendId);
}
