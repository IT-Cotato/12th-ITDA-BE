package com.cotato.itda.domain.garden.service.command.strategy;

import com.cotato.itda.domain.garden.dto.req.SharedPlantReqDTO;
import com.cotato.itda.domain.garden.entity.SharedPlant;
import com.cotato.itda.domain.garden.enums.GrowthType;
import com.cotato.itda.domain.garden.enums.SupplyType;
import com.cotato.itda.domain.garden.exception.SharedPlantException;
import com.cotato.itda.domain.garden.exception.code.SharedPlantErrorCode;
import com.cotato.itda.domain.member.entity.Member;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class SoloWateringStrategy implements WateringStrategy {

    @Override
    public boolean supports(SharedPlant sharedPlant, SharedPlantReqDTO.WaterPlantReqDTO dto) {
        return sharedPlant.getIsSoloMode() && dto.supplyType() == SupplyType.WATER;
    }

    @Override
    public void water(SharedPlant sharedPlant, Member currentMember, List<WateringStrategy> allStrategies) {

        if (sharedPlant.getGrowthDate().isEqual(LocalDate.now())) {
            throw new SharedPlantException(SharedPlantErrorCode.ALREADY_WATERED);
        }

        sharedPlant.water(GrowthType.WATER.getGrowth(), currentMember, true);
    }
}
