package com.cotato.itda.domain.challenge.entity;

import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.mission.entity.Mission;
import com.cotato.itda.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
@Table(name = "challenge")
@SQLRestriction("is_deleted = false")
public class Challenge extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "content")
    private String content;

    @Builder.Default
    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Builder.Default
    @Column(name = "comment_count", nullable = false)
    private int commentCount = 0;

    @Builder.Default
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

}
