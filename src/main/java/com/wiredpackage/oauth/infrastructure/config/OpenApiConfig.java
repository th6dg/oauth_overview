package com.wiredpackage.oauth.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String securitySchemeKey = "Bearer";

    @Bean
    public OpenAPI accountsApi() {
        return new OpenAPI()
            .info(new Info().title("Taopass OAuth2 API"))
            .components(
                new Components().addSecuritySchemes(securitySchemeKey,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                ))
            .addSecurityItem(
                new SecurityRequirement().addList(securitySchemeKey));
    }
}
