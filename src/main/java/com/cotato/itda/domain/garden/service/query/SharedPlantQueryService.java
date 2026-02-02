package com.cotato.itda.domain.garden.service.query;

import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;

import java.util.List;

public interface SharedPlantQueryService {
    SharedPlantResDTO.SharedPlantInfoListDTO getSharedPlants(Long memberId, List<SharedPlantStatus> statuses);
}
