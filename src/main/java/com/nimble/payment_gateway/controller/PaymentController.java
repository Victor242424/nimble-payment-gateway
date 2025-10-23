package com.nimble.payment_gateway.controller;

import com.nimble.payment_gateway.model.dto.request.DepositRequest;
import com.nimble.payment_gateway.model.dto.request.PaymentRequest;
import com.nimble.payment_gateway.model.dto.response.PaymentResponse;
import com.nimble.payment_gateway.security.UserPrincipal;
import com.nimble.payment_gateway.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Pagamentos", description = "Endpoints de pagamentos e depósitos")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/pay-charge")
    @Operation(summary = "Pagar cobrança", description = "Realiza o pagamento de uma cobrança")
    public ResponseEntity<PaymentResponse> payCharge(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse response = paymentService.payCharge(userPrincipal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/deposit")
    @Operation(summary = "Depositar saldo", description = "Adiciona saldo à conta do usuário")
    public ResponseEntity<PaymentResponse> deposit(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody DepositRequest request) {
        PaymentResponse response = paymentService.deposit(userPrincipal.getId(), request);
        return ResponseEntity.ok(response);
    }
}