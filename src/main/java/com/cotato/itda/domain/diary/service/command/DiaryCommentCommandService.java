package com.cotato.itda.domain.diary.service.command;

import com.cotato.itda.domain.diary.converter.DiaryCommentConverter;
import com.cotato.itda.domain.diary.dto.request.DiaryCommentRequest;
import com.cotato.itda.domain.diary.dto.response.DiaryCommentResponse;
import com.cotato.itda.domain.diary.dto.response.WriterInfo;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.entity.DiaryComment;
import com.cotato.itda.domain.diary.repository.DiaryCommentRepository;
import com.cotato.itda.domain.diary.repository.DiaryRepository;
import com.cotato.itda.domain.diary.validator.DiaryAccessValidator;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.notification.service.command.NotificationCommandService;
import com.cotato.itda.domain.diary.exception.code.DiaryErrorCode;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryCommentCommandService {

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryCommentRepository diaryCommentRepository;
    private final DiaryAccessValidator diaryAccessValidator;
    private final NotificationCommandService notificationCommandService;

    @Transactional
    public DiaryCommentResponse createComment(Long memberId, Long diaryId, DiaryCommentRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", memberId)));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_NOT_FOUND));

        // 일기 권한 검증
        diaryAccessValidator.validateDiaryAccess(memberId, diary);

        DiaryComment comment = DiaryCommentConverter.toEntity(diary, member, request);

        if (request.parentId() != null) {
            DiaryComment parent = diaryCommentRepository.findById(request.parentId())
                    .orElseThrow(() -> new BusinessException(DiaryErrorCode.PARENT_COMMENT_NOT_FOUND));
            comment.setParent(parent);
        }

        DiaryComment savedComment = diaryCommentRepository.save(comment);

        diary.increaseComment();
        notificationCommandService.createDiaryCommentNotification(member, diary);

        // 작성자 정보 생성
        WriterInfo writerInfo = DiaryCommentConverter.toWriterInfo(member, member.getName(), true);
        return DiaryCommentConverter.toResponse(savedComment, writerInfo);
    }

    @Transactional
    public void softDeleteComment(Long memberId, Long diaryId, Long commentId) {

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_NOT_FOUND));

        DiaryComment comment = diaryCommentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.COMMENT_NOT_FOUND));

        // 댓글이 해당 일기의 댓글인지 확인
        if (!comment.getDiary().getId().equals(diaryId)) {
            throw  new BusinessException(DiaryErrorCode.COMMENT_NOT_FOUND);
        }

        if (!comment.getMember().getId().equals(memberId)) {
            throw new BusinessException(DiaryErrorCode.COMMENT_FORBIDDEN);
        }

        comment.delete();
        diary.decreaseComment();
    }
}
