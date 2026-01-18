package com.cotato.itda.domain.diary.controller;

import com.cotato.itda.domain.diary.dto.response.DiaryLikeResponse;
import com.cotato.itda.domain.diary.service.command.DiaryLikeCommandService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
@Tag(name = "Diary", description = "공유일기 API")
public class DiaryLikeController {

    private final DiaryLikeCommandService diaryLikeCommandService;

    @Operation(summary = "특정 공유일기 좋아요", description = "특정 공유일기에 좋아요를 등록합니다. 이미 좋아요 누른 경우 409 에러가 반환됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "좋아요 권한 없음 (친구 관계 아님)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "이미 좋아요 누른 일기"),
    })
    @SecurityRequirement(name = "AccessToken")
    @PostMapping("/{diaryId}/likes")
    public ApiResponse<DiaryLikeResponse> addLike(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "좋아요할 일기 ID", required = true) @PathVariable Long diaryId
    ) {
        Long memberId = jwtPrincipal.memberId();
        DiaryLikeResponse response = diaryLikeCommandService.addLike(memberId, diaryId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "특정 공유일기 좋아요 취소", description = "특정 공유일기에 대한 좋아요를 취소합니다. 취소할 좋아요가 없는 경우 404 에러가 반환됩니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID 또는 취소할 좋아요 없음")
    })
    @SecurityRequirement(name = "AccessToken")
    @DeleteMapping("/{diaryId}/likes")
    public ApiResponse<DiaryLikeResponse> deleteLike(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "좋아요 취소할 일기 ID", required = true) @PathVariable Long diaryId
    ) {
        Long memberId = jwtPrincipal.memberId();
        DiaryLikeResponse response = diaryLikeCommandService.deleteLike(memberId, diaryId);
        return ApiResponse.success(response);
    }

}
