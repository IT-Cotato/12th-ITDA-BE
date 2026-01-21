package com.cotato.itda.domain.challenge.service.query;

import com.cotato.itda.domain.challenge.converter.ChallengeConverter;
import com.cotato.itda.domain.challenge.dto.response.ChallengeDashboardResponse;
import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.repository.ChallengeRepository;
import com.cotato.itda.domain.mission.entity.Mission;
import com.cotato.itda.domain.mission.repository.MissionRepository;
import com.cotato.itda.global.error.constant.ChallengeErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeQueryService {

    private final MissionRepository missionRepository;
    private final ChallengeRepository challengeRepository;

    public ChallengeDashboardResponse getChallengeDashboard(Long memberId) {
        LocalDate today = LocalDate.now();

        // 1. 오늘 미션 조회
        Mission mission = missionRepository.findByMissionDate(today)
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.MISSION_NOT_FOUND));

        // 2. 오늘의 내 챌린지 조회 (없는 경우 null 반환)
        Challenge myChallenge = challengeRepository.findByMemberIdAndMission(memberId, mission)
                .orElse(null);

        // 3. 이번주(월~일) 챌린지 기록 조회
        LocalDate startDate = today.with(DayOfWeek.MONDAY);
        LocalDate endDate = today.with(DayOfWeek.SUNDAY);
        List<Challenge> weeklyChallenges = challengeRepository.findWeeklyChallenges(memberId, startDate, endDate);

        return ChallengeConverter.toDashboardResponse(today, mission, myChallenge, weeklyChallenges);
    }

}
