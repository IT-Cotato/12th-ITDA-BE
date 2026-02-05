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
        List<DiaryComment> comments = commentSlice.getContent();

        // 작성자의 friendship nickname Map 조회
        Map<Long, String> friendshipNicknameMap = getFriendNicknameMap(memberId, comments);

        List<DiaryCommentListResponse.CommentItem> commentItems = comments.stream()
                .map(comment -> {
                    Member writer = comment.getMember();
                    boolean isMe = writer.getId().equals(memberId);

                    String nickname = isMe
                            ? writer.getName()
                            : friendshipNicknameMap.getOrDefault(writer.getId(), writer.getName());

                    WriterInfo writerInfo = DiaryCommentConverter.toWriterInfo(writer, nickname, isMe);
                    return DiaryCommentConverter.toListItem(comment, writerInfo);
                })
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
