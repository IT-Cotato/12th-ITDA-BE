package com.cotato.itda.domain.garden.controller;

import com.cotato.itda.domain.garden.dto.req.SharedPlantReqDTO;
import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SharedPlantControllerDocs {

    @Operation(summary = "공유 식물 목록 조회 API By 정원", description = "상태별로 공유 식물 목록을 조회합니다. ?status=GROWING&status=WITHERED 또는 ?status=COMPLETED 형태로 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    ApiResponse<SharedPlantResDTO.SharedPlantInfoListDTO> getSharedPlants(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "조회할 상태 목록 (GROWING, WITHERED, COMPLETED)", example = "GROWING")
            @RequestParam(name = "status") List<SharedPlantStatus> statuses
    );

    @Operation(summary = "공유 식물 물주기 API By 정원", description = "공유 식물에 물을 줍니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공유 식물을 찾을 수 없음")
    })
    @PatchMapping("/{sharedPlantId}")
    ApiResponse<SharedPlantResDTO.WaterInfoResDTO> wateringPlant(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "공유 식물 ID") @PathVariable(name = "sharedPlantId") Long sharedPlantId,
            @Valid @RequestBody SharedPlantReqDTO.WaterPlantReqDTO dto
    );
}
