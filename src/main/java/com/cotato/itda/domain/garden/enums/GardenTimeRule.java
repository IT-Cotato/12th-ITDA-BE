package com.cotato.itda.domain.garden.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GardenTimeRule {
    WATERABLE(24),
    WITHERED(48),
    NUTRITION_AVAILABLE(72);

    private final int hours;
}
