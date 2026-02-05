package com.cotato.itda.domain.diary.service.command;

import com.cotato.itda.domain.diary.dto.response.DiaryLikeResponse;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.entity.DiaryLike;
import com.cotato.itda.domain.diary.repository.DiaryLikeRepository;
import com.cotato.itda.domain.diary.repository.DiaryRepository;
import com.cotato.itda.domain.diary.validator.DiaryAccessValidator;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
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
public class DiaryLikeCommandService {

    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryLikeRepository diaryLikeRepository;
    private final DiaryAccessValidator diaryAccessValidator;

    @Transactional
    public DiaryLikeResponse addLike(Long memberId, Long diaryId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", memberId)));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_NOT_FOUND));

        // 일기 권한 검증
        diaryAccessValidator.validateDiaryAccess(memberId, diary);

        if (diaryLikeRepository.existsByDiaryIdAndMemberId(diaryId, memberId)) {
            throw new BusinessException(DiaryErrorCode.LIKE_ALREADY_EXISTS);
        }

        DiaryLike like = DiaryLike.builder()
                .diary(diary)
                .member(member)
                .build();

        diaryLikeRepository.save(like);
        diary.increaseLike();

        return new DiaryLikeResponse(diary.getLikeCount());
    }

    @Transactional
    public DiaryLikeResponse deleteLike(Long memberId, Long diaryId){

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_NOT_FOUND));

        DiaryLike like = diaryLikeRepository.findByDiaryIdAndMemberId(diaryId, memberId)
                        .orElseThrow(() -> new BusinessException(DiaryErrorCode.LIKE_NOT_FOUND));

        diaryLikeRepository.delete(like);
        diary.decreaseLike();

        return new DiaryLikeResponse(diary.getLikeCount());
    }
}
