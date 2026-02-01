package com.cotato.itda.domain.garden.repository;

import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantLog;
import com.cotato.itda.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface SharedPlantLogRepository extends JpaRepository<SharedPlantLog, Long> {

    boolean existsBySharedPlantAndWateredByAndUsedNutrientAndCreatedAtBetween(
            SharedPlant sharedPlant,
            Long wateredBy,
            boolean usedNutrient,
            LocalDateTime startOfDay,
            LocalDateTime endOfDay
    );
}
