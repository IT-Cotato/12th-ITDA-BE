package com.cotato.itda.domain.garden.service.command;

import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;

public interface SharedPlantCommandService {

    SharedPlantResDTO.PlantActionResDTO water(Long sharedPlantId, Long memberId);

    SharedPlantResDTO.PlantActionResDTO giveNutrient(Long sharedPlantId, Long memberId);

    SharedPlantResDTO.PlantActionResDTO plantSeed(Long sharedPlantId, Long memberId);
}
