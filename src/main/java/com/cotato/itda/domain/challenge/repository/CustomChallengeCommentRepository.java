package com.cotato.itda.domain.challenge.repository;

import com.cotato.itda.domain.challenge.entity.ChallengeComment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

public interface CustomChallengeCommentRepository {

    Slice<ChallengeComment> findComments(Long challengeId, Long memberId, Long lastId, PageRequest pageRequest);
}
