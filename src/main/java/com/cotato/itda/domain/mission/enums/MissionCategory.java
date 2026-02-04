package com.cotato.itda.domain.mission.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MissionCategory {
    FOOD("음식"),
    PLANT("식물"),
    COLOR("색상"),
    MOMENT("순간"),
    TV("TV");

    private final String description;
}
