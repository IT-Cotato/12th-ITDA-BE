package com.cotato.itda.domain.garden.service.command;

import com.cotato.itda.domain.garden.dto.req.PlantReqDTO;
import com.cotato.itda.domain.garden.dto.res.PlantResDTO;

public interface PlantCommandService {
    PlantResDTO.CreatePlantResDTO createPlant(PlantReqDTO.CreatePlantReqDTO dto);
}
