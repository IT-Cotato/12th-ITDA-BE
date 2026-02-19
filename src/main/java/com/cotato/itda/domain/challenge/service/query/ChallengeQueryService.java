package com.cotato.itda.domain.challenge.service.query;

import com.cotato.itda.domain.challenge.converter.ChallengeConverter;
import com.cotato.itda.domain.challenge.dto.response.ChallengeDashboardResponse;
import com.cotato.itda.domain.challenge.dto.response.ChallengeDetailResponse;
import com.cotato.itda.domain.challenge.dto.response.ChallengeListResponse;
import com.cotato.itda.domain.challenge.dto.response.MyChallengeResponse;
import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.repository.ChallengeLikeRepository;
import com.cotato.itda.domain.challenge.repository.ChallengeRepository;
import com.cotato.itda.domain.challenge.repository.ChallengeViewRepository;
import com.cotato.itda.domain.challenge.service.command.ChallengeCommandService;
import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.mission.entity.Mission;
import com.cotato.itda.domain.mission.repository.MissionRepository;
import com.cotato.itda.domain.challenge.exception.code.ChallengeErrorCode;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeQueryService {

    private final MemberRepository memberRepository;
    private final FriendshipRepository friendshipRepository;
    private final MissionRepository missionRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeCommandService challengeCommandService;
    private final ChallengeLikeRepository challengeLikeRepository;
    private final ChallengeViewRepository challengeViewRepository;

    public ChallengeDashboardResponse getChallengeDashboard(Long memberId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        log.info("[TIMEZONE DEBUG] Current date in Asia/Seoul: {}", today);
        log.info("[TIMEZONE DEBUG] System default timezone: {}", ZoneId.systemDefault());

        // 1. 오늘 미션 조회
        Mission mission = missionRepository.findByMissionDate(today)
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.MISSION_NOT_FOUND));
        log.info("[TIMEZONE DEBUG] Found mission with missionDate: {}, missionId: {}", mission.getMissionDate(), mission.getId());

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

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

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
            nickname = writer.getName();
        } else {
            // 작성자가 친구 관계인지 확인
            Friendship friendship = friendshipRepository.findByMemberAndFriendAndStatus(member, writer, FriendshipStatus.ACTIVE)
                    .orElseThrow(() -> new BusinessException(ChallengeErrorCode.CHALLENGE_FORBIDDEN));

            nickname = friendship.getDisplayName();
        }

        // 작성자가 본인이 아닌 경우 ChallengeView 데이터 생성
        if (!isMe) {
            challengeCommandService.createChallengeView(challenge, member);
        }

        boolean isLiked = challengeLikeRepository.existsByChallengeAndMember(challenge, member);

        ChallengeDetailResponse.MemberInfo memberInfo = ChallengeConverter.toMemberInfo(writer, nickname);
        return ChallengeConverter.toDetailResponse(challenge, memberInfo, isLiked);
    }

    public ChallengeListResponse getChallengeList(Long memberId, Long lastId, int size) {

        // 1. 날짜 범위 계산
        LocalDateTime startOfToday = LocalDate.now(ZoneId.of("Asia/Seoul")).atStartOfDay();
        LocalDateTime startOfNextDay = LocalDate.now(ZoneId.of("Asia/Seoul")).plusDays(1).atStartOfDay();

        // 2. challenge 리스트 조회
        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Challenge> challengeSlice = challengeRepository.findFriendChallenges(
                memberId, lastId, startOfToday, startOfNextDay, pageRequest);

        List<Challenge> challenges = challengeSlice.getContent();

        // 3. 작성자의 friendship nickname 조회
        Map<Long, String> friendshipNicknameMap = getFriendNicknameMap(memberId, challenges);

        // 4. 읽음 여부 일괄 조회
        List<Long> challengeIds = challenges.stream().map(Challenge::getId).toList();
        Set<Long> viewedIds;
        if (challengeIds.isEmpty()) {
            viewedIds = Set.of();
        } else {
            viewedIds = new HashSet<>(challengeViewRepository.findViewedChallengeIds(challengeIds, memberId));
        }

        List<ChallengeListResponse.ChallengeItem> challengeItems = challenges.stream()
                .map(challenge -> {
                    Member member = challenge.getMember();

                    String nickname = friendshipNicknameMap.get(member.getId());

                    return ChallengeConverter.toListItem(
                            ChallengeConverter.toListMemberInfo(member, nickname),
                            challenge,
                            viewedIds.contains(challenge.getId())
                    );
                })
                .toList();

        // 커서 ID 계산
        Long newLastId = challengeItems.isEmpty() ? null : challengeItems.get(challengeItems.size() - 1).challengeId();

        return ChallengeConverter.toListResponse(challengeItems, newLastId, challengeSlice.hasNext());
    }

    // 작성자들의 nickname 일괄 조회
    private Map<Long, String> getFriendNicknameMap(Long memberId, List<Challenge> challenges) {

        // 작성자 ID 리스트 추출
        List<Long> writerIds = challenges.stream()
                .map(challenge -> challenge.getMember().getId())
                .distinct()
                .toList();

        if (writerIds.isEmpty()) {
            return Map.of();
        }

        // Friendship 조회 후 map 변환
        return friendshipRepository.findActiveFriendships(memberId, writerIds)
                .stream()
                .collect(Collectors.toMap(
                        f -> f.getFriend().getId(),
                        Friendship::getDisplayName
                ));
    }

}
