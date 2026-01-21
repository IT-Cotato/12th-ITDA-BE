package com.cotato.itda.domain.challenge.repository;

import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.mission.entity.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    @Query("SELECT c FROM Challenge c WHERE c.member.id = :memberId AND c.mission = :mission")
    Optional<Challenge> findByMemberIdAndMission(Long memberId, Mission mission);

    // 이번주 챌린지 조회
    @Query("SELECT c FROM Challenge c "
            + "JOIN FETCH c.mission m "
            + "WHERE c.member.id = :memberId "
            + "AND m.missionDate >= :startDate " +
            "AND m.missionDate <= :endDate"
    )
    List<Challenge> findWeeklyChallenges(
            @Param("memberId") Long memberId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
