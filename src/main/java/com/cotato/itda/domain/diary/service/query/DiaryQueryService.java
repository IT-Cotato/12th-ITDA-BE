package com.cotato.itda.domain.diary.service.query;

import com.cotato.itda.domain.diary.converter.DiaryConverter;
import com.cotato.itda.domain.diary.dto.response.DiaryDetailResponse;
import com.cotato.itda.domain.diary.dto.response.DiaryListResponse;
import com.cotato.itda.domain.diary.dto.response.WriterInfo;
import com.cotato.itda.domain.diary.dto.response.MonthlyDiaryListResponse;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.repository.DiaryLikeRepository;
import com.cotato.itda.domain.diary.repository.DiaryRepository;
import com.cotato.itda.domain.diary.repository.projection.MonthlyDiaryInfo;
import com.cotato.itda.domain.friendship.entity.Friendship;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.repository.FriendshipRepository;
import com.cotato.itda.domain.member.entity.Member;
import com.cotato.itda.domain.member.repository.MemberRepository;
import com.cotato.itda.global.error.constant.DiaryErrorCode;
import com.cotato.itda.global.error.constant.UserErrorCode;
import com.cotato.itda.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryQueryService {

    private final FriendshipRepository friendshipRepository;
    private final MemberRepository memberRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryLikeRepository diaryLikeRepository;

    /**
     * 일기 상세 조회
     */
    public DiaryDetailResponse getDiaryDetail(Long memberId, Long diaryId) {

        // 일기 조회
        Diary diary = diaryRepository.findByIdWithMember(diaryId)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_NOT_FOUND));

        Member writer = diary.getMember();
        boolean isMe = writer.getId().equals(memberId);
        String nickname;

        if (isMe) {
            nickname = writer.getName();
        } else {
            // 작성자가 친구 관계인지 확인
            Friendship friendship = friendshipRepository.findByMember_IdAndFriend_IdAndStatus(memberId, writer.getId(), FriendshipStatus.ACTIVE)
                    .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_FORBIDDEN));

            nickname = friendship.getDisplayName();
        }

        // 좋아요 여부 판별
        boolean isLiked = diaryLikeRepository.existsByDiaryIdAndMemberId(diaryId, memberId);

        WriterInfo diaryWriterInfo = DiaryConverter.toWriterInfo(writer, nickname, isMe);
        return DiaryConverter.toDetailResponse(diary, diaryWriterInfo, isLiked);
    }

    /**
     * 일기 목록 조회
     */
    public DiaryListResponse getDiaryList(Long memberId, Long lastId, int size) {

        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Diary> diarySlice = diaryRepository.findDiariesByMemberOrFriends(memberId, lastId, pageRequest);
        List<Diary> diaries = diarySlice.getContent();

        // 작성자의 Friendship nickname 조회
        Map<Long, String> friendshipNicknameMap = getFriendNicknameMap(memberId, diaries);

        // 일기 좋아요 일괄 조회
        List<Long> diaryIds = diaries.stream().map(Diary::getId).toList();

        Set<Long> likedDiaryIds = diaryIds.isEmpty()
                ? Set.of()
                : new HashSet<>(diaryLikeRepository.findLikedDiaryIdsByDiaryIdsAndMemberId(diaryIds, memberId));

        // Entity -> DTO 변환
        List<DiaryListResponse.DiaryItem> diaryItems = diaries.stream()
                .map(diary -> {
                    Member writer = diary.getMember();
                    boolean isMe = writer.getId().equals(memberId);

                    // 작성자 nickname 결정
                    String nickname = isMe ? writer.getName() : friendshipNicknameMap.get(writer.getId());

                    boolean isLiked = likedDiaryIds.contains(diary.getId());

                    WriterInfo writerInfo = DiaryConverter.toWriterInfo(writer, nickname, isMe);
                    return DiaryConverter.toListItem(diary, writerInfo, isLiked);
                })
                .toList();

        // 커서 ID 계산
        Long newLastId = diaryItems.isEmpty() ? null : diaryItems.get(diaryItems.size() - 1).diaryId();

        return DiaryConverter.toListResponse(diaryItems, newLastId, diarySlice.hasNext());
    }

    /**
     * 내 월별 일기 목록 조회
     */
    public MonthlyDiaryListResponse getMonthlyDiaryList(Long memberId, int year, int month) {

        // 해당 월의 시작일(1일)과 마지막 날(말일) 계산
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        // 해당 월에 작성된 일기 목록 조회
        List<MonthlyDiaryInfo> diaries = diaryRepository.findMonthlyDiaries(memberId, start, end);

        return DiaryConverter.toMonthlyListResponse(null, year, month, diaries);
    }

    /**
     * 친구 월별 일기 목록 조회
     */
    public MonthlyDiaryListResponse getFriendMonthlyDiaryList(Long memberId, Long targetMemberId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        Member writer = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", targetMemberId)));

        // 친구 관계 조회
        Friendship friendship = friendshipRepository.findByMember_IdAndFriend_IdAndStatus(memberId, targetMemberId, FriendshipStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_FORBIDDEN));

        // 작성자 정보 변환
        WriterInfo writerInfo = DiaryConverter.toWriterInfo(writer, friendship.getDisplayName(), false);

        // 해당 월의 일기 목록 조회
        List<MonthlyDiaryInfo> diaries = diaryRepository.findMonthlyDiaries(targetMemberId, start, end);

        return DiaryConverter.toMonthlyListResponse(writerInfo, year, month, diaries);
    }

    /**
     *  작성자들의 nickname 일괄 조회
      */
    private Map<Long, String> getFriendNicknameMap(Long memberId, List<Diary> diaries) {

        // 작성자 ID 리스트 추출
        List<Long> writerIds = diaries.stream()
                .map(diary -> diary.getMember().getId())
                .filter(writerId -> !writerId.equals(memberId)) // 본인 제외
                .distinct()
                .toList();

        if (writerIds.isEmpty()) {
            return Map.of();
        }

        // Friendship 조회 후 map 변환
        return friendshipRepository.findActiveFriendships(memberId, writerIds)
                .stream()
                .collect(Collectors.toMap(
                        f -> f.getFriend().getId(),
                        Friendship::getDisplayName
                ));
    }

}
