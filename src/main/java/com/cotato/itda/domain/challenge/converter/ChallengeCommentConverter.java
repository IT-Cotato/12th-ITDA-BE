package com.cotato.itda.domain.challenge.converter;

import com.cotato.itda.domain.challenge.dto.request.ChallengeCommentRequest;
import com.cotato.itda.domain.challenge.dto.response.ChallengeCommentResponse;
import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.entity.ChallengeComment;
import com.cotato.itda.domain.member.entity.Member;

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
}
