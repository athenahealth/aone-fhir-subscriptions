package com.athenahealth.eventing.partner.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")
public class OpenApiConfiguration {

  @Bean
  public OpenAPI aarOpenApi(@Value("${springdoc.version}") String appVersion) {
    final String securitySchemeName = "bearerAuth";
    return new OpenAPI()
        .info(buildInfo(appVersion))
        .components(new Components().addSecuritySchemes("bearerAuth", buildHTTPSecurityScheme()))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
  }

  private SecurityScheme buildHTTPSecurityScheme() {
    return new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT");
  }

  private Info buildInfo(String appVersion) {
    return new Info()
        .title("Stub Webhook Service")
        .version(appVersion)
        .description("Stub Service to simulate MDP partner webhook")
        .version(appVersion);
  }
}

