package com.itbaizhan.travel.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI travelOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("travel API")
                .description("去哪旅游网前后端分离接口")
                .version("v1"));
    }
}
