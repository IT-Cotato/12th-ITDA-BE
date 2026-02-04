package com.cotato.itda.domain.garden.service.command.strategy;

import com.cotato.itda.domain.garden.dto.req.SharedPlantReqDTO;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.enums.GrowthType;
import com.cotato.itda.domain.garden.enums.SupplyType;
import com.cotato.itda.domain.garden.exception.SharedPlantException;
import com.cotato.itda.domain.garden.exception.code.SharedPlantErrorCode;
import com.cotato.itda.domain.garden.repository.SharedPlantLogRepository;
import com.cotato.itda.domain.member.entity.Member;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class NutrientWateringStrategy implements WateringStrategy {

    private final SharedPlantLogRepository sharedPlantLogRepository;

    public NutrientWateringStrategy(SharedPlantLogRepository sharedPlantLogRepository) {
        this.sharedPlantLogRepository = sharedPlantLogRepository;
    }

    @Override
    public boolean supports(SharedPlant sharedPlant, SharedPlantReqDTO.WaterPlantReqDTO dto) {
        return dto.supplyType() == SupplyType.NUTRIENT;
    }

    @Override
    public void water(SharedPlant sharedPlant, Member currentMember, List<WateringStrategy> allStrategies) {
        WateringStrategy baseStrategy = findBaseStrategy(sharedPlant, allStrategies);

        if (sharedPlant.getGrowthDate() != null) {
            long daysSinceWatered = Math.max(0, ChronoUnit.DAYS.between(sharedPlant.getGrowthDate(), LocalDate.now()) - 1);
            if (daysSinceWatered <= 1) {
                throw new SharedPlantException(SharedPlantErrorCode.CANNOT_USE_NUTRIENT_YET);
            }
        }

        if (currentMember.getNutrientCount() <= 0) {
            throw new SharedPlantException(SharedPlantErrorCode.DONT_HAVE_NUTRIENT);
        }

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        boolean alreadyGaveNutrient = sharedPlantLogRepository.existsBySharedPlantAndWateredByAndUsedNutrientAndCreatedAtBetween(
                sharedPlant,
                currentMember.getId(),
                true,
                startOfDay,
                endOfDay
        );

        if (alreadyGaveNutrient) {
            throw new SharedPlantException(SharedPlantErrorCode.ALREADY_GAVE_NUTRIENT_TODAY);
        }

        baseStrategy.water(sharedPlant, currentMember, allStrategies);

        sharedPlant.nutrient(GrowthType.NUTRIENT.getGrowth(), currentMember);
    }

    private WateringStrategy findBaseStrategy(SharedPlant sharedPlant, List<WateringStrategy> allStrategies) {

        SharedPlantReqDTO.WaterPlantReqDTO dto = new SharedPlantReqDTO.WaterPlantReqDTO(SupplyType.WATER);

        return allStrategies.stream()
                .filter(strategy -> !(strategy instanceof NutrientWateringStrategy))
                .filter(strategy -> strategy.supports(sharedPlant, dto))
                .findFirst()
                .orElseThrow(() -> new SharedPlantException(SharedPlantErrorCode.NOT_FOUND_STRATEGY));
    }
}
