package com.mutantes.mutant_detector.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@Schema(name = "ErrorResponse", description = "Estructura estandarizada para errores de la API")
public class ErrorResponse {
    @Schema(description = "Nombre o tipo del error", example = "Validation Error")
    private String error;
    @Schema(description = "Mensaje legible para el usuario", example = "El campo dna no puede estar vacío")
    private String message;
    @Schema(description = "Código HTTP devuelto", example = "400")
    private int status;
    @Schema(description = "Ruta donde ocurrió el error", example = "/mutant")
    private String path;
    @Schema(description = "Fecha y hora del error", example = "2025-11-24 14:55:11")
    private LocalDateTime timestamp;
}
