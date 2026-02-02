package com.cotato.itda.domain.mission.repository;

import com.cotato.itda.domain.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {

    Optional<Mission> findByMissionDate(LocalDate missionDate);
}
