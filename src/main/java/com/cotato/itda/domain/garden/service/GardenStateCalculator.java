package com.cotato.itda.domain.garden.service;

import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.entity.SharedPlantLog;
import com.cotato.itda.domain.garden.enums.GardenState;
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

        Optional<SharedPlantLog> lastLog = sharedPlantLogRepository.findTopBySharedPlantOrderByCreatedAtDesc(plant);
        if (lastLog.isEmpty()) return GardenState.SEED_READY;

        SharedPlantLog log = lastLog.get();
        long hours = ChronoUnit.HOURS.between(log.getWateredAt(), LocalDateTime.now());

        if (log.isUsedNutrient() && plant.getStatus() == SharedPlantStatus.WITHERED) {
            return GardenState.AFTER_NUTRITION;
        }

        if (hours >= 72) return GardenState.NUTRITION_AVAILABLE;
        if (hours >= 48) return GardenState.WITHERED;
        if (log.isStageChanged()) return GardenState.GROWING;
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
