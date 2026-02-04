package com.cotato.itda.domain.garden.dto.req;

import com.cotato.itda.domain.garden.enums.SupplyType;
import jakarta.validation.constraints.NotNull;

public class SharedPlantReqDTO {

    public record WaterPlantReqDTO(
            @NotNull(message = "물주기 타입은 필수입니다.")
            SupplyType supplyType
    ) {}
}
