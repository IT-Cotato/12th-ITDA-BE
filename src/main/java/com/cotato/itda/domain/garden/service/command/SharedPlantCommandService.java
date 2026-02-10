package com.cotato.itda.domain.garden.service.command;

import com.cotato.itda.domain.garden.dto.req.SharedPlantReqDTO;
import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;

public interface SharedPlantCommandService {
    SharedPlantResDTO.WaterInfoResDTO waterPlant(Long sharedPlantId, SharedPlantReqDTO.WaterPlantReqDTO dto, Long memberId);

    SharedPlantResDTO.PlantActionResDTO water(Long sharedPlantId, Long memberId);

    SharedPlantResDTO.PlantActionResDTO plantSeed(Long sharedPlantId, Long memberId);
}
