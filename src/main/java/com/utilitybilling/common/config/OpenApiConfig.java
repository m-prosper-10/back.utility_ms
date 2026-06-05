package com.utilitybilling.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI utilityBillingOpenAPI() {
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes(
                    BEARER_SCHEME,
                    new SecurityScheme()
                        .name(BEARER_SCHEME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            )
            .info(new Info()
                .title("Utility Billing System API")
                .description("Backend API for utility billing operations")
                .version("1.0.0"));
    }
}
