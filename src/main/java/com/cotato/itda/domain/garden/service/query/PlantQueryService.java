package com.cotato.itda.domain.garden.service.query;

import com.cotato.itda.domain.garden.dto.res.PlantResDTO;
import org.springframework.data.domain.Sort;

public interface PlantQueryService {
    PlantResDTO.PlantListResDto getPlantList(Sort sort);
}
