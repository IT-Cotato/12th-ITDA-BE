package com.cotato.itda.domain.challenge.repository;

import com.cotato.itda.domain.challenge.entity.Challenge;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

import java.time.LocalDateTime;

public interface CustomChallengeRepository {

    Slice<Challenge> findFriendChallenges
            (Long memberId, Long lastId, LocalDateTime startOfToday, LocalDateTime startOfNextDay, PageRequest pageRequest);
}
