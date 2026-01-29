package com.cotato.itda.domain.garden.dto.req;

import com.cotato.itda.domain.garden.enums.PlantDifficulty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PlantReqDTO {

    public record CreatePlantReqDTO(
            @NotBlank(message = "이름은 필수입니다.")
            @Size(min = 2, max = 20, message = "이름은 2~20자 사이여야 합니다.")
            String name,
            @NotNull(message = "난이도는 필수입니다.")
            PlantDifficulty difficulty,
            @NotNull(message = "기간은 필수입니다.")
            Integer duration,
            @NotBlank(message = "해시태그는 필수입니다.")
            String hashtags,
            @NotNull(message = "높이는 필수입니다.")
            Integer height,
            @NotNull(message = "최대 씨앗 성장값은 필수입니다.")
            Integer seedMax,
            @NotNull(message = "최대 새싹 성장값은 필수입니다.")
            Integer sproutMax,
            @NotNull(message = "최대 줄기 성장값은 필수입니다.")
            Integer stemMax,
            @NotNull(message = "최대 꽃봉오리 성장값은 필수입니다.")
            Integer budMax,
            @NotNull(message = "최대 만개 성장값은 필수입니다.")
            Integer bloomMax
    ) {}
}
