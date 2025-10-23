package com.nimble.payment_gateway.model.dto.response;

import com.nimble.payment_gateway.enums.PaymentMethod;
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
@Schema(description = "Resposta de pagamento")
public class PaymentResponse {

    @Schema(description = "ID da transação")
    private UUID transactionId;

    @Schema(description = "ID da cobrança")
    private UUID chargeId;

    @Schema(description = "Valor pago")
    private BigDecimal amount;

    @Schema(description = "Método de pagamento")
    private PaymentMethod paymentMethod;

    @Schema(description = "Status do pagamento")
    private String status;

    @Schema(description = "Mensagem")
    private String message;

    @Schema(description = "Data da transação")
    private LocalDateTime transactionDate;
}