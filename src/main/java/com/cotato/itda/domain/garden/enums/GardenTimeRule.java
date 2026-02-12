package com.cotato.itda.domain.garden.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@RequiredArgsConstructor
public enum GardenTimeRule {
    WATERABLE(24),
    WITHERED(48),
    NUTRITION_AVAILABLE(72);

    private final int hours;

    public static boolean isWithered(LocalDateTime lastWateredAt) {
        if (lastWateredAt == null) return false;
        return ChronoUnit.HOURS.between(lastWateredAt, LocalDateTime.now()) >= WITHERED.getHours();
    }
}
