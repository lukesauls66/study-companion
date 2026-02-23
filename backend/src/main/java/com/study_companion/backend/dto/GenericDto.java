package com.study_companion.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class GenericDto {
    @Schema(description = "Generic error response")
        public record ErrorResponse(
                        @Schema(description = "Error message", example = "Access denied") String message,
                        @Schema(description = "Error flag", example = "true") boolean error) {
        }
}
