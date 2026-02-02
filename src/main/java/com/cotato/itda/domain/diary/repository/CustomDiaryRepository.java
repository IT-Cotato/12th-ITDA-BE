package com.cotato.itda.domain.diary.repository;

import com.cotato.itda.domain.diary.entity.Diary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

public interface CustomDiaryRepository {

    Slice<Diary> findDiariesByMemberOrFriends(Long memberId, Long lastId, PageRequest pageRequest);
}
