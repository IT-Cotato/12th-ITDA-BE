package com.cotato.itda.domain.garden.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GrowthType {
    NONE(0),
    WATER(5),
    NUTRIENT(5);

    private final int growth;
}
