package com.cotato.itda.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@OpenAPIDefinition(
    servers = {
        @Server(url = "http://localhost:8080", description = "Local")
        // @Server(url = "https://api.simtok.com", description = "Production")
    },
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
@Profile({"default", "dev", "staging", "swagger"})
@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("ITDA 프로젝트 API 명세서")
                .description("ITDA API 명세서입니다.")
                .version("1.0.0");
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
}

