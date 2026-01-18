package com.cotato.itda.domain.diary.service.query;

import com.cotato.itda.domain.diary.converter.DiaryConverter;
import com.cotato.itda.domain.diary.dto.response.DiaryDetailResponse;
import com.cotato.itda.domain.diary.dto.response.DiaryListResponse;
import com.cotato.itda.domain.diary.dto.response.DiaryWriterInfo;
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

    public DiaryDetailResponse getDiaryDetail(Long memberId, Long diaryId) {

        Diary diary = diaryRepository.findByIdWithMember(diaryId)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_NOT_FOUND));

        Member writer = diary.getMember();
        boolean isMe = writer.getId().equals(memberId);
        Friendship friendship = null;

        // 임시 구현: 권한 확인 후 표시할 이름 결정
        if(!isMe) {
            // 작성자가 친구 관계인지 확인
            friendship = friendshipRepository.findByMemberIdAndFriendIdAndStatus(memberId, writer.getId(), FriendshipStatus.ACTIVE)
                    .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_FORBIDDEN));
        }
        String nickname = determineNickname(writer, friendship);

        boolean isLiked = diaryLikeRepository.existsByDiaryIdAndMemberId(diaryId, memberId);

        DiaryWriterInfo diaryWriterInfo = DiaryConverter.toWriterInfo(writer, nickname, isMe);
        return DiaryConverter.toDetailResponse(diary, diaryWriterInfo, isLiked);
    }

    public DiaryListResponse getDiaryList(Long memberId, Long lastId, int size) {

        PageRequest pageRequest = PageRequest.of(0, size);
        Slice<Diary> diarySlice = diaryRepository.findDiariesByMemberOrFriends(memberId, lastId, pageRequest);
        List<Diary> diaries = diarySlice.getContent();

        // 작성자 중 현재 멤버의 friendship 정보 조회하여 Map으로 변환
        Map<Long, Friendship> friendshipMap = getFriendshipMap(memberId, diaries);

        // DiaryLike 일괄 조회
        List<Long> diaryIds = diaries.stream().map(Diary::getId).toList();
        Set<Long> likedDiaryIds;
        if (diaryIds.isEmpty()) {
            likedDiaryIds = Set.of();
        } else {
            likedDiaryIds = new HashSet<>(diaryLikeRepository.findLikedDiaryIdsByDiaryIdsAndMemberId(diaryIds, memberId));
        }

        // Entity -> DTO 변환
        List<DiaryListResponse.DiaryItem> diaryItems = diaries.stream()
                .map(diary -> {
                    Member writer = diary.getMember();
                    boolean isMe = writer.getId().equals(memberId);

                    // friendship 확인
                    Friendship friendship = isMe ? null : friendshipMap.get(writer.getId());
                    // 닉네임 결정
                    String nickname = determineNickname(writer, friendship);

                    boolean isLiked = likedDiaryIds.contains(diary.getId());

                    DiaryWriterInfo writerInfo = DiaryConverter.toWriterInfo(writer, nickname, isMe);
                    return DiaryConverter.toListItem(diary, writerInfo, isLiked);
                })
                .toList();

        // 커서 ID 계산
        Long newLastId = diaryItems.isEmpty() ? null : diaryItems.get(diaryItems.size() - 1).diaryId();

        return DiaryConverter.toListResponse(diaryItems, newLastId, diarySlice.hasNext());
    }

    // 내 월별 일기 목록 조회
    public MonthlyDiaryListResponse getMonthlyDiaryList(Long memberId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<MonthlyDiaryInfo> diaries = diaryRepository.findMonthlyDiaries(memberId, start, end);

        return DiaryConverter.toMonthlyListResponse(null, year, month, diaries);
    }

    // 친구 월별 일기 목록 조회
    public MonthlyDiaryListResponse getFriendMonthlyDiaryList(Long memberId, Long targetMemberId, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        Member member = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND, Map.of("userId", targetMemberId)));

        Friendship friendship = friendshipRepository.findByMemberIdAndFriendIdAndStatus(memberId, targetMemberId, FriendshipStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(DiaryErrorCode.DIARY_FORBIDDEN));

        String nickname = determineNickname(member, friendship);

        DiaryWriterInfo writerInfo = DiaryConverter.toWriterInfo(member, nickname, false);
        List<MonthlyDiaryInfo> diaries = diaryRepository.findMonthlyDiaries(targetMemberId, start, end);
        return DiaryConverter.toMonthlyListResponse(writerInfo, year, month, diaries);
    }

    // 작성자에 대한 friendship 조회 -> 수정 필요 (현재는 friendship 엔티티 전체 조회)
    private Map<Long, Friendship> getFriendshipMap(Long memberId, List<Diary> diaries) {

        // diary의 memberId 리스트 추출
        List<Long> writerIds = diaries.stream()
                .map(diary -> diary.getMember().getId())
                .filter(writerId -> !writerId.equals(memberId)) // 본인 제외
                .distinct()
                .toList();

        if (writerIds.isEmpty()) {
            return Map.of();
        }

        // friendship 조회
        return friendshipRepository
                .findAllByMemberIdAndFriendIdInAndStatus(memberId, writerIds, FriendshipStatus.ACTIVE)
                .stream()
                .collect(Collectors.toMap(f -> f.getFriend().getId(), f -> f));
    }

    // 작성자 닉네임 결정
    private String determineNickname(Member writer, Friendship friendship) {

        // friendship이 존재하며, nickname이 null 아닌 경우 해당 nickname 사용
        if (friendship != null && friendship.getNickname() != null) {
            return friendship.getNickname();
        }

        // 그 외의 경우 작성자의 profileName 사용
        return writer.getProfileName();
    }

}
