package com.cotato.itda.domain.challenge.repository;

import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.entity.ChallengeView;
import com.cotato.itda.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface ChallengeViewRepository extends JpaRepository<ChallengeView, Long> {

    boolean existsByChallengeAndMember(Challenge challenge, Member member);

    // 사용자가 조회한 챌린지 ID 목록 반환
    @Query("SELECT cv.challenge.id FROM ChallengeView cv " +
            "WHERE cv.challenge.id IN :challengeIds " +
            "AND cv.member.id = :memberId")
    List<Long> findViewedChallengeIds(@Param("challengeIds") List<Long> challengeIds, @Param("memberId") Long memberId);
}
