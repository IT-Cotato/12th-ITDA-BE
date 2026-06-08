package com.cotato.itda.domain.challenge.service.query;

import com.cotato.itda.domain.challenge.converter.ChallengeCommentConverter;
import com.cotato.itda.domain.challenge.dto.response.ChallengeCommentListResponse;
import com.cotato.itda.domain.challenge.entity.Challenge;
import com.cotato.itda.domain.challenge.entity.ChallengeComment;
import com.cotato.itda.domain.challenge.repository.ChallengeCommentRepository;
import com.cotato.itda.domain.challenge.repository.ChallengeRepository;
import com.cotato.itda.domain.challenge.service.validator.ChallengeAccessValidator;
import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.challenge.exception.code.ChallengeErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeCommentQueryService {

    private final ChallengeRepository challengeRepository;
    private final ChallengeCommentRepository challengeCommentRepository;
    private final FriendshipRepository friendshipRepository;
    private final ChallengeAccessValidator challengeAccessValidator;

    public ChallengeCommentListResponse getCommentList(Long memberId, Long challengeId, Long lastId, int size) {

        // 챌린지 조회
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ChallengeErrorCode.CHALLENGE_NOT_FOUND));

        // 챌린지 권한 검증
        challengeAccessValidator.validateChallengeAccess(memberId, challenge);

        // 1. 댓글 조회
        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<ChallengeComment> commentSlice = challengeCommentRepository.findComments(challengeId, memberId, lastId, pageRequest);
        List<ChallengeComment> rootComments = commentSlice.getContent();

        // 2. 작성자 nickname Map 생성
        Map<Long, String> nicknameMap = getFriendNicknameMap(memberId, rootComments);

        List<ChallengeCommentListResponse.CommentItem> commentItems = rootComments.stream()
                .map(root -> {
                    Member writer = root.getMember();
                    boolean isMe = writer.getId().equals(memberId);

                    // 대댓글 변환: 부모댓글이 가지고 있는 대댓글 리스트를 순회하며 DTO 리스트로 변환
                    List<ChallengeCommentListResponse.CommentItem> childItems = root.getChildComments().stream()
                            .filter(child -> !child.getIsDeleted()) // 삭제된 댓글은 화면에서 숨김
                            .map(child -> {
                                boolean isChildMe = child.getMember().getId().equals(memberId);
                                ChallengeCommentListResponse.WriterInfo childWriter = ChallengeCommentConverter.toListWriterInfo(child.getMember(), child.getMember().getName());

                                // 대댓글은 하위에 자식이 없으므로 childComments 자리에 null을 넘김
                                return ChallengeCommentConverter.toListItem(child, childWriter, null);
                            })
                            .toList();

                    // 부모 댓글이 삭제 상태인 경우
                    if (root.getIsDeleted()) {
                        if (childItems.isEmpty()) {
                            return null;
                        }
                        ChallengeCommentListResponse.WriterInfo unknownWriter =
                                ChallengeCommentConverter.toListWriterInfo(null, "(알 수 없음)");

                        return ChallengeCommentConverter.toDeletedListItem(root, unknownWriter, childItems);
                    }

                    String nickname = isMe
                            ? writer.getName()
                            : nicknameMap.getOrDefault(writer.getId(), writer.getName());

                    ChallengeCommentListResponse.WriterInfo writerInfo = ChallengeCommentConverter.toListWriterInfo(writer, nickname);
                    return ChallengeCommentConverter.toListItem(root, writerInfo, childItems);
                })
                .filter(Objects::nonNull)
                .toList();

        // 4. 커서 ID 계산
        Long newLastId = commentItems.isEmpty() ? null : commentItems.get(commentItems.size() - 1).commentId();

        return ChallengeCommentConverter.toListResponse(commentItems, newLastId, commentSlice.hasNext());
    }

    private Map<Long, String> getFriendNicknameMap(Long memberId, List<ChallengeComment> comments) {

        // 작성자 ID 리스트 추출
        List<Long> writerIds = comments.stream()
                .map(comment -> comment.getMember().getId())
                .filter(writerId -> !writerId.equals(memberId))
                .distinct()
                .toList();

        if (writerIds.isEmpty()) {
            return Map.of();
        }

        return friendshipRepository.findActiveFriendships(memberId, writerIds)
                .stream()
                .collect(Collectors.toMap(
                        f -> f.getFriend().getId(),
                        Friendship::getDisplayName
                ));
    }
}
