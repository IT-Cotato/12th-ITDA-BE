package com.cotato.itda.domain.garden.controller;

import com.cotato.itda.domain.garden.dto.req.SharedPlantReqDTO;
import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.service.command.SharedPlantCommandService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shared-plants")
public class SharedPlantController implements SharedPlantControllerDocs {

    private final SharedPlantCommandService sharedPlantCommandService;

    // TODO: 식물 시듦 상태 변환 & 식물 함께 돌봄으로 변환

    @PatchMapping("/{sharedPlantId}")
    public ApiResponse<SharedPlantResDTO.WaterInfoResDTO> wateringPlant(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @PathVariable(name = "sharedPlantId") Long sharedPlantId,
            @Valid @RequestBody SharedPlantReqDTO.WaterPlantReqDTO dto
    ) {
        return ApiResponse.success(sharedPlantCommandService.waterPlant(sharedPlantId, dto, jwtPrincipal.memberId()));
    }
}
