package com.nimble.payment_gateway.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de erro")
public class ErrorResponse {

    @Schema(description = "Timestamp do erro")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Schema(description = "Código de status HTTP")
    private int status;

    @Schema(description = "Mensagem de erro")
    private String message;

    @Schema(description = "Detalhes do erro")
    private String details;

    @Schema(description = "Lista de erros de validação")
    private List<ValidationError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }
}