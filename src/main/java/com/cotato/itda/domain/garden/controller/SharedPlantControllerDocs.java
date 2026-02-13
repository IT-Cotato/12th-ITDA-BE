package com.cotato.itda.domain.garden.controller;

import com.cotato.itda.domain.garden.dto.res.SharedPlantResDTO;
import com.cotato.itda.domain.garden.enums.SharedPlantStatus;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface SharedPlantControllerDocs {

    @Operation(summary = "공유 식물 목록 조회 API By 정원", description = "상태별로 공유 식물 목록을 조회합니다. ?status=GROWING&status=WITHERED 또는 ?status=COMPLETED 형태로 조회합니다. status를 지정하지 않으면 기본값으로 GROWING, WITHERED를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping
    ApiResponse<SharedPlantResDTO.SharedPlantInfoListDTO> getSharedPlants(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(
                    description = "조회할 상태 목록 (GROWING, WITHERED, COMPLETED). 미지정 시 기본값: GROWING, WITHERED",
                    array = @ArraySchema(schema = @Schema(implementation = SharedPlantStatus.class))
            )
            @RequestParam(name = "status", required = false) List<SharedPlantStatus> statuses
    );

    @Operation(summary = "공유 식물 물주기 API By 정원", description = """
            공유 식물에 물을 줍니다.

            **물주기 규칙:**
            - 번갈아가며 물을 줘야 합니다 (연속 물주기 불가)
            - 72시간 이상 경과 시 영양제를 먼저 줘야 합니다
            - 영양제 투여 후에는 같은 사람이 물을 줘야 합니다

            **상태별 동작:**
            - WITHERED (48h~72h): 물주기 가능, 상태 회복
            - NUTRITION_AVAILABLE (72h+): 영양제 필요 에러 반환
            - AFTER_NUTRITION: 영양제 준 본인만 물주기 가능
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "씨앗이 심어지지 않음 / 연속 물주기 불가 / 영양제 필요 / 완료된 식물"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "참여자가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공유 식물을 찾을 수 없음")
    })
    @PostMapping("/{sharedPlantId}/water")
    ApiResponse<SharedPlantResDTO.PlantActionResDTO> wateringPlant(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "공유 식물 ID") @PathVariable(name = "sharedPlantId") Long sharedPlantId
    );

    @Operation(summary = "영양제 주기 API By 정원", description = """
            시든 식물에 영양제를 줍니다.

            **영양제 규칙:**
            - 72시간 이상 물을 주지 않은 경우에만 사용 가능
            - 영양제 투여 후 같은 사람이 물을 줘야 식물이 회복됩니다
            - 영양제 보유 개수가 1개 이상이어야 합니다

            **상태 변화:**
            - NUTRITION_AVAILABLE (72h+) → AFTER_NUTRITION
            """)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "씨앗이 심어지지 않음 / 영양제를 줄 수 없음 / 영양제 없음 / 완료된 식물"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "참여자가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공유 식물을 찾을 수 없음")
    })
    @PostMapping("/{sharedPlantId}/nutrient")
    ApiResponse<SharedPlantResDTO.PlantActionResDTO> giveNutrient(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "공유 식물 ID") @PathVariable(name = "sharedPlantId") Long sharedPlantId
    );

    @Operation(summary = "씨앗 심기 API By 정원", description = "초대 수락 후 씨앗을 심습니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 심어진 식물 / 완료된 식물"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증 실패"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "참여자가 아님"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "공유 식물을 찾을 수 없음")
    })
    @PostMapping("/{sharedPlantId}/plant")
    ApiResponse<SharedPlantResDTO.PlantActionResDTO> plantSeed(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "공유 식물 ID") @PathVariable(name = "sharedPlantId") Long sharedPlantId
    );
}
