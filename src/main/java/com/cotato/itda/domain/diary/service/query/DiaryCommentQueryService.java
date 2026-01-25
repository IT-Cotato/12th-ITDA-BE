package com.cotato.itda.domain.diary.service.query;

import com.cotato.itda.domain.diary.converter.DiaryCommentConverter;
import com.cotato.itda.domain.diary.dto.response.DiaryCommentListResponse;
import com.cotato.itda.domain.diary.dto.response.WriterInfo;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.entity.DiaryComment;
import com.cotato.itda.domain.diary.repository.DiaryCommentRepository;
import com.cotato.itda.domain.diary.repository.DiaryRepository;
import com.cotato.itda.domain.diary.validator.DiaryAccessValidator;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.global.error.constant.DiaryErrorCode;
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
        Map<Long, String> friendNicknameMap = getFriendNicknameMap(memberId, comments);

        List<DiaryCommentListResponse.CommentItem> commentItems = comments.stream()
                .map(comment -> {
                    Member writer = comment.getMember();

                    String nickname = determineNickname(writer, friendNicknameMap.get(writer.getId()));
                    boolean isMe = writer.getId().equals(memberId);

                    WriterInfo writerInfo = DiaryCommentConverter.toWriterInfo(writer, nickname, isMe);
                    return DiaryCommentConverter.toListItem(comment, writerInfo);
                })
                .toList();

        // 커서 ID 계산
        Long newLastId = commentItems.isEmpty() ? null : commentItems.get(commentItems.size() - 1).commentId();

        return DiaryCommentConverter.toListResponse(commentItems, newLastId, commentSlice.hasNext());
    }

    // 작성자들의 friendship nickname 일괄 조회
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

        // nickname 조회 후 Map으로 변환
        return friendshipRepository.findFriendNicknames(memberId, writerIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0], // friend.id
                        row -> (String) row [1] // nickname
                ));
    }

    private String determineNickname(Member writer, String friendshipNickname) {
        return friendshipNickname != null ? friendshipNickname : writer.getName();
    }
}
