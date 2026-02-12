package com.cotato.itda.domain.garden.repository;

import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SharedPlantRepository extends JpaRepository<SharedPlant, Long> {
    @Query("SELECT CASE WHEN sp.memberA.id = :memberId THEN sp.memberB.id ELSE sp.memberA.id END " +
            "FROM SharedPlant sp " +
            "WHERE (sp.memberA.id = :memberId OR sp.memberB.id = :memberId) " +
            "AND sp.status = com.cotato.itda.domain.garden.enums.SharedPlantStatus.GROWING")
    List<Long> findGrowingOrWitheredSharedPlantFriendIds(@Param("memberId") Long memberId);

    @Query("SELECT COUNT(sp) > 0 FROM SharedPlant sp " +
            "WHERE sp.memberA.id = :memberId1 AND sp.memberB.id = :memberId2 " +
            "AND sp.status = com.cotato.itda.domain.garden.enums.SharedPlantStatus.GROWING")
    boolean existsGrowingOrWitheredBetween(@Param("memberId1") Long memberId1, @Param("memberId2") Long memberId2);

    @Query("SELECT sp FROM SharedPlant sp " +
            "WHERE (sp.memberA.id = :memberId OR sp.memberB.id = :memberId) " +
            "AND sp.status IN :statuses " +
            "ORDER BY sp.createdAt DESC")
    List<SharedPlant> findAllByMemberIdAndStatusIn(@Param("memberId") Long memberId, @Param("statuses") List<SharedPlantStatus> statuses);
}
