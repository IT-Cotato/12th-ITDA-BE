package com.cotato.itda.domain.image.controller;

import com.cotato.itda.domain.image.dto.request.PresignedUrlRequest;
import com.cotato.itda.domain.image.dto.response.PresignedUrlResponse;
import com.cotato.itda.domain.image.service.S3Service;
import com.cotato.itda.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image")
@Tag(name = "Image", description = "이미지 업로드 및 관리 API")

public class S3Controller {

    private final S3Service s3Service;

    /**
     * 이미지 업로드를 위한 presigned URL 발급
     */
    @Operation(
            summary = "S3 업로드용 Presigned URL 생성",
            description = "클라이언트가 S3에 직접 업로드할 수 있는 Presigned URL을 발급합니다. "
            + "업로드 시 PUT 메서드로 요청해야 하며, 요청 헤더에 응답으로 반환된 Content-Type을 포함해야 합니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 S3 폴더명 또는 파일명 또는 확장자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "S3 서버 통신 오류")
    })
    @PostMapping("presigned-url")
    public ApiResponse<PresignedUrlResponse> getPresignedUrl(@Valid @RequestBody PresignedUrlRequest request) {
        PresignedUrlResponse response = s3Service.getPresignedUrl(request.folder(), request.fileName());
        return ApiResponse.success(response);
    }
}
