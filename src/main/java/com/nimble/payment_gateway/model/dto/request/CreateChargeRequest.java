package com.nimble.payment_gateway.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criação de cobrança")
public class CreateChargeRequest {

    @NotBlank(message = "CPF do destinatário é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter exatamente 11 dígitos")
    @Schema(description = "CPF do destinatário da cobrança", example = "98765432100")
    private String recipientCpf;

    @NotNull(message = "Valor é obrigatório")
    @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
    @Digits(integer = 17, fraction = 2, message = "Valor inválido")
    @Schema(description = "Valor da cobrança", example = "100.50")
    private BigDecimal amount;

    @Size(max = 500, message = "Descrição deve ter no máximo 500 caracteres")
    @Schema(description = "Descrição opcional da cobrança", example = "Pagamento de serviço")
    private String description;
}