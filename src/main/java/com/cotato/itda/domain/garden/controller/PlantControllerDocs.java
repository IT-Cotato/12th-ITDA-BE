package com.cotato.itda.domain.garden.controller;

import com.cotato.itda.domain.garden.dto.req.PlantReqDTO;
import com.cotato.itda.domain.garden.dto.res.PlantResDTO;
import com.cotato.itda.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

public interface PlantControllerDocs {

        // @Operation(summary = "식물 추가 API By 정원", description = "새로운 식물을 추가합니다. 관리자 권한이
        // 필요합니다.")
        // @ApiResponses({
        // @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200",
        // description = "성공"),
        // @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400",
        // description = "유효하지 않은 입력값 또는 이미 존재하는 식물 이름"),
        // @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403",
        // description = "관리자 권한 없음")
        // })
        // @PostMapping
        // ApiResponse<PlantResDTO.CreatePlantResDTO> createPlant(
        // @Valid @RequestBody PlantReqDTO.CreatePlantReqDTO dto
        // );

        @Operation(summary = "전체 식물 조회 API By 정원", description = "전체 식물 목록을 조회합니다. 정렬 조건을 변경할 수 있습니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
        })
        @GetMapping
        ApiResponse<PlantResDTO.PlantListResDto> getAllPlants(
                        @Parameter(description = "정렬 기준 (기본값: difficulty, 오름차순)") @ParameterObject @SortDefault(sort = "difficulty", direction = Sort.Direction.ASC) Sort sort);
}
