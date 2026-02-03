package com.cotato.itda.domain.challenge.repository;

import com.cotato.itda.domain.challenge.entity.ChallengeComment;
import com.cotato.itda.domain.challenge.entity.QChallengeComment;
import com.cotato.itda.domain.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@RequiredArgsConstructor
public class CustomChallengeCommentRepositoryImpl implements CustomChallengeCommentRepository{

    private final JPAQueryFactory jpaQueryFactory;
    QChallengeComment challengeComment = QChallengeComment.challengeComment;
    QMember member = QMember.member;

    @Override
    public Slice<ChallengeComment> findComments(Long challengeId, Long memberId, Long lastId, PageRequest pageRequest) {

        List<ChallengeComment> comments = jpaQueryFactory
                .selectFrom(challengeComment)
                .join(challengeComment.member, member).fetchJoin()
                .where(
                        challengeComment.challenge.id.eq(challengeId),
                        cursorCondition(lastId)
                )
                .orderBy(challengeComment.id.asc())
                .limit(pageRequest.getPageSize() + 1)
                .fetch();

        boolean hasNext = comments.size() > pageRequest.getPageSize();

        if (hasNext) {
            comments.remove(pageRequest.getPageSize());
        }

        return new SliceImpl<>(comments, pageRequest, hasNext);
    }

    private BooleanExpression cursorCondition(Long lastId) {
        if (lastId == null) {
            return null;
        }
        return challengeComment.id.gt(lastId);
    }

}
