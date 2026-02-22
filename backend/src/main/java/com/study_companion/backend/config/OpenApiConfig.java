package com.study_companion.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Study Companion API",
        version = "1.0.0",
        description = "REST API for Study Companion - flashcard management application with multi-database architecture (PostgreSQL, MongoDB, Redis)"
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Development server"),
        @Server(url = "https://your-production-domain.com", description = "Production server (update URL when deployed)")
    }
)
public class OpenApiConfig {
    // SpringDoc will automatically configure Swagger UI and OpenAPI documentation
    // Access Swagger UI at: http://localhost:8080/swagger-ui.html
    // Access OpenAPI JSON at: http://localhost:8080/v3/api-docs
}