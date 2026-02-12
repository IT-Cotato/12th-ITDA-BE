package com.cotato.itda.domain.garden.converter;

import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantLog;

public class SharedPlantLogConverter {

    public static SharedPlantLog toSharedPlantLog(
            SharedPlant sharedPlant,
            Long wateredBy,
            boolean affectedGrowth,
            int growthIncrement,
            boolean usedNutrient,
            boolean stageChanged
    ) {
        return SharedPlantLog.builder()
                .sharedPlant(sharedPlant)
                .wateredBy(wateredBy)
                .affectedGrowth(affectedGrowth)
                .growthIncrement(growthIncrement)
                .usedNutrient(usedNutrient)
                .stageChanged(stageChanged)
                .build();
    }
}
