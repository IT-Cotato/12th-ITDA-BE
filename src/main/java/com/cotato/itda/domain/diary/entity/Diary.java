package com.cotato.itda.domain.diary.entity;

import com.cotato.itda.domain.diary.enums.EmojiCode;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(
    name = "diary",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_diary_member_date", columnNames = {"member_id", "diary_date"})
    },
    indexes = {
        @Index(name = "idx_diary_member_date", columnList = "member_id, diary_date")
    }
)
@SQLRestriction("is_deleted = false") // 삭제되지 않은 엔티티만 조회
public class Diary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "diary_date", nullable = false)
    private LocalDate date;

    @Column(name = "emoji_code", nullable = false)
    private EmojiCode emojiCode;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Integer commentCount = 0;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void update(LocalDate date, EmojiCode emojiCode, String content, String imageUrl) {
        this.date = date;
        this.emojiCode = emojiCode;
        this.content = content;
        this.imageUrl = imageUrl;
    }

    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    public void increaseLike() {
        this.likeCount++;
    }

    public void decreaseLike() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

}
