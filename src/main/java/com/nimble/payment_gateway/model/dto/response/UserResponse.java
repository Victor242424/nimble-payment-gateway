package com.nimble.payment_gateway.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados do usuário")
public class UserResponse {

    @Schema(description = "ID do usuário")
    private UUID id;

    @Schema(description = "Nome do usuário")
    private String name;

    @Schema(description = "CPF do usuário")
    private String cpf;

    @Schema(description = "Email do usuário")
    private String email;

    @Schema(description = "Saldo disponível")
    private BigDecimal balance;

    @Schema(description = "Data de criação")
    private LocalDateTime createdAt;
}