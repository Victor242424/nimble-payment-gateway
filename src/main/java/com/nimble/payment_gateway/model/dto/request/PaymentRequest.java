package com.nimble.payment_gateway.model.dto.request;

import com.nimble.payment_gateway.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para pagamento de cobrança")
public class PaymentRequest {

    @NotNull(message = "ID da cobrança é obrigatório")
    @Schema(description = "ID da cobrança a ser paga")
    private UUID chargeId;

    @NotNull(message = "Método de pagamento é obrigatório")
    @Schema(description = "Método de pagamento", example = "BALANCE")
    private PaymentMethod paymentMethod;

    @Schema(description = "Dados do cartão (obrigatório se paymentMethod for CREDIT_CARD)")
    private CreditCardData creditCard;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreditCardData {
        @Schema(description = "Número do cartão", example = "4111111111111111")
        private String cardNumber;

        @Schema(description = "Data de expiração (MM/YY)", example = "12/25")
        private String expirationDate;

        @Schema(description = "CVV", example = "123")
        private String cvv;
    }
}