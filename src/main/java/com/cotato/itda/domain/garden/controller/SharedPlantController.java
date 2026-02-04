package com.cotato.itda.domain.garden.controller;

import com.cotato.itda.domain.garden.dto.req.SharedPlantReqDTO;
import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.domain.garden.service.command.SharedPlantCommandService;
import com.cotato.itda.domain.garden.service.query.SharedPlantQueryService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import jakarta.validation.Valid;
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

    // TODO: 식물 시듦 상태 변환 & 식물 함께 돌봄으로 변환 & 솔로 모드 자동 진입

    @GetMapping
    public ApiResponse<SharedPlantResDTO.SharedPlantInfoListDTO> getSharedPlants(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @RequestParam(name = "status", required = false) List<SharedPlantStatus> statuses
    ) {
        List<SharedPlantStatus> effectiveStatuses = (statuses == null || statuses.isEmpty())
                ? List.of(SharedPlantStatus.GROWING, SharedPlantStatus.WITHERED)
                : statuses;
        return ApiResponse.success(sharedPlantQueryService.getSharedPlants(jwtPrincipal.memberId(), effectiveStatuses));
    }

    @PatchMapping("/{sharedPlantId}")
    public ApiResponse<SharedPlantResDTO.WaterInfoResDTO> wateringPlant(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @PathVariable(name = "sharedPlantId") Long sharedPlantId,
            @Valid @RequestBody SharedPlantReqDTO.WaterPlantReqDTO dto
    ) {
        return ApiResponse.success(sharedPlantCommandService.waterPlant(sharedPlantId, dto, jwtPrincipal.memberId()));
    }
}
