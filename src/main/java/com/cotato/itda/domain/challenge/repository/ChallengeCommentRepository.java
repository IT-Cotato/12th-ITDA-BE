package com.cotato.itda.domain.challenge.repository;

import com.cotato.itda.domain.challenge.entity.ChallengeComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChallengeCommentRepository extends JpaRepository<ChallengeComment, Long>, CustomChallengeCommentRepository {
    @Modifying(clearAutomatically = true)
    @Query("UPDATE ChallengeComment cc SET cc.isDeleted = true WHERE cc.challenge.id = :challengeId")
    void softDeleteAllByChallengeId(@Param("challengeId") Long challengeId);
}
