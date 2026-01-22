package com.cotato.itda.domain.challenge.service.validator;

import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.global.error.constant.ChallengeErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeAccessValidator {

    private final FriendshipRepository friendshipRepository;

    // 챌린지 접근 권한 검증 (본인 또는 친구)
    public void validateChallengeAccess(Long memberId, Challenge challenge) {

        Long writerId = challenge.getMember().getId();
        if (memberId.equals(writerId)) {
            return;
        }
        boolean isFriend = friendshipRepository.existsByMemberIdAndFriendIdAndStatus(memberId, writerId, FriendshipStatus.ACTIVE);

        if (!isFriend) {
            throw new BusinessException(ChallengeErrorCode.CHALLENGE_FORBIDDEN);
        }
    }
}
