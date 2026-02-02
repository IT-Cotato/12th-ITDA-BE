package com.cotato.itda.domain.diary.validator;

import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.global.error.constant.DiaryErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiaryAccessValidator {

    private final FriendshipRepository friendshipRepository;

    // 일기 접근 권한 검증 (본인 또는 친구)
    public void validateDiaryAccess(Long memberId, Diary diary) {

        boolean isMyDiary = memberId.equals(diary.getMember().getId());
        boolean isFriend = friendshipRepository.existsByMemberIdAndFriendIdAndStatus(memberId, diary.getMember().getId(), FriendshipStatus.ACTIVE);

        if (!isMyDiary && !isFriend) {
            throw new BusinessException(DiaryErrorCode.DIARY_FORBIDDEN);
        }
    }
}
