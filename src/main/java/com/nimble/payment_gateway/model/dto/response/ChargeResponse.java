package com.nimble.payment_gateway.model.dto.response;

import com.nimble.payment_gateway.enums.ChargeStatus;
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
@Schema(description = "Dados da cobrança")
public class ChargeResponse {

    @Schema(description = "ID da cobrança")
    private UUID id;

    @Schema(description = "Dados do originador")
    private UserSummary originator;

    @Schema(description = "Dados do destinatário")
    private UserSummary recipient;

    @Schema(description = "Valor da cobrança")
    private BigDecimal amount;

    @Schema(description = "Descrição da cobrança")
    private String description;

    @Schema(description = "Status da cobrança")
    private ChargeStatus status;

    @Schema(description = "Data de criação")
    private LocalDateTime createdAt;

    @Schema(description = "Data de atualização")
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSummary {
        private UUID id;
        private String name;
        private String cpf;
    }
}