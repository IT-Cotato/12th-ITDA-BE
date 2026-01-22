package com.cotato.itda.domain.challenge.service.query;

import com.cotato.itda.domain.challenge.converter.ChallengeConverter;
import com.cotato.itda.domain.challenge.dto.response.ChallengeDashboardResponse;
import com.cotato.itda.domain.challenge.dto.response.ChallengeDetailResponse;
import com.cotato.itda.domain.challenge.dto.response.MyChallengeResponse;
import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.repository.ChallengeRepository;
import com.cotato.itda.domain.challenge.service.command.ChallengeCommandService;
import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.mission.entity.Mission;
import com.cotato.itda.domain.mission.repository.MissionRepository;
import com.cotato.itda.global.error.constant.ChallengeErrorCode;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeQueryService {

    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;
    private final MissionRepository missionRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeCommandService challengeCommandService;

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

    public MyChallengeResponse getMyChallenge(Long memberId) {

        LocalDate today = LocalDate.now();

        Mission mission = missionRepository.findByMissionDate(today)
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.MISSION_NOT_FOUND));

        Challenge myChallenge = challengeRepository.findByMemberIdAndMission(memberId, mission)
                .orElse(null);

        return ChallengeConverter.toMyChallengeResponse(myChallenge);
    }

    public ChallengeDetailResponse getChallengeDetail(Long memberId, Long challengeId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("memberId", memberId)));

        Challenge challenge = challengeRepository.findByIdWithMember(challengeId)
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

        Member writer = challenge.getMember();
        boolean isMe = writer.getId().equals(memberId);
        String nickname;

        if (isMe) {
            nickname = writer.getProfileName();
        } else {
            // 작성자가 친구 관계인지 확인
            Friendship friendship = friendshipRepository.findByMemberIdAndFriendIdAndStatus(memberId, writer.getId(), FriendshipStatus.ACTIVE)
                    .orElseThrow(() -> new BusinessException(ChallengeErrorCode.CHALLENGE_FORBIDDEN));

            nickname = determineNickname(writer, friendship.getNickname());
        }

        // ChallengeView 데이터 생성
        challengeCommandService.createChallengeView(challenge, member);

        //TODO: 좋아요 기능 구현 후 수정
        boolean isLiked = false;

        ChallengeDetailResponse.MemberInfo memberInfo = ChallengeConverter.toMemberInfo(writer, nickname);
        return ChallengeConverter.toDetailResponse(challenge, memberInfo, isLiked);
    }

    private String determineNickname(Member writer, String friendshipNickname) {
        return friendshipNickname != null ? friendshipNickname : writer.getProfileName();
    }

}
