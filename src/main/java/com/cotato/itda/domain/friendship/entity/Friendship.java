package com.cotato.itda.domain.friendship.entity;

import com.cotato.itda.domain.chattopic.entity.ChatTopic;
import com.cotato.itda.domain.friendship.entity.mapping.FriendshipTopic;
import com.cotato.itda.domain.friendship.enums.ChatGoal;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.enums.SpeechStyle;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Enumerated(EnumType.STRING)
    private ChatGoal chatGoal;

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
    @Builder.Default
    private List<FriendshipTopic> friendshipTopics = new ArrayList<>();

    public void update(String nickname, SpeechStyle speechStyle, ChatGoal chatGoal, FriendshipStatus status) {
        this.nickname = nickname;
        if (speechStyle != null)
            this.speechStyle = speechStyle;
        if (chatGoal != null)
            this.chatGoal = chatGoal;
        if (status != null)
            this.status = status;
    }

    public void replaceTopics(List<ChatTopic> topics) {
        Set<String> requestedCodes = topics.stream()
                .map(ChatTopic::getCode)
                .collect(Collectors.toSet());

        friendshipTopics.removeIf(friendshipTopic ->
                !requestedCodes.contains(friendshipTopic.getChatTopic().getCode()));

        Set<String> existingCodes = friendshipTopics.stream()
                .map(friendshipTopic -> friendshipTopic.getChatTopic().getCode())
                .collect(Collectors.toSet());

        List<FriendshipTopic> topicsToAdd = topics.stream()
                .filter(topic -> !existingCodes.contains(topic.getCode()))
                .map(topic -> FriendshipTopic.builder()
                        .friendship(this)
                        .chatTopic(topic)
                        .build())
                .toList();

        friendshipTopics.addAll(topicsToAdd);
    }

    public String getDisplayName() {
        return nickname != null ? nickname : friend.getName();
    }

    public Long getFriendId() {
        return friend.getId();
    }
}
