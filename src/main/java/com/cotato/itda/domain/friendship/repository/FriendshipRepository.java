package com.cotato.itda.domain.friendship.repository;

import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.member.entity.Member;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
    List<Friendship> findAllByMemberIdAndStatusIn(Long memberId, List<FriendshipStatus> statuses, Sort sort);

    boolean existsByMemberIdAndFriendId(Long memberId, Long friendId);

    boolean existsByMemberIdAndFriendIdAndStatus(Long memberId, Long friendId, FriendshipStatus status);

    Optional<Friendship> findByMemberAndFriendAndStatus(Member member, Member friend, FriendshipStatus friendshipStatus);

    @Query("SELECT f FROM Friendship f " +
            "JOIN FETCH f.friend " +
            "WHERE f.member.id = :memberId " +
            "AND f.friend.id IN :friendIds " +
            "AND f.status = 'ACTIVE'")
    List<Friendship> findActiveFriendships(@Param("memberId") Long memberId,
                                           @Param("friendIds") List<Long> friendIds);
}
