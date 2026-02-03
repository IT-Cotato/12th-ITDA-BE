package com.cotato.itda.domain.challenge.converter;

import com.cotato.itda.domain.challenge.dto.request.ChallengeCreateRequest;
import com.cotato.itda.domain.challenge.dto.response.*;
import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.enums.ChallengeStatus;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.mission.entity.Mission;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ChallengeConverter {

    public static ChallengeDashboardResponse toDashboardResponse(
            LocalDate today,
            Mission mission,
            Challenge myChallenge,
            List<Challenge> weeklyChallenges
    ) {
        return ChallengeDashboardResponse.builder()
                .todayDate(today)
                .mission(toTodayMissionInfo(mission))
                .myChallenge(toMyChallengeInfo(myChallenge))
                .weeklyStatus(toWeeklyStatus(today, weeklyChallenges))
                .build();
    }

    public static ChallengeDashboardResponse.TodayMissionInfo toTodayMissionInfo(Mission mission) {
        return ChallengeDashboardResponse.TodayMissionInfo.builder()
                .missionId(mission.getId())
                .missionDate(mission.getMissionDate())
                .category(mission.getCategory())
                .keyword(mission.getKeyword())
                .content(mission.getContent())
                .build();
    }

    public static ChallengeDashboardResponse.MyChallengeInfo toMyChallengeInfo(Challenge challenge) {
        if (challenge == null) {
            return null;
        }
        return ChallengeDashboardResponse.MyChallengeInfo.builder()
                .challengeId(challenge.getId())
                .imageUrl(challenge.getImageUrl())
                .build();
    }

    public static List<ChallengeDashboardResponse.WeeklyStatus> toWeeklyStatus(LocalDate today, List<Challenge> challenges) {
        List<ChallengeDashboardResponse.WeeklyStatus> statusList = new ArrayList<>();
        LocalDate startDate = today.with(DayOfWeek.MONDAY); // 이번주 월요일 날짜

        for (int i = 0; i < 7; i++) {
            // 1. 날짜 계산
            LocalDate targetDate = startDate.plusDays(i);

            // 2. 해당 날짜에 challenge 데이터가 있는지 확인
            boolean isDone = false;
            for (Challenge c : challenges) {
                // mission_date가 해당 날짜와 일치하는 경우, 미션 참여 완료
                if (c.getMission().getMissionDate().isEqual(targetDate)) {
                    isDone = true;
                    break;
                }
            }

            // 3. 참여 상태 판별
            ChallengeStatus status;
            if (isDone) {
                status = ChallengeStatus.DONE;
            } else if (targetDate.isAfter(today)) {
                status = ChallengeStatus.FUTURE;
            } else if (targetDate.isEqual(today)) {
                status = ChallengeStatus.WAITING;
            } else {
                status = ChallengeStatus.MISSED;
            }

            statusList.add(
                    ChallengeDashboardResponse.WeeklyStatus.builder()
                            .date(targetDate)
                            .status(status)
                            .build()
            );
        }
        return statusList;
    }

    public static Challenge toEntity(ChallengeCreateRequest request, Member member, Mission mission) {
        return Challenge.builder()
                .member(member)
                .mission(mission)
                .imageUrl(request.imageUrl())
                .build();
    }

    public static ChallengeResponse toResponse(Challenge challenge, Mission mission) {
        return ChallengeResponse.builder()
                .missionId(mission.getId())
                .challengeId(challenge.getId())
                .imageUrl(challenge.getImageUrl())
                .likeCount(challenge.getLikeCount())
                .commentCount(challenge.getCommentCount())
                .createdAt(challenge.getCreatedAt())
                .updatedAt(challenge.getUpdatedAt())
                .build();
    }

    public static MyChallengeResponse toMyChallengeResponse(Challenge challenge) {
        if (challenge == null) {
            return MyChallengeResponse.builder()
                    .isCompleted(false)
                    .challengeId(null)
                    .imageUrl(null)
                    .createdAt(null)
                    .build();
        }
        return MyChallengeResponse.builder()
                .isCompleted(true)
                .challengeId(challenge.getId())
                .imageUrl(challenge.getImageUrl())
                .createdAt(challenge.getCreatedAt())
                .build();
    }

    public static ChallengeDetailResponse toDetailResponse(Challenge challenge, ChallengeDetailResponse.MemberInfo memberInfo, boolean isLiked) {
        return ChallengeDetailResponse.builder()
                .memberInfo(memberInfo)
                .challengeId(challenge.getId())
                .imageUrl(challenge.getImageUrl())
                .createdAt(challenge.getCreatedAt())
                .isLiked(isLiked)
                .likeCount(challenge.getLikeCount())
                .commentCount(challenge.getCommentCount())
                .build();
    }

    public static ChallengeDetailResponse.MemberInfo toMemberInfo(Member member, String nickname) {
        return ChallengeDetailResponse.MemberInfo.builder()
                .memberId(member.getId())
                .nickname(nickname)
                .build();

    }

    public static ChallengeListResponse toListResponse(
            List<ChallengeListResponse.ChallengeItem> items,
            Long lastId,
            boolean hasNext
    ) {
        return ChallengeListResponse.builder()
                .challenges(items)
                .lastId(lastId)
                .hasNext(hasNext)
                .build();

    }

    public static ChallengeListResponse.ChallengeItem toListItem(
            ChallengeListResponse.MemberInfo memberInfo,
            Challenge challenge,
            boolean isViewed
    ){
        return ChallengeListResponse.ChallengeItem.builder()
                .memberInfo(memberInfo)
                .challengeId(challenge.getId())
                .imageUrl(challenge.getImageUrl())
                .createdAt(challenge.getCreatedAt())
                .isViewed(isViewed)
                .build();

    }

    public static ChallengeListResponse.MemberInfo toListMemberInfo(Member member, String nickname) {
        return ChallengeListResponse.MemberInfo.builder()
                .memberId(member.getId())
                .nickname(nickname)
                .build();
    }
}
