package com.cotato.itda.domain.diary.repository;

import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.entity.QDiary;
import com.cotato.itda.domain.friendship.entity.QFriendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@RequiredArgsConstructor
public class CustomDiaryRepositoryImpl implements CustomDiaryRepository{

    private final JPAQueryFactory jpaQueryFactory;
    QDiary diary = QDiary.diary;
    QMember member = QMember.member;
    QFriendship friendship = QFriendship.friendship;

    @Override
    public Slice<Diary> findDiariesByMemberOrFriends(Long memberId, Long lastId, PageRequest pageRequest) {

        List<Diary> diaries = jpaQueryFactory
                .selectFrom(diary)

                // Member fetch join
                .leftJoin(diary.member, member).fetchJoin()

                // Friendship을 LEFT JOIN하여 일기 작성자가 친구인 일기만 조회
                .leftJoin(friendship)
                .on(friendship.member.id.eq(memberId)
                        .and(friendship.friend.id.eq(diary.member.id))
                        .and(friendship.status.eq(FriendshipStatus.ACTIVE)))

                .where(
                        diary.member.id.eq(memberId).or(friendship.id.isNotNull()),
                        cursorCondition(lastId)
                )
                .orderBy(diary.id.desc())
                .limit(pageRequest.getPageSize() + 1)
                .fetch();

        boolean hasNext = diaries.size() > pageRequest.getPageSize();
        if (hasNext) {
            diaries.remove(pageRequest.getPageSize());
        }

        return new SliceImpl<>(diaries, pageRequest, hasNext);
    }

    // 마지막 ID를 기준으로 특정 일기 ID보다 작은 일기만 조회
    private BooleanExpression cursorCondition(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return diary.id.lt(lastId);
    }

}
