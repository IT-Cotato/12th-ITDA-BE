package com.cotato.itda.domain.garden.service.command;

import com.cotato.itda.domain.garden.converter.PlantConverter;
import com.cotato.itda.domain.garden.dto.req.PlantReqDTO;
import com.cotato.itda.domain.garden.dto.res.PlantResDTO;
import com.cotato.itda.domain.garden.entity.Plant;
import com.cotato.itda.domain.garden.exception.PlantException;
import com.cotato.itda.domain.garden.exception.code.PlantErrorCode;
import com.cotato.itda.domain.garden.repository.PlantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class PlantCommandServiceImpl implements PlantCommandService {

    private final PlantRepository plantRepository;

    @Override
    public PlantResDTO.CreatePlantResDTO createPlant(PlantReqDTO.CreatePlantReqDTO dto) {

        if (plantRepository.existsByName(dto.name())) {
            throw new PlantException(PlantErrorCode.DUPLICATE_NAME, Map.of("name", dto.name()));
        }
        Plant plant = PlantConverter.toPlant(dto);
        plantRepository.save(plant);
        return PlantConverter.toPlantCreateResDto(plant);
    }
}
