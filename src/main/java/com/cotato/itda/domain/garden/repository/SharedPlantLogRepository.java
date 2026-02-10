package com.cotato.itda.domain.garden.repository;

import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SharedPlantLogRepository extends JpaRepository<SharedPlantLog, Long> {

    boolean existsBySharedPlantAndWateredByAndUsedNutrientAndCreatedAtBetween(
            SharedPlant sharedPlant,
            Long wateredBy,
            boolean usedNutrient,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );

    @Query("SELECT spl FROM SharedPlantLog spl " +
            "WHERE spl.sharedPlant.id IN :sharedPlantIds AND spl.wateredBy = :memberId " +
            "AND spl.wateredAt = (SELECT MAX(spl2.wateredAt) FROM SharedPlantLog spl2 " +
            "WHERE spl2.sharedPlant.id = spl.sharedPlant.id AND spl2.wateredBy = :memberId)")
    List<SharedPlantLog> findLatestLogsBySharedPlantIdsAndMemberId(
            @Param("sharedPlantIds") List<Long> sharedPlantIds,
            @Param("memberId") Long memberId
    );

    Optional<SharedPlantLog> findTopBySharedPlantOrderByCreatedAtDesc(SharedPlant sharedPlant);
}
