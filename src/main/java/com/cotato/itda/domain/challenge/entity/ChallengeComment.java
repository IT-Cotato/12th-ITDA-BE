package com.cotato.itda.domain.challenge.entity;

import com.cotato.itda.domain.diary.entity.DiaryComment;
import com.cotato.itda.domain.diary.exception.code.DiaryErrorCode;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.global.entity.BaseEntity;
import com.cotato.itda.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "challenge_comment")
public class ChallengeComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private ChallengeComment parentComment;

    @OneToMany(mappedBy = "parentComment")
    @Builder.Default
    private List<ChallengeComment> childComments = new ArrayList<>();

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void setParent(ChallengeComment parent) {
        if (parent == null) {
            return;
        }
        // depth 2 제한
        if (parent.getParentComment() != null) {
            throw new BusinessException(DiaryErrorCode.REPLY_DEPTH_LIMIT);
        }
        this.parentComment = parent;
        parent.getChildComments().add(this);
    }
}
