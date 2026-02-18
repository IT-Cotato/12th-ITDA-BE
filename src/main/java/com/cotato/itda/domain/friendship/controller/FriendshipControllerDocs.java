package com.cotato.itda.domain.friendship.controller;

import com.cotato.itda.domain.friendship.dto.req.FriendshipReqDTO;
import com.cotato.itda.domain.friendship.dto.res.FriendshipResDTO;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.Explode;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface FriendshipControllerDocs {

    @Operation(
            summary = "친구 추가 API By 정원",
            description = "friendId를 Path Variable로 전달받아 친구 관계를 추가합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "자기 자신을 친구로 추가하거나 이미 존재하는 친구 관계"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @PostMapping("/{friendId}")
    ApiResponse<FriendshipResDTO.CreateDTO> createFriendship(
            @Parameter(hidden = true) JwtPrincipal jwtPrincipal,

            @Parameter(description = "친구로 추가할 사용자 ID", required = true)
            @PathVariable Long friendId
    );

    @Operation(
            summary = "친구 관계 설정 API By 정원",
            description = "친구 별명, 대화 말투, 대화 목표, 대화 주제를 설정합니다. 설정 완료 시 상태가 ACTIVE로 변경됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력값 또는 존재하지 않는 대화 주제"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (본인의 친구 관계가 아님)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "친구 관계를 찾을 수 없음")
    })
    @PatchMapping("/{friendshipId}")
    ApiResponse<FriendshipResDTO.UpdateDTO> updateFriendship(
            @Parameter(description = "수정할 친구 관계 ID", required = true)
            @PathVariable Long friendshipId,

            @Parameter(hidden = true) JwtPrincipal jwtPrincipal,

            @Valid @RequestBody FriendshipReqDTO.UpdateDTO dto
    );

    @Operation(
            summary = "친구 목록 조회 API By 정원",
            description = "전체 친구 목록을 조회합니다. status 필터링 및 정렬이 가능합니다. status를 지정하지 않으면 ACTIVE 상태의 친구만 조회됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping
    ApiResponse<FriendshipResDTO.FriendshipListDTO> getFriendshipList(
            @Parameter(hidden = true) JwtPrincipal jwtPrincipal,

            @Parameter(
                    description = "친구 설정 상태. 친구를 설정했다면 ACTIVE로, 설정하지 않았다면 PENDING으로 조회됩니다. 미입력 시 ACTIVE만 조회됩니다.",
                    explode = Explode.TRUE,
                    array = @ArraySchema(schema = @Schema(implementation = FriendshipStatus.class))
            )
            @RequestParam(required = false, defaultValue = "ACTIVE") List<FriendshipStatus> status,

            @ParameterObject
            @Parameter(
                    name = "sort",
                    description = "정렬 기준. 예: lastInteractedAt,desc 또는 createdAt,asc"
            )
            Sort sort
    );

    @Operation(
            summary = "친구 관계 삭제 API By 정원",
            description = "친구 관계를 삭제합니다. DB에서 완전히 삭제되며 복구할 수 없습니다. 연관된 대화 주제도 함께 삭제됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (본인의 친구 관계가 아님)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "친구 관계를 찾을 수 없음")
    })
    @DeleteMapping("/{friendshipId}")
    ApiResponse<Void> deleteFriendship(
            @Parameter(description = "삭제할 친구 관계 ID", required = true)
            @PathVariable Long friendshipId,

            @Parameter(hidden = true) JwtPrincipal jwtPrincipal
    );

    @Operation(
            summary = "친구 관계 설정 조회 API By 정원",
            description = "친구 관계의 상세 설정 정보를 조회합니다. 별명, 대화 말투, 대화 목표, 대화 주제를 포함합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (본인의 친구 관계가 아님)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "친구 관계를 찾을 수 없음")
    })
    @GetMapping("/settings/{friendshipId}")
    ApiResponse<FriendshipResDTO.FriendshipSettingsDTO> getFriendshipSettings(
            @Parameter(description = "조회할 친구 관계 ID", required = true)
            @PathVariable Long friendshipId,

            @Parameter(hidden = true) JwtPrincipal jwtPrincipal
    );

    @Operation(
            summary = "초대 코드로 회원 검색 API By 정원",
            description = "초대 코드를 통해 친구로 추가할 회원을 검색합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "본인의 초대 코드로 검색할 수 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 초대 코드의 회원을 찾을 수 없음")
    })
    @GetMapping("/search")
    ApiResponse<FriendshipResDTO.SearchByInviteCodeDTO> searchByInviteCode(
            @Parameter(hidden = true) JwtPrincipal jwtPrincipal,

            @Parameter(description = "검색할 초대 코드", required = true)
            @RequestParam String inviteCode
    );
}
