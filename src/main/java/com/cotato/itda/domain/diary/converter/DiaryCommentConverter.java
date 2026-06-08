package com.cotato.itda.domain.diary.converter;

import com.cotato.itda.domain.diary.dto.request.DiaryCommentRequest;
import com.cotato.itda.domain.diary.dto.response.DiaryCommentListResponse;
import com.cotato.itda.domain.diary.dto.response.DiaryCommentResponse;
import com.cotato.itda.domain.diary.dto.response.WriterInfo;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.entity.DiaryComment;
import com.cotato.itda.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.List;

public class DiaryCommentConverter {

    public static DiaryComment toEntity(Diary diary, Member member, DiaryCommentRequest request) {
        return DiaryComment.builder()
                .diary(diary)
                .member(member)
                .content(request.content())
                .build();
    }

    public static DiaryCommentResponse toResponse(DiaryComment comment, WriterInfo writerInfo) {
        Long parentId = (comment.getParentComment() != null) ? comment.getParentComment().getId() : null;

        return DiaryCommentResponse.builder()
                .commentId(comment.getId())
                .parentId(parentId)
                .writerInfo(writerInfo)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public static WriterInfo toWriterInfo(Member member, String nickname, boolean isMe) {
        Long memberId = (member == null) ? null : member.getId();
        return WriterInfo.builder()
                .memberId(memberId)
                .nickname(nickname) // 댓글 등록 응답의 경우, 내 프로필 이름
                .profileImageUrl(member.getProfileImageUrl())
                .isMe(isMe)
                .build();
    }

    public static DiaryCommentListResponse.CommentItem toListItem(
            DiaryComment comment,
            WriterInfo writerInfo,
            List<DiaryCommentListResponse.CommentItem> childComments
    ) {
        return DiaryCommentListResponse.CommentItem.builder()
                .commentId(comment.getId())
                .writerInfo(writerInfo)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .childComments(childComments != null ? childComments : new ArrayList<>()) // ◀ 대댓글 리스트 주입
                .isDeleted(false)
                .build();
    }

    public static DiaryCommentListResponse toListResponse(
            List<DiaryCommentListResponse.CommentItem> items,
            Long lastId,
            boolean hasNext
    ) {
        return DiaryCommentListResponse.builder()
                .comments(items)
                .lastId(lastId)
                .hasNext(hasNext)
                .build();
    }

    public static DiaryCommentListResponse.CommentItem toDeletedListItem(
            DiaryComment root,
            WriterInfo unknownWriter,
            List<DiaryCommentListResponse.CommentItem> childItems
    ) {
        return DiaryCommentListResponse.CommentItem.builder()
                .commentId(root.getId())
                .content("삭제된 댓글입니다.")
                .writerInfo(unknownWriter)
                .childComments(childItems)
                .createdAt(root.getCreatedAt())
                .isDeleted(true)
                .build();
    }
}
