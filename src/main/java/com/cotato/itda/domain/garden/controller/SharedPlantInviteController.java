package com.cotato.itda.domain.garden.controller;

import com.cotato.itda.domain.garden.dto.req.PlantInviteReqDTO;
import com.cotato.itda.domain.garden.dto.res.PlantInviteResDTO;
import com.cotato.itda.domain.garden.service.command.SharedPlantInviteCommandService;
import com.cotato.itda.domain.garden.service.query.SharedPlantInviteQueryService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/plant-invites")
public class SharedPlantInviteController implements SharedPlantInviteControllerDocs {

    private final SharedPlantInviteQueryService sharedPlantInviteQueryService;
    private final SharedPlantInviteCommandService sharedPlantInviteCommandService;

    @Override
    @GetMapping("/candidates")
    public ApiResponse<PlantInviteResDTO.PlantInviteCandidatesResDto> getInviteCandidates(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal) {
        return ApiResponse.success(sharedPlantInviteQueryService.getInviteCandidates(jwtPrincipal.memberId()));
    }

    @Override
    @GetMapping("/my")
    public ApiResponse<PlantInviteResDTO.MyPlantInviteListResDTO> getMyPendingInvitations(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal) {
        return ApiResponse.success(sharedPlantInviteQueryService.getMyPendingInvitations(jwtPrincipal.memberId()));
    }

    @Override
    @PostMapping
    public ApiResponse<PlantInviteResDTO.CreateSharedPlantInviteResDTO> createInvite(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Valid @RequestBody PlantInviteReqDTO.CreateSharedPlantInviteReqDTO dto) {
        return ApiResponse.success(sharedPlantInviteCommandService.createInvite(jwtPrincipal.memberId(), dto));
    }

    @Override
    @PatchMapping("/{plantInviteId}")
    public ApiResponse<PlantInviteResDTO.UpdateSharedPlantInviteResDTO> updateInviteStatus(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @PathVariable(name = "plantInviteId") Long plantInviteId,
            @Valid @RequestBody PlantInviteReqDTO.UpdateSharedPlantInviteReqDTO dto) {
        return ApiResponse.success(
                sharedPlantInviteCommandService.updateInviteStatus(jwtPrincipal.memberId(), plantInviteId, dto));
    }
}
