package com.cotato.itda.domain.diary.converter;

import com.cotato.itda.domain.diary.dto.request.DiaryRequest;
import com.cotato.itda.domain.diary.dto.response.*;
import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.repository.projection.MonthlyDiaryInfo;
import com.cotato.itda.domain.member.entity.Member;

import java.util.List;

public class DiaryConverter {

    public static Diary toEntity(DiaryRequest request, Member member) {
        return Diary.builder()
                .member(member)
                .date(request.date())
                .emojiCode(request.emojiCode())
                .content(request.content())
                .imageUrl(request.imageUrl())
                .build();
    }

    public static DiaryResponse toResponse(Diary diary) {
        return DiaryResponse.builder()
                .diaryId(diary.getId())
                .date(diary.getDate())
                .emojiCode(diary.getEmojiCode())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .likeCount(diary.getLikeCount())
                .commentCount(diary.getCommentCount())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }

    public static WriterInfo toWriterInfo(Member member, String nickname, boolean isMe) {
        return WriterInfo.builder()
                .memberId(member.getId())
                .nickname(nickname)
                .profileImageUrl(member.getProfileImageUrl())
                .isMe(isMe)
                .build();
    }

    public static DiaryDetailResponse toDetailResponse(Diary diary, WriterInfo writerInfo, boolean isLiked) {
        return DiaryDetailResponse.builder()
                .writerInfo(writerInfo)
                .diaryId(diary.getId())
                .date(diary.getDate())
                .emojiCode(diary.getEmojiCode())
                .emojiDescription(diary.getEmojiCode().getDescription())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .likeCount(diary.getLikeCount())
                .commentCount(diary.getCommentCount())
                .isLiked(isLiked)
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }

    public static DiaryListResponse.DiaryItem toListItem(Diary diary, WriterInfo writerInfo, boolean isLiked) {
        return DiaryListResponse.DiaryItem.builder()
                .writerInfo(writerInfo)
                .diaryId(diary.getId())
                .date(diary.getDate())
                .emojiCode(diary.getEmojiCode())
                .content(diary.getContent())
                .imageUrl(diary.getImageUrl())
                .likeCount(diary.getLikeCount())
                .commentCount(diary.getCommentCount())
                .isLiked(isLiked)
                .createdAt(diary.getCreatedAt())
                .build();
    }

    public static DiaryListResponse toListResponse(
            List<DiaryListResponse.DiaryItem> items,
            Long lastId,
            boolean hasNext
    ) {
        return DiaryListResponse.builder()
                .diaries(items)
                .lastId(lastId)
                .hasNext(hasNext)
                .build();
    }

    public static MonthlyDiaryListResponse.MonthlyDiaryItem toMonthlyListItem(MonthlyDiaryInfo diary) {
        return MonthlyDiaryListResponse.MonthlyDiaryItem.builder()
                .diaryId(diary.getId())
                .date(diary.getDate())
                .emojiCode(diary.getEmojiCode())
                .build();
    }

    public static MonthlyDiaryListResponse toMonthlyListResponse(
            WriterInfo writerInfo,
            Integer year,
            Integer month,
            List<MonthlyDiaryInfo> diaries
    ) {
        List<MonthlyDiaryListResponse.MonthlyDiaryItem> items = diaries.stream()
                .map(DiaryConverter::toMonthlyListItem)
                .toList();

        return MonthlyDiaryListResponse.builder()
                .writerInfo(writerInfo)
                .year(year)
                .month(month)
                .diaryCount(items.size())
                .diaries(items)
                .build();
    }

}
