package com.cotato.itda;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 서버 상태 체크 API
 */
@Tag(name = "Health Check", description = "서버 상태 확인 API")
@RestController
public class HealthCheckController {

    @Operation(summary = "서버 작동 확인", description = "서버가 정상적으로 실행 중인지 확인합니다.")
    @GetMapping("/health")
    public String healthCheck() {
        return "Server is running";
    }
}