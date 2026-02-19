package com.cotato.itda.domain.mission.repository;

import com.cotato.itda.domain.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    @Query(value = "SELECT * FROM mission WHERE mission_date = CAST(:missionDate AS DATE)", nativeQuery = true)
    Optional<Mission> findByMissionDate(@Param("missionDate") LocalDate missionDate);
}
