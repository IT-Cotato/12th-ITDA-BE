package com.cotato.itda.domain.garden.converter;

import com.cotato.itda.domain.garden.dto.req.PlantReqDTO;
import com.cotato.itda.domain.garden.dto.res.PlantResDTO;
import com.cotato.itda.domain.garden.entity.Plant;

import java.util.List;

public class PlantConverter {

    public static PlantResDTO.CreatePlantResDTO toPlantCreateResDto(Plant plant) {
        return PlantResDTO.CreatePlantResDTO.builder()
                .plantId(plant.getId())
                .plantName(plant.getName())
                .createdAt(plant.getCreatedAt())
                .build();
    }

    public static Plant toPlant(PlantReqDTO.CreatePlantReqDTO dto) {
        return Plant.builder()
                .name(dto.name())
                .difficulty(dto.difficulty())
                .duration(dto.duration())
                .hashtags(dto.hashtags())
                .height(dto.height())
                .seedMax(dto.seedMax())
                .sproutMax(dto.sproutMax())
                .stemMax(dto.stemMax())
                .budMax(dto.budMax())
                .bloomMax(dto.bloomMax())
                .build();
    }

    public static PlantResDTO.PlantInfoResDto toPlantInfoResDto(Plant plant) {
        return PlantResDTO.PlantInfoResDto.builder()
                .plantId(plant.getId())
                .plantName(plant.getName())
                .difficulty(plant.getDifficulty())
                .duration(plant.getDuration())
                .hashtags(plant.getHashtags())
                .height(plant.getHeight())
                .build();
    }

    public static PlantResDTO.PlantListResDto toPlantListResDto(List<Plant> plants) {

        List<PlantResDTO.PlantInfoResDto> plantInfos = plants.stream()
                .map(PlantConverter::toPlantInfoResDto)
                .toList();

        return PlantResDTO.PlantListResDto.builder()
                .count(plants.size())
                .plants(plantInfos)
                .build();
    }
}
