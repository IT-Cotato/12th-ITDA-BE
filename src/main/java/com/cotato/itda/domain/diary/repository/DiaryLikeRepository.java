package com.cotato.itda.domain.diary.repository;

import com.cotato.itda.domain.diary.entity.DiaryLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DiaryLikeRepository extends JpaRepository<DiaryLike, Long> {

    @Query("SELECT COUNT(dl) > 0 FROM DiaryLike dl " +
           "WHERE dl.diary.id = :diaryId AND dl.member.id = :memberId" )
    boolean existsByDiaryIdAndMemberId(@Param("diaryId") Long diaryId, @Param("memberId") Long memberId);

    Optional<DiaryLike> findByDiaryIdAndMemberId(Long diaryId, Long memberId);

    @Query("SELECT dl.diary.id FROM DiaryLike dl " +
            "WHERE dl.diary.id IN :diaryIds AND dl.member.id = :memberId")
    List<Long> findLikedDiaryIdsByDiaryIdsAndMemberId(@Param("diaryIds") List<Long> diaryIds, @Param("memberId") Long memberId);
}
