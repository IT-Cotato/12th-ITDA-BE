package com.cotato.itda.global.config;

import java.util.Map;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;

@OpenAPIDefinition(
    info = @Info(
        title = "ITDA API DOCS",
        version = "v1",
        description = "ITDA Service Public API Documentation",
        license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Local"),
        @Server(url = "https://43.202.184.232.nip.io", description = "Production")
    },
    // ✅ 전역 기본 보안(원하면 아래 줄 삭제)
    security = @SecurityRequirement(name = "AccessToken")
)
@SecurityScheme(
    name = "AccessToken",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "일반 API 접근용 Access JWT"
)
@SecurityScheme(
    name = "RefreshToken",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "토큰 재발급 전용 Refresh JWT"
)
@Profile({"default", "dev", "staging", "swagger","local","prod"})
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
            .group("Auth")
            .displayName("Auth (인증/인가)")
            .packagesToScan("com.cotato.itda.domain.auth.controller")
            .pathsToMatch("/api/auth/**")
            .build();
    }

    @Bean
    public GroupedOpenApi signupApi() {
        return GroupedOpenApi.builder()
            .group("Signup")
            .displayName("Signup API")
            .packagesToScan("com.cotato.itda.domain.signup.controller")
            .pathsToMatch("/api/signup/**")
            .build();
    }
    @Bean
    public GroupedOpenApi MemberApi() {
        return GroupedOpenApi.builder()
            .group("Member")
            .displayName("Member API")
            .packagesToScan("com.cotato.itda.domain.member.controller")
            .pathsToMatch("/api/member/**")
            .build();
    }

    @Bean
    public GroupedOpenApi FriendshipApi() {
        return GroupedOpenApi.builder()
                .group("Friendship")
                .displayName("Friendship API")
                .packagesToScan("com.cotato.itda.domain.friendship.controller")
                .pathsToMatch("/api/friendships/**")
                .build();
    }
  
    @Bean
    public GroupedOpenApi ProfileApi() {
        return GroupedOpenApi.builder()
                .group("Profile")
                .displayName("Profile API")
                .packagesToScan("com.cotato.itda.domain.profile.controller")
                .pathsToMatch("/api/profile/**")
                .build();
    }

    @Bean
    public GroupedOpenApi ChatTopicApi() {
        return GroupedOpenApi.builder()
                .group("ChatTopic")
                .displayName("ChatTopic API")
                .packagesToScan("com.cotato.itda.domain.chattopic.controller")
                .pathsToMatch("/api/chat-topics/**")
                .build();
    }
  
    @Bean
    public GroupedOpenApi ImageApi() {
        return GroupedOpenApi.builder()
                .group("Image")
                .displayName("Image API")
                .packagesToScan("com.cotato.itda.domain.image.controller")
                .pathsToMatch("/api/image/**")
                .build();
    }

    @Bean
    public GroupedOpenApi DiaryApi() {
        return GroupedOpenApi.builder()
                .group("Diary")
                .displayName("Diary API")
                .packagesToScan("com.cotato.itda.domain.diary.controller")
                .pathsToMatch("/api/diaries/**")
                .build();
    }

    // === 공통 에러 응답 자동 추가 (선택) ===
    @Bean
    public OperationCustomizer addGlobalResponses() {
        return (operation, handlerMethod) -> {
            ApiResponse error = new ApiResponse()
                .description("공통 에러 응답")
                .content(new Content().addMediaType(
                    "application/json",
                    new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                ));
            operation.getResponses().putIfAbsent("400", error);
            operation.getResponses().putIfAbsent("500", error);
            return operation;
        };
    }

    // === 전역 스키마 등록 (ErrorResponse/DataResponse) (선택) ===
    @Bean
    public OpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            Schema<?> errorSchema = new ObjectSchema()
                .addProperty("status", new StringSchema()
                    .description("HTTP 상태 구분 문자열 (예: Bad Request, Not Found)")
                    .example("Bad Request"))
                .addProperty("timestamp", new StringSchema()
                    .description("응답 발생 시각 (RFC3339 포맷, Asia/Seoul 기준)")
                    .example("2026-01-07T12:34:56.789+09:00"))
                .addProperty("message", new StringSchema()
                    .description("에러 메시지 (사용자 피드백용)")
                    .example("요청이 올바르지 않습니다."))
                .addProperty("code", new StringSchema()
                    .description("도메인 별 세부 오류 코드")
                    .example("COMMON_ERROR_400_BAD_REQUEST"))
                .addProperty("reasons", new ObjectSchema()
                    .description("필드 검증 실패 등의 상세 사유 (Key-Value 구조)")
                    .example(Map.of(
                        "phoneNumber", "전화번호 형식이 올바르지 않습니다.",
                        "password", "비밀번호는 8자 이상이어야 합니다."
                    )));

            Schema<?> dataSchema = new ObjectSchema()
                .addProperty("status", new StringSchema()
                    .description("HTTP 상태 구분 문자열 (예: OK, Created)")
                    .example("OK"))
                .addProperty("timestamp", new StringSchema()
                    .description("응답 발생 시각 (RFC3339 포맷, Asia/Seoul 기준)")
                    .example("2026-01-07T14:25:01.123+09:00"))
                .addProperty("data", new ObjectSchema()
                    .description("응답 데이터 (도메인별 DTO 구조)"));

            if (openApi.getComponents() == null) {
                openApi.setComponents(new io.swagger.v3.oas.models.Components());
            }
            openApi.getComponents()
                .addSchemas("ErrorResponse", errorSchema)
                .addSchemas("DataResponse", dataSchema);
        };
    }
}
