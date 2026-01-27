package com.cotato.itda.domain.challenge.repository;

import com.cotato.itda.domain.challenge.entity.ChallengeComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChallengeCommentRepository extends JpaRepository<ChallengeComment, Long>, CustomChallengeCommentRepository {
}
