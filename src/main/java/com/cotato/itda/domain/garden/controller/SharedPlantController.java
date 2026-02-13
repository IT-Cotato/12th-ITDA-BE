package com.cotato.itda.domain.garden.controller;

import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.domain.garden.service.command.SharedPlantCommandService;
import com.cotato.itda.domain.garden.service.query.SharedPlantQueryService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/shared-plants")
public class SharedPlantController implements SharedPlantControllerDocs {

    private final SharedPlantCommandService sharedPlantCommandService;
    private final SharedPlantQueryService sharedPlantQueryService;

    // TODO: 식물 시듦 상태 변환 & 식물 함께 돌봄으로 변환

    @GetMapping
    public ApiResponse<SharedPlantResDTO.SharedPlantInfoListDTO> getSharedPlants(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @RequestParam(name = "status", required = false) List<SharedPlantStatus> statuses
    ) {
        List<SharedPlantStatus> effectiveStatuses = (statuses == null || statuses.isEmpty())
                ? List.of(SharedPlantStatus.GROWING)
                : statuses;
        return ApiResponse.success(sharedPlantQueryService.getSharedPlants(jwtPrincipal.memberId(), effectiveStatuses));
    }

    @PostMapping("/{sharedPlantId}/water")
    public ApiResponse<SharedPlantResDTO.PlantActionResDTO> wateringPlant(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @PathVariable(name = "sharedPlantId") Long sharedPlantId
    ) {
        return ApiResponse.success(sharedPlantCommandService.water(sharedPlantId, jwtPrincipal.memberId()));
    }

    @PostMapping("/{sharedPlantId}/nutrient")
    public ApiResponse<SharedPlantResDTO.PlantActionResDTO> giveNutrient(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @PathVariable(name = "sharedPlantId") Long sharedPlantId
    ) {
        return ApiResponse.success(sharedPlantCommandService.giveNutrient(sharedPlantId, jwtPrincipal.memberId()));
    }

    @PostMapping("/{sharedPlantId}/plant")
    public ApiResponse<SharedPlantResDTO.PlantActionResDTO> plantSeed(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @PathVariable(name = "sharedPlantId") Long sharedPlantId
    ) {
        return ApiResponse.success(sharedPlantCommandService.plantSeed(sharedPlantId, jwtPrincipal.memberId()));
    }
}
