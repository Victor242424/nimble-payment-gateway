package com.nimble.payment_gateway.controller;

import com.nimble.payment_gateway.enums.ChargeStatus;
import com.nimble.payment_gateway.model.dto.request.CreateChargeRequest;
import com.nimble.payment_gateway.model.dto.response.ChargeResponse;
import com.nimble.payment_gateway.security.UserPrincipal;
import com.nimble.payment_gateway.service.ChargeService;
import com.nimble.payment_gateway.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/charges")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Cobranças", description = "Endpoints de gestão de cobranças")
public class ChargeController {

    private final ChargeService chargeService;
    private final PaymentService paymentService;

    @PostMapping
    @Operation(summary = "Criar cobrança", description = "Cria uma nova cobrança para outro usuário")
    public ResponseEntity<ChargeResponse> createCharge(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateChargeRequest request) {
        ChargeResponse response = chargeService.createCharge(userPrincipal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/sent")
    @Operation(summary = "Cobranças enviadas", description = "Lista cobranças criadas pelo usuário")
    public ResponseEntity<List<ChargeResponse>> getSentCharges(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Filtrar por status") @RequestParam(required = false) ChargeStatus status) {
        List<ChargeResponse> charges = chargeService.getSentCharges(userPrincipal.getId(), status);
        return ResponseEntity.ok(charges);
    }

    @GetMapping("/received")
    @Operation(summary = "Cobranças recebidas", description = "Lista cobranças recebidas pelo usuário")
    public ResponseEntity<List<ChargeResponse>> getReceivedCharges(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Filtrar por status") @RequestParam(required = false) ChargeStatus status) {
        List<ChargeResponse> charges = chargeService.getReceivedCharges(userPrincipal.getId(), status);
        return ResponseEntity.ok(charges);
    }

    @DeleteMapping("/{chargeId}")
    @Operation(summary = "Cancelar cobrança", description = "Cancela uma cobrança (pendente ou paga)")
    public ResponseEntity<Map<String, String>> cancelCharge(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable UUID chargeId) {

        // Busca a cobrança para verificar se está paga
        var charge = chargeService.findById(chargeId);

        if (charge.isPaid()) {
            paymentService.cancelPaidCharge(chargeId, userPrincipal.getId());
        } else {
            chargeService.cancelCharge(chargeId, userPrincipal.getId());
        }

        return ResponseEntity.ok(Map.of("message", "Cobrança cancelada com sucesso"));
    }
}