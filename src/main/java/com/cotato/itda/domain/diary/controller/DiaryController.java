package com.cotato.itda.domain.diary.controller;

import com.cotato.itda.domain.diary.dto.request.DiaryRequest;
import com.cotato.itda.domain.diary.dto.response.*;
import com.cotato.itda.domain.diary.service.command.DiaryCommandService;
import com.cotato.itda.domain.diary.service.query.DiaryQueryService;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/diaries")
@Tag(name = "Diary", description = "공유일기 API")
public class DiaryController {

    private final DiaryCommandService diaryCommandService;
    private final DiaryQueryService diaryQueryService;

    @Operation(summary = "공유일기 등록",
            description = """
                        새로운 일기를 등록합니다. 
                        - 일기 날짜(YYYY-MM-dd), 이모지 코드값, 내용, 사진 URL을 request body로 전달받아 새 일기를 생성합니다.
                        - request body에 일기 날짜를 포함하지 않는 경우 서버 기준 현재 날짜로 등록됩니다.
                        - 해당 날짜에 이미 작성된 일기가 존재하는 경우, 등록이 불가능합니다.
                        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력값 또는 존재하지 않는 이모지 코드"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "해당 날짜에 이미 작성된 일기 존재"),
    })
    @SecurityRequirement(name = "AccessToken")
    @PostMapping
    public ApiResponse<DiaryResponse> createDiary(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Valid @RequestBody DiaryRequest request
    ) {
        Long memberId = jwtPrincipal.memberId();

        LocalDate date = (request.date() != null) ? request.date() : LocalDate.now(ZoneId.of("Asia/Seoul"));

        DiaryResponse response = diaryCommandService.createDiary(memberId, request, date);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "공유일기 수정",
            description = """
                        기존에 작성된 일기를 수정합니다. 
                        - 변경되지 않은 필드도 기존 값을 포함하여 모든 필드를 보내야 합니다.
                        - 작성자 본인만 수정 가능합니다. 
                        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 입력값 또는 존재하지 않는 이모지 코드"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (작성자만 수정 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @PutMapping("/{diaryId}")
    public ApiResponse<DiaryResponse> updateDiary(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "수정할 일기 ID", required = true, example = "1") @PathVariable Long diaryId,
            @Valid @RequestBody DiaryRequest request
    ) {
        Long memberId = jwtPrincipal.memberId();

        DiaryResponse response = diaryCommandService.updateDiary(memberId, diaryId, request);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "공유일기 삭제",
            description = """
                        특정 일기를 삭제합니다. 
                        - 작성자 본인만 삭제 가능합니다. 
                        - DB에서 완전히 삭제되지 않고, 삭제 상태로 변경되어 조회되지 않습니다.
                        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "권한 없음 (작성자만 삭제 가능)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @DeleteMapping("/{diaryId}")
    public ApiResponse<Void> deleteDiary(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "삭제할 일기 ID", required = true, example = "1") @PathVariable Long diaryId
    ) {
        Long memberId = jwtPrincipal.memberId();

        diaryCommandService.softDeleteDiary(memberId, diaryId);
        return ApiResponse.success(null);
    }

    @Operation(
            summary = "공유일기 상세조회",
            description = """ 
                        특정 일기의 상세 정보를 조회합니다. 
                        - 일기 정보, 작성자 정보, 좋아요 여부가 반환됩니다. 
                        - 작성자가 본인이거나, 친구 관계인 경우에만 조회가 가능합니다.
                        """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "조회 권한 없음 (친구 관계 아님)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 일기 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @GetMapping("/{diaryId}")
    public ApiResponse<DiaryDetailResponse> getDiaryDetail(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "조회할 일기 ID", required = true, example = "1") @PathVariable Long diaryId
    ) {
        Long memberId = jwtPrincipal.memberId();

        DiaryDetailResponse response = diaryQueryService.getDiaryDetail(memberId, diaryId);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "공유일기 목록 조회",
            description = """ 
                    나와 친구가 작성한 공유일기 전체 목록을 무한스크롤로 조회합니다.
                    - 일기 목록은 최신순으로 반환됩니다.
                    - 일기 정보, 작성자 정보, 좋아요 여부, 페이징 정보(lastId, hasNext)가 반환됩니다.
                    - 첫 조회: lastId는 null로 요청합니다.
                    - 추가 조회: 응답의 hasNext가 true인 경우, 응답받은 lastId를 요청 파라미터로 포함해 다음 데이터를 요청합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 파라미터"),
    })
    @SecurityRequirement(name = "AccessToken")
    @GetMapping
    public ApiResponse<DiaryListResponse> getDiaryList(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Parameter(description = "직전 조회 응답의 lastId 값 (첫 조회 요청 시 null)") @RequestParam(required = false) Long lastId,
            @Parameter(description = "조회할 일기 개수 (기본값 5개)") @RequestParam(required = false, defaultValue = "5") int size
    ) {
        Long memberId = jwtPrincipal.memberId();

        DiaryListResponse response = diaryQueryService.getDiaryList(memberId, lastId, size);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "나의 월별 공유일기 목록 조회",
            description = """
                    내가 작성한 특정 월의 일기 목록을 조회합니다.
                    - 일기 ID, 날짜, 이모지 코드를 반환합니다.
                    - 연도 및 월이 null일 경우 현재 연도 및 월로 조회됩니다.
                    - 일기 날짜는 오름차순으로 정렬되어 반환됩니다.
                    - 작성자(나) 정보는 null로 반환됩니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 파라미터"),
    })
    @SecurityRequirement(name = "AccessToken")
    @GetMapping("/monthly/me")
    public ApiResponse<MonthlyDiaryListResponse> getMonthlyDiaryList(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,

            @Parameter(description = "조회 연도", example = "2026")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "조회 월", example = "1", schema = @Schema(minimum = "1", maximum = "12"))
            @RequestParam(required = false) Integer month
    ) {
        Long memberId = jwtPrincipal.memberId();

        if (year == null) {
            year = LocalDate.now(ZoneId.of("Asia/Seoul")).getYear();
        }
        if (month == null) {
            month = LocalDate.now(ZoneId.of("Asia/Seoul")).getMonthValue();
        }

        MonthlyDiaryListResponse response = diaryQueryService.getMonthlyDiaryList(memberId, year, month);
        return ApiResponse.success(response);
    }

    @Operation(
            summary = "친구 월별 공유일기 목록 조회",
            description = """
                    pathVariable로 전달받은 사용자의 월 일기 목록을 조회합니다.
                    - 작성자 정보, 일기 ID, 날짜, 이모지 코드를 반환합니다.
                    - 연도 및 월이 null일 경우 현재 연도 및 월로 조회됩니다.
                    - 일기 날짜는 오름차순(날짜순)으로 정렬되어 반환됩니다.
                    - 작성자가 친구 관계인 경우에만 조회가 가능합니다.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 요청 파라미터"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "조회 권한 없음 (친구 관계 아님)"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "존재하지 않는 멤버 ID"),
    })
    @SecurityRequirement(name = "AccessToken")
    @GetMapping("/monthly/{memberId}")
    public ApiResponse<MonthlyDiaryListResponse> getFriendMonthlyDiaryList(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,

            @Parameter(description = "조회할 친구 멤버 ID", required = true, example = "1")
            @PathVariable("memberId") Long targetMemberId,

            @Parameter(description = "조회 연도", example = "2026")
            @RequestParam(required = false) Integer year,

            @Parameter(description = "조회 월", example = "1", schema = @Schema(minimum = "1", maximum = "12"))
            @RequestParam(required = false) Integer month
    ) {
        Long memberId = jwtPrincipal.memberId();

        if (year == null) {
            year = LocalDate.now(ZoneId.of("Asia/Seoul")).getYear();
        }
        if (month == null) {
            month = LocalDate.now(ZoneId.of("Asia/Seoul")).getMonthValue();
        }

        MonthlyDiaryListResponse response = diaryQueryService.getFriendMonthlyDiaryList(memberId, targetMemberId, year, month);
        return ApiResponse.success(response);
    }

}
