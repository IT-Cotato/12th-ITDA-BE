package com.cotato.itda.domain.garden.controller;

import com.cotato.itda.domain.garden.dto.req.PlantReqDTO;
import com.cotato.itda.domain.garden.dto.res.PlantResDTO;
import com.cotato.itda.domain.garden.service.command.PlantCommandService;
import com.cotato.itda.domain.garden.service.query.PlantQueryService;
import com.cotato.itda.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plants")
@Tag(name = "Plant", description = "식물 API")
public class PlantController implements PlantControllerDocs {

    private final PlantCommandService plantCommandService;
    private final PlantQueryService plantQueryService;

    // @Override
    // @PostMapping
    // public ApiResponse<PlantResDTO.CreatePlantResDTO> createPlant(
    // @Valid @RequestBody PlantReqDTO.CreatePlantReqDTO dto) {
    // return ApiResponse.success(plantCommandService.createPlant(dto));
    // }

    @Override
    @GetMapping
    public ApiResponse<PlantResDTO.PlantListResDto> getAllPlants(
            @SortDefault(sort = "difficulty", direction = Sort.Direction.ASC) Sort sort
    ) {
        return ApiResponse.success(plantQueryService.getPlantList(sort));
    }
}
