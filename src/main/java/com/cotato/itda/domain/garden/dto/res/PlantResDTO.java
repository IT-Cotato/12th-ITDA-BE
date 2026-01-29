package com.cotato.itda.domain.garden.dto.res;

import com.cotato.itda.domain.garden.enums.PlantDifficulty;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public class PlantResDTO {

    @Builder
    public record CreatePlantResDTO(
            Long plantId,
            String plantName,
            LocalDateTime createdAt
    ) {}

    @Builder
    public record PlantListResDto(
            Integer count,
            List<PlantInfoResDto> plants
    ) {}

    @Builder
    public record PlantInfoResDto(
            Long plantId,
            String plantName,
            PlantDifficulty difficulty,
            Integer duration,
            String hashtags,
            Integer height
    ) {}
}
