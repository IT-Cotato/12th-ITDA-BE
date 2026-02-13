package com.cotato.itda.domain.garden.service;

import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantLog;
import com.cotato.itda.domain.garden.enums.GardenState;
import com.cotato.itda.domain.garden.enums.GardenTimeRule;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.domain.garden.repository.SharedPlantLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GardenStateCalculator {

    private final SharedPlantLogRepository sharedPlantLogRepository;

    public GardenState calculateState(SharedPlant plant) {
        if (plant.getStatus() == SharedPlantStatus.COMPLETED) return GardenState.COMPLETED;
        if (!plant.isPlanted()) return GardenState.SEED_READY;
        if (plant.getLastWateredAt() == null) return GardenState.WATERABLE;

        long hours = ChronoUnit.HOURS.between(plant.getLastWateredAt(), LocalDateTime.now());

        Optional<SharedPlantLog> lastLog = sharedPlantLogRepository.findTopBySharedPlantOrderByCreatedAtDesc(plant);
        if (lastLog.isPresent()) {
            SharedPlantLog log = lastLog.get();
            if (log.isUsedNutrient() && GardenTimeRule.isWithered(plant.getLastWateredAt())) {
                return GardenState.AFTER_NUTRITION;
            }
            if (log.isStageChanged() && hours <= 24) return GardenState.GROWING;
        }

        if (hours >= 72) return GardenState.NUTRITION_AVAILABLE;
        if (hours >= 48) return GardenState.WITHERED;
        if (hours <= 24) return GardenState.WATERED_RECENTLY;
        return GardenState.WATERABLE;
    }

    public int calculatePercentage(SharedPlant plant) {
        int bloomMax = plant.getPlant().getBloomMax();
        if (bloomMax <= 0) return 0;

        int percentage = (plant.getGrowthValue() * 100) / bloomMax;
        return Math.min(percentage, 100);
    }
}
