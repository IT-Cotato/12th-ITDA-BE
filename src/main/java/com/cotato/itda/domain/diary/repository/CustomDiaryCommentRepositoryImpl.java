package com.cotato.itda.domain.diary.repository;

import com.cotato.itda.domain.diary.entity.DiaryComment;
import com.cotato.itda.domain.diary.entity.QDiaryComment;
import com.cotato.itda.domain.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.util.List;

@RequiredArgsConstructor
public class CustomDiaryCommentRepositoryImpl implements CustomDiaryCommentRepository{

    private final JPAQueryFactory jpaQueryFactory;
    QDiaryComment diaryComment = QDiaryComment.diaryComment;
    QMember member = QMember.member;

    @Override
    public Slice<DiaryComment> findComments(Long diaryId, Long memberId, Long lastId, PageRequest pageRequest) {

        List<DiaryComment> comments = jpaQueryFactory
                .selectFrom(diaryComment)
                .join(diaryComment.member, member).fetchJoin() // member도 같이 조회
                .where(
                        diaryComment.diary.id.eq(diaryId),
                        cursorCondition(lastId)
                )
                .orderBy(diaryComment.id.asc())
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
        return diaryComment.id.gt(lastId); // 댓글 오름차순 조회
    }

}
