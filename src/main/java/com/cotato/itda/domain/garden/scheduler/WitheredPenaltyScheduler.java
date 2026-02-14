package com.cotato.itda.domain.garden.scheduler;

import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.domain.garden.repository.SharedPlantRepository;
import com.cotato.itda.domain.garden.service.GardenStateCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class WitheredPenaltyScheduler {

    private final SharedPlantRepository sharedPlantRepository;
    private final GardenStateCalculator gardenStateCalculator;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void applyWitheredPenalty() {
        List<SharedPlant> plants = sharedPlantRepository
                .findAllByStatusAndIsPlantedTrue(SharedPlantStatus.GROWING);

        for (SharedPlant plant : plants) {
            int expectedPenalty = gardenStateCalculator.calculateExpectedPenalty(plant);
            int diff = expectedPenalty - plant.getAppliedPenalty();

            if (diff > 0) {
                plant.applyPenalty(diff);
            }
        }
    }
}
