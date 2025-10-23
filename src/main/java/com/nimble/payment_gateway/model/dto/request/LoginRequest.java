package com.nimble.payment_gateway.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para login")
public class LoginRequest {

    @NotBlank(message = "CPF ou email é obrigatório")
    @Schema(description = "CPF (11 dígitos) ou email do usuário", example = "12345678901")
    private String identifier;

    @NotBlank(message = "Senha é obrigatória")
    @Schema(description = "Senha do usuário", example = "senha123")
    private String password;
}