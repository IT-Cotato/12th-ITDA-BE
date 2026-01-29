package com.cotato.itda.domain.friendship.repository;

import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findAllByMemberIdAndStatusIn(Long memberId, List<FriendshipStatus> statuses, Sort sort);

    List<Friendship> findAllByMember_IdAndStatus(Long memberId, FriendshipStatus status, Sort sort);

    List<Friendship> findAllByMember_IdAndFriend_IdInAndStatus(Long memberId, List<Long> friendIds,
            FriendshipStatus status);

    boolean existsByMember_IdAndFriend_Id(Long memberId, Long friendId);

    Optional<Friendship> findByMember_IdAndFriend_Id(Long memberId, Long friendId);
}
