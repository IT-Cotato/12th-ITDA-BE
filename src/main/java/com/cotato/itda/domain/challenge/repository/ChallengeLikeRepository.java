package com.cotato.itda.domain.challenge.repository;

import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.entity.ChallengeLike;
import com.cotato.itda.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChallengeLikeRepository extends JpaRepository<ChallengeLike, Long> {

    boolean existsByChallengeAndMember(Challenge challenge, Member member);

    Optional<ChallengeLike> findByChallengeAndMember(Challenge challenge, Member member);
}
