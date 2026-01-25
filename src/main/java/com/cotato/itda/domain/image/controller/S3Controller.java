package com.cotato.itda.domain.image.controller;

import com.cotato.itda.domain.image.dto.request.PresignedUrlRequest;
import com.cotato.itda.domain.image.dto.response.PresignedUrlResponse;
import com.cotato.itda.domain.image.service.S3Service;
import com.cotato.itda.global.common.response.ApiResponse;
import com.cotato.itda.global.security.jwt.principal.JwtPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/image")
@Tag(name = "Image", description = "S3 이미지 업로드 및 관리 API")

public class S3Controller {

    private final S3Service s3Service;

    /**
     * 이미지 업로드를 위한 presigned URL 발급
     */
    @Operation(
            summary = "S3 업로드용 Presigned URL 생성",
            description = """
                    클라이언트가 이미지를 S3에 직접 업로드하기 위한 Presigned URL을 발급합니다. 
                    - **요청 방식**: 이 API에서 응답받은 presignedUrl로 **PUT** 요청을 보내야 합니다.
                    - **필수 헤더**: 이미지 업로드 요청 시 Content-Type 헤더에 이 API에서 응답받은 contentType을 포함해야 합니다.
                    - **저장용 경로 (imageUrl)**: S3 업로드 후, imageUrl 값을 사용해 서버에 다른 저장(프로필 이미지 등록 등) 요청을 보냅니다.
                    """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "유효하지 않은 S3 폴더명 또는 파일명 또는 확장자"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "S3 서버 통신 오류")
    })
    @PostMapping("presigned-url")
    public ApiResponse<PresignedUrlResponse> getPresignedUrl(
            @Parameter(hidden = true) @AuthenticationPrincipal JwtPrincipal jwtPrincipal,
            @Valid @RequestBody PresignedUrlRequest request) {

        PresignedUrlResponse response = s3Service.getPresignedUrl(request.folder(), request.fileName());
        return ApiResponse.success(response);
    }
}
