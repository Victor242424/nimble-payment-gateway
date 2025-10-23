package com.nimble.payment_gateway.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Resposta de autenticação")
public class AuthResponse {

    @Schema(description = "Token JWT")
    private String token;

    @Schema(description = "Tipo do token", example = "Bearer")
    @Builder.Default
    private String type = "Bearer";

    @Schema(description = "Dados do usuário")
    private UserResponse user;
}