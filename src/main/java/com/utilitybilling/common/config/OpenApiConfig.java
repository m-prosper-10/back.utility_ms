package com.utilitybilling.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI utilityBillingOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Utility Billing System API")
                .description("Backend API for utility billing operations")
                .version("1.0.0"));
    }
}
