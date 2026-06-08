package com.cotato.itda.domain.diary.repository;

import com.cotato.itda.domain.diary.entity.DiaryComment;
import com.cotato.itda.domain.diary.entity.QDiaryComment;
import com.cotato.itda.domain.member.entity.QMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
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
    QDiaryComment child = new QDiaryComment("child");

    @Override
    public Slice<DiaryComment> findComments(Long diaryId, Long memberId, Long lastId, PageRequest pageRequest) {

        List<DiaryComment> comments = jpaQueryFactory
                .selectFrom(diaryComment)
                .join(diaryComment.member, member).fetchJoin() // member도 같이 조회
                .leftJoin(diaryComment.childComments, child).fetchJoin()
                .where(
                        diaryComment.diary.id.eq(diaryId),
                        diaryComment.parentComment.isNull(),
                        cursorCondition(lastId),

                        // 아직 삭제되지 않은 댓글이거나, 삭제되었더라도 대댓글이 남아 있는 경우에만 조회
                        diaryComment.isDeleted.isFalse()
                                .or(
                                        JPAExpressions.selectFrom(child)
                                                .where(child.parentComment.eq(diaryComment)
                                                        .and(child.isDeleted.isFalse()))
                                                .exists()
                                )
                )
                .orderBy(diaryComment.id.asc())
                .limit(pageRequest.getPageSize() + 1)
                .distinct()
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
