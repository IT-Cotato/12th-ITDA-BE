package com.cotato.itda.domain.friendship.controller;

import com.cotato.itda.domain.friendship.dto.req.FriendshipReqDTO;
import com.cotato.itda.domain.friendship.dto.res.FriendshipResDTO;
import com.cotato.itda.domain.friendship.enums.FriendshipStatus;
import com.cotato.itda.domain.friendship.service.command.FriendshipCommandService;
import com.cotato.itda.domain.friendship.service.query.FriendshipQueryService;
import com.cotato.itda.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/friendships")
@Tag(name = "Friendship", description = "친구 관계 API")
public class FriendshipController implements FriendshipControllerDocs {

    private final FriendshipCommandService friendshipCommandService;
    private final FriendshipQueryService friendshipQueryService;

    // TODO: JWT 사용 시 memberId 추출 로직으로 변경
    @PostMapping("/{friendId}")
    @Override
    public ApiResponse<FriendshipResDTO.CreateDTO> createFriendship(
            @RequestParam Long memberId,
            @PathVariable Long friendId
    ) {
        return ApiResponse.success(friendshipCommandService.createFriendship(friendId, memberId));
    }

    // TODO: JWT 사용 시 memberId 추출 로직으로 변경
    @PatchMapping("/{friendshipId}")
    @Override
    public ApiResponse<FriendshipResDTO.UpdateDTO> updateFriendship(
            @PathVariable Long friendshipId,
            @RequestParam Long memberId,
            @Valid @RequestBody FriendshipReqDTO.UpdateDTO dto
    ) {
        return ApiResponse.success(friendshipCommandService.updateFriendship(dto, friendshipId, memberId));
    }

    // TODO: JWT 사용 시 memberId 추출 로직으로 변경
    @GetMapping
    @Override
    public ApiResponse<FriendshipResDTO.FriendshipListDTO> getFriendshipList(
            @RequestParam Long memberId,
            @RequestParam(required = false) List<FriendshipStatus> status,
            @SortDefault(sort = "lastInteractedAt", direction = Sort.Direction.DESC) Sort sort
    ) {
        return ApiResponse.success(friendshipQueryService.getFriendshipList(memberId, status, sort));
    }

    // TODO: JWT 사용 시 memberId 추출 로직으로 변경
    @DeleteMapping("/{friendshipId}")
    @Override
    public ApiResponse<Void> deleteFriendship(
            @PathVariable Long friendshipId,
            @RequestParam Long memberId
    ) {
        friendshipCommandService.deleteFriendship(friendshipId, memberId);
        return ApiResponse.success(null);
    }

    // TODO: JWT 사용 시 memberId 추출 로직으로 변경
    @GetMapping("/settings/{friendshipId}")
    @Override
    public ApiResponse<FriendshipResDTO.FriendshipSettingsDTO> getFriendshipSettings(
            @PathVariable Long friendshipId,
            @RequestParam Long memberId
    ) {
        return ApiResponse.success(friendshipQueryService.getFriendshipSettings(friendshipId, memberId));
    }
}
