package com.cotato.itda.domain.challenge.repository;

import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.entity.QChallenge;
import com.cotato.itda.domain.friendship.entity.QFriendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class CustomChallengeRepositoryImpl implements CustomChallengeRepository{

    private final JPAQueryFactory jpaQueryFactory;
    QChallenge challenge = QChallenge.challenge;
    QMember member = QMember.member;
    QFriendship friendship = QFriendship.friendship;

    @Override
    public Slice<Challenge> findFriendChallenges(Long memberId,
                                                 Long lastId,
                                                 LocalDateTime startOfToday,
                                                 LocalDateTime startOfNextDay,
                                                 PageRequest pageRequest) {

        List<Challenge> challenges = jpaQueryFactory
                .selectFrom(challenge)
                .join(challenge.member, member).fetchJoin()

                .leftJoin(friendship)
                .on(friendship.member.id.eq(memberId)
                        .and(friendship.friend.id.eq(challenge.member.id))
                        .and(friendship.status.eq(FriendshipStatus.ACTIVE)))

                .where(
                        // JOIN 결과가 존재하는 데이터만 필터링
                        friendship.id.isNotNull(),

                        todayCondition(startOfToday, startOfNextDay),
                        cursorCondition(lastId)
                )
                .orderBy(challenge.id.desc())
                .limit(pageRequest.getPageSize() + 1)
                .fetch();


        boolean hasNext = challenges.size() > pageRequest.getPageSize();
        if (hasNext) {
            challenges.remove(pageRequest.getPageSize());
        }

        return new SliceImpl<>(challenges, pageRequest, hasNext);
    }

    // 마지막 ID를 기준으로 특정 챌린지 ID보다 작은 챌린지만 조회
    private BooleanExpression cursorCondition(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return challenge.id.lt(lastId);
    }

    // createdAt이 오늘에 해당하는 챌린지만 조회 (오늘 00:00(포함) ~ 내일 00:00(미만))
    private BooleanExpression todayCondition(LocalDateTime startOfToday, LocalDateTime startOfNextDay) {
        return challenge.createdAt.goe(startOfToday)
                .and(challenge.createdAt.lt(startOfNextDay));
    }

}
