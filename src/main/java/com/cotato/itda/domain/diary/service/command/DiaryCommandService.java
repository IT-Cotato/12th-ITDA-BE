package com.cotato.itda.domain.diary.service.command;

import com.cotato.itda.domain.diary.converter.DiaryConverter;
import com.cotato.itda.domain.diary.dto.request.DiaryRequest;
import com.cotato.itda.domain.diary.dto.response.DiaryResponse;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.repository.DiaryRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.DiaryErrorCode;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryCommandService {

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;

    @Transactional
    public DiaryResponse createDiary(Long memberId, DiaryRequest request) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", memberId)));

        // 해당 날짜 일기 중복 작성 방지
        if (diaryRepository.existsByMemberIdAndDate(memberId, request.date())) {
            throw new BusinessException(DiaryErrorCode.DIARY_ALREADY_EXISTS);
        }

        Diary diary = DiaryConverter.toEntity(request, member);
        diaryRepository.save(diary);

        return DiaryConverter.toResponse(diary);
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

        diary.delete();
    }

}
