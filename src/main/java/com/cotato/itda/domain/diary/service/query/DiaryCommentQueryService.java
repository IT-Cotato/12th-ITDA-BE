package com.cotato.itda.domain.diary.service.query;

import com.cotato.itda.domain.diary.converter.DiaryCommentConverter;
import com.cotato.itda.domain.diary.dto.response.DiaryCommentListResponse;
import com.cotato.itda.domain.diary.dto.response.WriterInfo;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.entity.DiaryComment;
import com.cotato.itda.domain.diary.repository.DiaryCommentRepository;
import com.cotato.itda.domain.diary.repository.DiaryRepository;
import com.cotato.itda.domain.diary.validator.DiaryAccessValidator;
import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.diary.exception.code.DiaryErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryCommentQueryService {

    private final DiaryCommentRepository diaryCommentRepository;
    private final FriendshipRepository friendshipRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryAccessValidator diaryAccessValidator;

    public DiaryCommentListResponse getCommentList(Long diaryId, Long memberId, Long lastId, int size) {

        // 일기 조회
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_NOT_FOUND));

        // 일기 권한 검증
        diaryAccessValidator.validateDiaryAccess(memberId, diary);

        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<DiaryComment> commentSlice = diaryCommentRepository.findComments(diaryId, memberId, lastId, pageRequest);
        List<DiaryComment> rootComments = commentSlice.getContent();

        // 작성자의 friendship nickname Map 조회
        Map<Long, String> friendshipNicknameMap = getFriendNicknameMap(memberId, rootComments);

        List<DiaryCommentListResponse.CommentItem> commentItems = rootComments.stream()
                .map(root -> {
                    Member writer = root.getMember();
                    boolean isMe = writer.getId().equals(memberId);

                    // 대댓글 변환: 부모댓글이 가지고 있는 대댓글 리스트를 순회하며 DTO 리스트로 변환
                    List<DiaryCommentListResponse.CommentItem> childItems = root.getChildComments().stream()
                            .filter(child -> !child.getIsDeleted()) // 삭제된 댓글은 화면에서 숨김
                            .map(child -> {
                                boolean isChildMe = child.getMember().getId().equals(memberId);
                                WriterInfo childWriter = DiaryCommentConverter.toWriterInfo(child.getMember(), child.getMember().getName(), isChildMe);

                                // 대댓글은 하위에 자식이 없으므로 childComments 자리에 null을 넘김
                                return DiaryCommentConverter.toListItem(child, childWriter, null);
                            })
                            .toList();

                    // 부모 댓글이 삭제 상태인 경우
                    if (root.getIsDeleted()) {
                        if (childItems.isEmpty()) {
                            return null;
                        }
                        WriterInfo unknownWriter = WriterInfo.builder()
                                .memberId(null)
                                .nickname("(알 수 없음)")
                                .isMe(false)
                                .build();

                        return DiaryCommentConverter.toDeletedListItem(root, unknownWriter, childItems);
                    }

                    String nickname = isMe
                            ? writer.getName()
                            : friendshipNicknameMap.getOrDefault(writer.getId(), writer.getName());

                    WriterInfo writerInfo = DiaryCommentConverter.toWriterInfo(writer, nickname, isMe);
                    return DiaryCommentConverter.toListItem(root, writerInfo, childItems);
                })
                .filter(Objects::nonNull)
                .toList();

        // 커서 ID 계산
        Long newLastId = commentItems.isEmpty() ? null : commentItems.get(commentItems.size() - 1).commentId();

        return DiaryCommentConverter.toListResponse(commentItems, newLastId, commentSlice.hasNext());
    }

    private Map<Long, String> getFriendNicknameMap(Long memberId, List<DiaryComment> comments) {

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
