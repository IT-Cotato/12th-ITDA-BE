package com.cotato.itda.domain.diary.repository.projection;

import com.cotato.itda.domain.diary.enums.EmojiCode;

import java.time.LocalDate;

public interface MonthlyDiaryInfo {
    Long getId();
    LocalDate getDate();
    EmojiCode getEmojiCode();
}
