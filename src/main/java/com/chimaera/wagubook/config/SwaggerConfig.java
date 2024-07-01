package com.chimaera.wagubook.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .version("v1.0.0")
                .title("Wagu Book Document")
                .description("크래프톤 정글 5기 3조 키메라팀");

        return new OpenAPI()
                .info(info);
    }
}
