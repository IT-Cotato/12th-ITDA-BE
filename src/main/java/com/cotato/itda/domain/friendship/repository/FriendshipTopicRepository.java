package com.cotato.itda.domain.friendship.repository;

import com.cotato.itda.domain.friendship.entity.mapping.FriendshipTopic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendshipTopicRepository extends JpaRepository<FriendshipTopic, Long> {
    List<FriendshipTopic> findByFriendshipId(Long id);
}
