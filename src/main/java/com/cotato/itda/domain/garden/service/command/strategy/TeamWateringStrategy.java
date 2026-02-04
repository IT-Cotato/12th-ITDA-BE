package com.cotato.itda.domain.garden.service.command.strategy;

import com.cotato.itda.domain.garden.dto.req.SharedPlantReqDTO;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.enums.GrowthType;
import com.cotato.itda.domain.garden.enums.MaxGrowthCount;
import com.cotato.itda.domain.garden.enums.SupplyType;
import com.cotato.itda.domain.garden.exception.SharedPlantException;
import com.cotato.itda.domain.garden.exception.code.SharedPlantErrorCode;
import com.cotato.itda.domain.member.entity.Member;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class TeamWateringStrategy implements WateringStrategy {

    @Override
    public boolean supports(SharedPlant sharedPlant, SharedPlantReqDTO.WaterPlantReqDTO dto) {
        return !sharedPlant.getIsSoloMode() && dto.supplyType() == SupplyType.WATER;
    }

    @Override
    public void water(SharedPlant sharedPlant, Member currentMember, List<WateringStrategy> allStrategies) {

        if (sharedPlant.getLastWateredBy() == null && sharedPlant.getGrowthDate() == null) {
            sharedPlant.water(GrowthType.WATER.getGrowth(), currentMember, true);
            return;
        }

        if (sharedPlant.getLastWateredBy() != null && sharedPlant.getLastWateredBy().equals(currentMember.getId())) {
            throw new SharedPlantException(SharedPlantErrorCode.CANNOT_WATER_CONSECUTIVELY);
        }

         if (sharedPlant.getGrowthDate().isEqual(LocalDate.now())) {
            if (sharedPlant.getDailyGrowthCount() >= MaxGrowthCount.TEAM_GROW.getMaxGrowthCount()) {
                throw new SharedPlantException(SharedPlantErrorCode.EXCEED_WATER_COUNT);
            }

            if (sharedPlant.getDailyGrowthCount() < MaxGrowthCount.TEAM_WATER.maxGrowthCount) {
                sharedPlant.water(GrowthType.WATER.getGrowth(), currentMember, false);
            } else {
                sharedPlant.water(GrowthType.NONE.getGrowth(), currentMember, false);
            }
        } else {
            sharedPlant.water(GrowthType.WATER.getGrowth(), currentMember, true);
        }
    }
}
