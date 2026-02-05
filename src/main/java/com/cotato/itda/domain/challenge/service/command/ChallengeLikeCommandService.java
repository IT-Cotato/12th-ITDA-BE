package com.cotato.itda.domain.challenge.service.command;

import com.cotato.itda.domain.challenge.dto.response.ChallengeLikeResponse;
import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.entity.ChallengeLike;
import com.cotato.itda.domain.challenge.repository.ChallengeLikeRepository;
import com.cotato.itda.domain.challenge.repository.ChallengeRepository;
import com.cotato.itda.domain.challenge.service.validator.ChallengeAccessValidator;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.challenge.exception.code.ChallengeErrorCode;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeLikeCommandService {

    private final MemberRepository memberRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeLikeRepository challengeLikeRepository;
    private final ChallengeAccessValidator challengeAccessValidator;

    @Transactional
    public ChallengeLikeResponse addLike(Long memberId, Long challengeId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("memberId", memberId)));

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

        challengeAccessValidator.validateChallengeAccess(memberId, challenge);

        if (challengeLikeRepository.existsByChallengeAndMember(challenge, member)) {
            throw new BusinessException(ChallengeErrorCode.LIKE_ALREADY_EXISTS);
        }

        ChallengeLike like = ChallengeLike.builder()
                .challenge(challenge)
                .member(member)
                .build();

        challengeLikeRepository.save(like);
        challenge.increaseLike();

        return new ChallengeLikeResponse(challenge.getLikeCount());
    }

    @Transactional
    public ChallengeLikeResponse deleteLike(Long memberId, Long challengeId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("memberId", memberId)));

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

        ChallengeLike like = challengeLikeRepository.findByChallengeAndMember(challenge, member)
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.LIKE_NOT_FOUND));

        challengeLikeRepository.delete(like);
        challenge.decreaseLike();

        return new ChallengeLikeResponse(challenge.getLikeCount());
    }
}
