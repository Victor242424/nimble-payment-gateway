package com.nimble.payment_gateway.controller;

import com.nimble.payment_gateway.model.dto.response.UserResponse;
import com.nimble.payment_gateway.security.UserPrincipal;
import com.nimble.payment_gateway.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Usuários", description = "Endpoints de gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Perfil do usuário", description = "Retorna informações do usuário autenticado")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserResponse response = userService.getUserProfile(userPrincipal.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/balance")
    @Operation(summary = "Consultar saldo", description = "Retorna o saldo disponível do usuário")
    public ResponseEntity<Map<String, BigDecimal>> getBalance(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        BigDecimal balance = userService.getBalance(userPrincipal.getId());
        return ResponseEntity.ok(Map.of("balance", balance));
    }
}