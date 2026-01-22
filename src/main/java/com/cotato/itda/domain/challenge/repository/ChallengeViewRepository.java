package com.cotato.itda.domain.challenge.repository;

import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.entity.ChallengeView;
import com.cotato.itda.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ChallengeViewRepository extends JpaRepository<ChallengeView, Long> {

    boolean existsByChallengeAndMember(Challenge challenge, Member member);
}
