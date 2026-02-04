package com.cotato.itda.domain.friendship.controller;

import com.cotato.itda.domain.friendship.dto.req.FriendshipReqDTO;
import com.cotato.itda.domain.friendship.dto.res.FriendshipResDTO;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.service.command.FriendshipCommandService;
import com.cotato.itda.domain.friendship.service.query.FriendshipQueryService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friendships")
@Tag(name = "Friendship", description = "친구 관계 API")
public class FriendshipController implements FriendshipControllerDocs {

    private final FriendshipCommandService friendshipCommandService;
    private final FriendshipQueryService friendshipQueryService;

    @SecurityRequirement(name = "AccessToken")
    @PostMapping("/{friendId}")
    @Override
    public ApiResponse<FriendshipResDTO.CreateDTO> createFriendship(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @PathVariable Long friendId
    ) {
        Long memberId = jwtPrincipal.memberId();
        return ApiResponse.success(friendshipCommandService.createFriendship(friendId, memberId));
    }

    @SecurityRequirement(name = "AccessToken")
    @PatchMapping("/{friendshipId}")
    @Override
    public ApiResponse<FriendshipResDTO.UpdateDTO> updateFriendship(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Valid @RequestBody FriendshipReqDTO.UpdateDTO dto
    ) {
        Long memberId = jwtPrincipal.memberId();
        return ApiResponse.success(friendshipCommandService.updateFriendship(dto, friendshipId, memberId));
    }

    @SecurityRequirement(name = "AccessToken")
    @GetMapping
    @Override
    public ApiResponse<FriendshipResDTO.FriendshipListDTO> getFriendshipList(
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @RequestParam(required = false) List<FriendshipStatus> status,
            @SortDefault(sort = "lastInteractedAt", direction = Sort.Direction.DESC) Sort sort
    ) {
        Long memberId = jwtPrincipal.memberId();
        return ApiResponse.success(friendshipQueryService.getFriendshipList(memberId, status, sort));
    }

    @SecurityRequirement(name = "AccessToken")
    @DeleteMapping("/{friendshipId}")
    @Override
    public ApiResponse<Void> deleteFriendship(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal
    ) {
        Long memberId = jwtPrincipal.memberId();
        friendshipCommandService.deleteFriendship(friendshipId, memberId);
        return ApiResponse.success(null);
    }

    @SecurityRequirement(name = "AccessToken")
    @GetMapping("/settings/{friendshipId}")
    @Override
    public ApiResponse<FriendshipResDTO.FriendshipSettingsDTO> getFriendshipSettings(
            @PathVariable Long friendshipId,
            @AuthenticationPrincipal JwtPrincipal jwtPrincipal
    ) {
        Long memberId = jwtPrincipal.memberId();
        return ApiResponse.success(friendshipQueryService.getFriendshipSettings(friendshipId, memberId));
    }
}
