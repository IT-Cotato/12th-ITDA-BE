package com.cotato.itda.domain.challenge.service.command;

import com.cotato.itda.domain.challenge.converter.ChallengeConverter;
import com.cotato.itda.domain.challenge.dto.request.ChallengeCreateRequest;
import com.cotato.itda.domain.challenge.dto.response.ChallengeResponse;
import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.entity.ChallengeView;
import com.cotato.itda.domain.challenge.repository.ChallengeRepository;
import com.cotato.itda.domain.challenge.repository.ChallengeViewRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.mission.entity.Mission;
import com.cotato.itda.domain.mission.repository.MissionRepository;
import com.cotato.itda.global.error.constant.ChallengeErrorCode;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeCommandService {

    private final MemberRepository memberRepository;
    private final MissionRepository missionRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeViewRepository challengeViewRepository;

    @Transactional
    public ChallengeResponse createChallenge(Long memberId, ChallengeCreateRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("memberId", memberId)));

        Mission mission = missionRepository.findById(request.missionId())
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.MISSION_NOT_FOUND,  Map.of("missionId", request.missionId())));

        // 요청한 미션이 오늘 날짜 미션인지 확인
        LocalDate today = LocalDate.now();
        if (!mission.getMissionDate().isEqual(today)) {
            throw new BusinessException(ChallengeErrorCode.MISSION_DATE_MISMATCH);
        }

        // 이미 참여한 미션인지 확인
        if (challengeRepository.existsByMemberIdAndMission(memberId, mission)){
            throw new BusinessException(ChallengeErrorCode.ALREADY_PARTICIPATED);
        }

        Challenge challenge = ChallengeConverter.toEntity(request, member, mission);
        Challenge savedChallenge = challengeRepository.save(challenge);

        //TODO: 영양제 점수 증가 로직 추가

        return ChallengeConverter.toResponse(savedChallenge, mission);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createChallengeView(Challenge challenge, Member member) {
        if (!challengeViewRepository.existsByChallengeAndMember(challenge, member)) {
                ChallengeView newView = ChallengeView.builder()
                        .challenge(challenge)
                        .member(member)
                        .build();
                challengeViewRepository.save(newView);
        }
    }
}
