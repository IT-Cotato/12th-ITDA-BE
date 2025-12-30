package com.cotato.itda.domain.friendship.entity.mapping;

import com.cotato.itda.domain.chattopic.entity.ChatTopic;
import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Table(name = "friendship_topic")
public class FriendshipTopic extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friendship_id", nullable = false)
    private Friendship friendship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_topic_id", nullable = false)
    private ChatTopic chatTopic;
}
