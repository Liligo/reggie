package com.liligo.reggie.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("瑞吉外卖 API 文档")
                        .version("1.0")
                        .description("瑞吉外卖项目的 RESTful API 文档")
                        .contact(new Contact()
                                .name("Liligo")
                                .url("https://github.com/Liligo/reggie")
                        )
                );
    }
}