package com.cotato.itda.domain.garden.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MaxGrowthCount {

    TEAM_GROW(4),
    TEAM_WATER(2),
    SOLO_WATER(1);

    public final int maxGrowthCount;
}
