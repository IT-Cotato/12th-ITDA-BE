package com.cotato.itda.domain.diary.repository;

import com.cotato.itda.domain.diary.entity.DiaryComment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;


public interface CustomDiaryCommentRepository {

    Slice<DiaryComment> findComments(Long diaryId, Long memberId, Long lastId, PageRequest pageRequest);

}
