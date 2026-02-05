package com.cotato.itda.domain.challenge.service.command;

import com.cotato.itda.domain.challenge.converter.ChallengeCommentConverter;
import com.cotato.itda.domain.challenge.dto.request.ChallengeCommentRequest;
import com.cotato.itda.domain.challenge.dto.response.ChallengeCommentResponse;
import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.entity.ChallengeComment;
import com.cotato.itda.domain.challenge.repository.ChallengeCommentRepository;
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
public class ChallengeCommentCommandService {

    private final MemberRepository memberRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeCommentRepository challengeCommentRepository;
    private final ChallengeAccessValidator challengeAccessValidator;

    @Transactional
    public ChallengeCommentResponse createComment(Long memberId, Long challengeId, ChallengeCommentRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("memberId", memberId)));

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

        // 챌린지 권한 검증
        challengeAccessValidator.validateChallengeAccess(memberId, challenge);

        // 댓글 생성
        ChallengeComment comment = ChallengeCommentConverter.toEntity(challenge, member, request);
        ChallengeComment savedComment = challengeCommentRepository.save(comment);

        challenge.increaseComment();

        return ChallengeCommentConverter.toResponse(savedComment, member);
    }

    @Transactional
    public void softDeleteComment(Long memberId, Long challengeId, Long commentId) {

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

        ChallengeComment comment = challengeCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getChallenge().getId().equals(challengeId)) {
            throw new BusinessException(ChallengeErrorCode.COMMENT_NOT_FOUND);
        }

        if (!comment.getMember().getId().equals(memberId)) {
            throw new BusinessException(ChallengeErrorCode.COMMENT_FORBIDDEN);
        }

        comment.delete();
        challenge.decreaseComment();
    }
}
