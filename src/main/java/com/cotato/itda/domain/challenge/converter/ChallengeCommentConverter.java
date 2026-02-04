package com.cotato.itda.domain.challenge.converter;

import com.cotato.itda.domain.challenge.dto.request.ChallengeCommentRequest;
import com.cotato.itda.domain.challenge.dto.response.ChallengeCommentListResponse;
import com.cotato.itda.domain.challenge.dto.response.ChallengeCommentResponse;
import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.entity.ChallengeComment;
import com.cotato.itda.domain.member.entity.Member;

import java.util.List;

public class ChallengeCommentConverter {

    public static ChallengeComment toEntity(Challenge challenge, Member member, ChallengeCommentRequest request) {
        return ChallengeComment.builder()
                .challenge(challenge)
                .member(member)
                .content(request.content())
                .build();
    }

    public static ChallengeCommentResponse toResponse(ChallengeComment comment, Member member) {
        return ChallengeCommentResponse.builder()
                .commentId(comment.getId())
                .writerInfo(toWriterInfo(member))
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    private static ChallengeCommentResponse.WriterInfo toWriterInfo(Member member) {
        return ChallengeCommentResponse.WriterInfo.builder()
                .memberId(member.getId())
                .name(member.getName())
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }

    public static ChallengeCommentListResponse toListResponse(
            List<ChallengeCommentListResponse.CommentItem> items,
            Long lastId,
            boolean hasNext
    ) {
        return ChallengeCommentListResponse.builder()
                .comments(items)
                .lastId(lastId)
                .hasNext(hasNext)
                .build();
    }

    public static ChallengeCommentListResponse.CommentItem toListItem(
            ChallengeComment comment,
            ChallengeCommentListResponse.WriterInfo writerInfo
    ) {
        return ChallengeCommentListResponse.CommentItem.builder()
                .commentId(comment.getId())
                .writerInfo(writerInfo)
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .build();
    }

    public static ChallengeCommentListResponse.WriterInfo toListWriterInfo(Member member, String nickname) {
        return ChallengeCommentListResponse.WriterInfo.builder()
                .memberId(member.getId())
                .nickname(nickname)
                .profileImageUrl(member.getProfileImageUrl())
                .build();
    }
}
