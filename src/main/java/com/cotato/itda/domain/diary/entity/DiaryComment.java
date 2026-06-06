package com.cotato.itda.domain.diary.entity;

import com.cotato.itda.domain.diary.exception.code.DiaryErrorCode;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.global.entity.BaseEntity;
import com.cotato.itda.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "diary_comment")
@SQLRestriction("is_deleted = false")
public class DiaryComment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = false)
    private Diary diary;

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
    private DiaryComment parentComment;

    @OneToMany(mappedBy = "parentComment")
    @Builder.Default
    private List<DiaryComment> childComments = new ArrayList<>();

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void setParent(DiaryComment parent) {
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
