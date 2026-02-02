package com.cotato.itda.domain.diary.repository;

import com.cotato.itda.domain.diary.entity.Diary;
import com.cotato.itda.domain.diary.repository.projection.MonthlyDiaryInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Long>, CustomDiaryRepository {

    boolean existsByMemberIdAndDate(Long memberId, LocalDate date);

    @Query("SELECT d.id as id, d.date as date, d.emojiCode as emojiCode " +
            "FROM Diary d " +
            "WHERE d.member.id = :memberId " +
            "AND d.date BETWEEN :start AND :end " +
            "ORDER BY d.date ASC")
    List<MonthlyDiaryInfo> findMonthlyDiaries(@Param("memberId") Long memberId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT d FROM Diary d JOIN FETCH d.member WHERE d.id = :diaryId")
    Optional<Diary> findByIdWithMember(@Param("diaryId") Long diaryId);

}
