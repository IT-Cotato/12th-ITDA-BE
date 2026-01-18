package com.cotato.itda.domain.diary.repository;

import com.cotato.itda.domain.diary.entity.DiaryComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryCommentRepository extends JpaRepository<DiaryComment, Long> {
}
