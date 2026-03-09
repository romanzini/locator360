package com.familylocator.api.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI familyLocatorOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("Family Locator API")
            .description("Backend API for Family Locator")
            .version("v1")
            .contact(new Contact().name("Family Locator Team")));
  }
}
