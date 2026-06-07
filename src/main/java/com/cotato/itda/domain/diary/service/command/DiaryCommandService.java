package com.cotato.itda.domain.diary.service.command;

import com.cotato.itda.domain.diary.converter.DiaryConverter;
import com.cotato.itda.domain.diary.dto.request.DiaryRequest;
import com.cotato.itda.domain.diary.dto.response.DiaryResponse;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.repository.DiaryCommentRepository;
import com.cotato.itda.domain.diary.repository.DiaryRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.domain.diary.exception.code.DiaryErrorCode;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryCommandService {

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryCommentRepository diaryCommentRepository;

    @Transactional
    public DiaryResponse createDiary(Long memberId, DiaryRequest request, LocalDate date) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", memberId)));

        Diary diary = DiaryConverter.toEntity(request, date, member);
        Diary savedDiary = diaryRepository.save(diary);

        member.addPoints(1);

        return DiaryConverter.toResponse(savedDiary);
    }

    @Transactional
    public DiaryResponse updateDiary(Long memberId, Long diaryId, DiaryRequest request) {

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_NOT_FOUND));

        // 권한 확인
        if (!diary.getMember().getId().equals(memberId)) {
            throw new BusinessException(DiaryErrorCode.DIARY_FORBIDDEN);
        }

        // 날짜 변경하는 경우 일기 중복 확인
        if (!diary.getDate().equals(request.date())) {
            if (diaryRepository.existsByMemberIdAndDate(memberId, request.date())) {
                throw new BusinessException(DiaryErrorCode.DIARY_ALREADY_EXISTS);
            }
        }

        diary.update(request.date(), request.emojiCode(), request.content(), request.imageUrl());
        return DiaryConverter.toResponse(diary);
    }

    @Transactional
    public void softDeleteDiary(Long memberId, Long diaryId) {

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_NOT_FOUND));

        if (!diary.getMember().getId().equals(memberId)) {
            throw new BusinessException(DiaryErrorCode.DIARY_FORBIDDEN);
        }

        // 해당 일기의 댓글 일괄 soft delete 처리
        diaryCommentRepository.softDeleteAllByDiaryId(diaryId, LocalDateTime.now());
        diary.delete();
    }

}
