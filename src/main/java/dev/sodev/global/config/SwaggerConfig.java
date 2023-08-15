package dev.sodev.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@OpenAPIDefinition(
        info = @Info(
                title = "Sodev 프로젝트 API 명세서",
                description = "Sodev 프로젝트에 사용되는 API 명세서",
                version = "v1"
        )
)
@Configuration
public class SwaggerConfig {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer";
    public static final String BEARER_FORMAT = "JWT";

    @Bean
    public OpenAPI openAPI() {
        String jwtSchemeName = AUTHORIZATION_HEADER;
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme(BEARER_PREFIX)
                        .bearerFormat(BEARER_FORMAT));

        // Swagger UI 접속 후, 딱 한 번만 accessToken을 입력해주면 모든 API에 토큰 인증 작업이 적용됩니다.
        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components);
    }

}