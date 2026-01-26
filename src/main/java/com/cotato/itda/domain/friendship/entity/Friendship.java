package com.cotato.itda.domain.friendship.entity;

import com.cotato.itda.domain.friendship.entity.mapping.FriendshipTopic;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.enums.SpeechStyle;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.global.entity.BaseEntity;
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
@Table(name = "friendship")
public class Friendship extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private Member friend;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "speech_style")
    @Enumerated(EnumType.STRING)
    private SpeechStyle speechStyle;

    @Column(name = "chat_goal")
    private String chatGoal;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FriendshipStatus status = FriendshipStatus.PENDING;

    @Column(name = "is_favorite", nullable = false)
    @Builder.Default
    private Boolean isFavorite = false;

    @Column(name = "last_interacted_at", nullable = false)
    @Builder.Default
    private LocalDateTime lastInteractedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "friendship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendshipTopic> friendshipTopics = new ArrayList<>();

    public void update(String nickname, SpeechStyle speechStyle, String chatGoal, FriendshipStatus status) {
        this.nickname = nickname;
        if (speechStyle != null) this.speechStyle = speechStyle;
        if (chatGoal != null) this.chatGoal = chatGoal;
        if (status != null) this.status = status;
    }

    public String getDisplayName() {
        return nickname != null ? nickname : friend.getName();
    }
}
