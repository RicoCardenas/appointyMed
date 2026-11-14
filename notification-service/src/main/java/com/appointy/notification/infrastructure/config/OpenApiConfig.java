package com.appointy.notification.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentacion OpenAPI (RNF-11). Solo se documenta /api/v1; /internal es servicio a servicio y no se publica. */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title("Appointy Med: notification-service").version("v1").description("Correos de citas, recordatorios y reintentos. Errores con estructura uniforme: codigo, mensaje, detalle y timestamp."))
                .components(new Components().addSecuritySchemes("bearer", new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearer"));
    }
}
