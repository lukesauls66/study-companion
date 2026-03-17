package com.study_companion.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Study Companion API", version = "1.0.0", description = "REST API for Study Companion"), servers = {
        @Server(url = "http://localhost:8080", description = "Development server"),
        @Server(url = "https://your-production-domain.com", description = "Production server (update URL when deployed)")
})
public class OpenApiConfig {
    // Access Swagger UI at: http://localhost:8080/swagger-ui/index.html
    // Access OpenAPI JSON at: http://localhost:8080/v3/api-docs
}