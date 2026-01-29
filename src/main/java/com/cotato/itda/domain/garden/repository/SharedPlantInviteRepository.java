package com.cotato.itda.domain.garden.repository;

import com.cotato.itda.domain.garden.entity.SharedPlantInvite;
import com.cotato.itda.domain.garden.enums.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface SharedPlantInviteRepository extends JpaRepository<SharedPlantInvite, Long> {
    @Query("SELECT CASE WHEN spi.inviter.id = :memberId THEN spi.invitee.id ELSE spi.inviter.id END " +
            "FROM SharedPlantInvite spi " +
            "WHERE (spi.inviter.id = :memberId OR spi.invitee.id = :memberId) " +
            "AND spi.status = com.cotato.itda.domain.garden.enums.InviteStatus.PENDING")
    List<Long> findPendingInviteFriendIds(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(spi) > 0 FROM SharedPlantInvite spi " +
            "WHERE ((spi.inviter.id = :memberId1 AND spi.invitee.id = :memberId2) " +
            "OR (spi.inviter.id = :memberId2 AND spi.invitee.id = :memberId1)) " +
            "AND spi.status = com.cotato.itda.domain.garden.enums.InviteStatus.PENDING")
    boolean existsPendingInviteBetween(@Param("memberId1") Long memberId1, @Param("memberId2") Long memberId2);

    List<SharedPlantInvite> findAllByInviteeIdAndStatusOrderByCreatedAtAsc(Long inviteeId, InviteStatus status);
}
