package com.cotato.itda.domain.diary.converter;

import com.cotato.itda.domain.diary.dto.request.DiaryCommentRequest;
import com.cotato.itda.domain.diary.dto.response.DiaryCommentListResponse;
import com.cotato.itda.domain.diary.dto.response.DiaryCommentResponse;
import com.cotato.itda.domain.diary.dto.response.DiaryListResponse;
import com.cotato.itda.domain.diary.dto.response.WriterInfo;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.entity.DiaryComment;
import com.cotato.itda.domain.member.entity.Member;

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
        return DiaryCommentResponse.builder()
                .commentId(comment.getId())
                .writerInfo(writerInfo)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public static WriterInfo toWriterInfo(Member member, String nickname, boolean isMe) {
        return WriterInfo.builder()
                .memberId(member.getId())
                .nickname(nickname) // 댓글 등록 응답의 경우, 내 프로필 이름
                .profileImageUrl(member.getProfileImageUrl())
                .isMe(isMe)
                .build();
    }

    public static DiaryCommentListResponse.CommentItem toListItem(DiaryComment comment, WriterInfo writerInfo) {
        return DiaryCommentListResponse.CommentItem.builder()
                .commentId(comment.getId())
                .writerInfo(writerInfo)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
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
}
