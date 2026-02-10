package com.cotato.itda.domain.garden.service;

import com.cotato.itda.domain.garden.converter.SharedPlantConverter;
import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.enums.GardenState;
import com.cotato.itda.domain.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlantActionResponseBuilder {

    private final GardenStateCalculator gardenStateCalculator;

    public SharedPlantResDTO.PlantActionResDTO build(SharedPlant plant, Member member) {
        GardenState gardenState = gardenStateCalculator.calculate(plant);
        int percentage = calculatePercentage(plant);

        return SharedPlantConverter.toPlantActionResDTO(plant, member, gardenState, percentage);
    }

    private int calculatePercentage(SharedPlant plant) {
        int bloomMax = plant.getPlant().getBloomMax();
        if (bloomMax <= 0) return 0;

        int percentage = (plant.getGrowthValue() * 100) / bloomMax;
        return Math.min(percentage, 100);
    }
}
