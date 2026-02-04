package com.cotato.itda.domain.garden.controller;

import com.cotato.itda.domain.garden.dto.req.PlantInviteReqDTO;
import com.cotato.itda.domain.garden.dto.res.PlantInviteResDTO;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "PlantInvite", description = "식물 초대 API")
public interface SharedPlantInviteControllerDocs {

        @Operation(summary = "식물 초대 후보 친구 리스트 조회 API By 정원", description = "식물 키우기에 초대할 수 있는 후보 친구 목록을 조회합니다. (현재 함께 키우는 식물이 없고, 초대 대기 상태도 아닌 친구)")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
        })
        @SecurityRequirement(name = "AccessToken")
        @GetMapping("/candidates")
        ApiResponse<PlantInviteResDTO.PlantInviteCandidatesResDto> getInviteCandidates(
                        @AuthenticationPrincipal JwtPrincipal jwtPrincipal);

        @Operation(summary = "내 공유 식물 초대 목록 조회 API By 정원", description = "나에게 온 대기 중인(PENDING) 식물 초대 목록을 조회합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공")
        })
        @SecurityRequirement(name = "AccessToken")
        @GetMapping("/my")
        ApiResponse<PlantInviteResDTO.MyPlantInviteListResDTO> getMyPendingInvitations(
                        @AuthenticationPrincipal JwtPrincipal jwtPrincipal);

        @Operation(summary = "식물 초대 요청 API By 정원", description = "친구에게 식물 키우기 초대 요청을 보냅니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "자기 자신 초대 불가능, 이미 대기 소환장 존재, 이미 공유 식물 존재"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "본인 혹은 상대방이 ACTIVE 상태가 아님 (친구 아님)"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자 혹은 식물을 찾을 수 없음")
        })
        @SecurityRequirement(name = "AccessToken")
        @PostMapping
        ApiResponse<PlantInviteResDTO.CreateSharedPlantInviteResDTO> createInvite(
                        @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
                        @Valid @RequestBody PlantInviteReqDTO.CreateSharedPlantInviteReqDTO dto);

        @Operation(summary = "식물 초대 수락/거절 API By 정원", description = "나에게 온 식물 초대를 수락하거나 거절합니다.")
        @ApiResponses({
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "이미 처리된 초대장, 이미 키우고 있는 식물 존재, 잘못된 상태값"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "초대받은 본인이 아님"),
                        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "초대장을 찾을 수 없음")
        })
        @SecurityRequirement(name = "AccessToken")
        @PatchMapping("/{plantInviteId}")
        ApiResponse<PlantInviteResDTO.UpdateSharedPlantInviteResDTO> updateInviteStatus(
                        @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
                        @PathVariable(name = "plantInviteId") Long plantInviteId,
                        @Valid @RequestBody PlantInviteReqDTO.UpdateSharedPlantInviteReqDTO dto);
}
