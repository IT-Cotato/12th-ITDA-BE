package com.cotato.itda.domain.diary.repository;

import com.cotato.itda.domain.diary.entity.DiaryComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long>, CustomDiaryCommentRepository {
    @Modifying
    @Query("UPDATE DiaryComment dc SET dc.isDeleted = true, dc.deletedAt = :now WHERE dc.diary.id = :diaryId")
    void softDeleteAllByDiaryId(@Param("diaryId") Long diaryId, @Param("now") LocalDateTime now);
}
