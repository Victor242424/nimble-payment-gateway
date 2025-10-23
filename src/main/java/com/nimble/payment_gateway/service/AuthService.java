package com.nimble.payment_gateway.service;

import com.nimble.payment_gateway.model.dto.request.LoginRequest;
import com.nimble.payment_gateway.model.dto.response.AuthResponse;
import com.nimble.payment_gateway.model.dto.response.UserResponse;
import com.nimble.payment_gateway.model.entity.User;
import com.nimble.payment_gateway.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Tentativa de login para: {}", request.getIdentifier());

        debugUser(request.getIdentifier(), request.getPassword());
        AuthResponse authResponse = loginManual(request);
        return authResponse;
    }

    public  AuthResponse autoLogin(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        User user = userService.findByIdentifier(request.getIdentifier());

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .cpf(user.getCpf())
                .email(user.getEmail())
                .balance(user.getBalance())
                .createdAt(user.getCreatedAt())
                .build();

        log.info("Login realizado com sucesso para: {}", user.getCpf());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .build();
    }

    // OPCIÓN 2: Si prefieres verificar manualmente primero (sin AuthenticationManager)
    public AuthResponse loginManual(LoginRequest request) {
        log.info("Tentativa de login manual para: {}", request.getIdentifier());

        // 1. Verificar si el usuario existe
        User user = userService.findByIdentifier(request.getIdentifier());

        // 2. Verificar contraseña manualmente
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.error("Senha incorreta para: {}", request.getIdentifier());
            throw new BadCredentialsException("CPF/Email ou senha incorretos");
        }

        // 3. Crear autenticación manual (si no usas AuthenticationManager)
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getCpf(),
                request.getPassword()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .cpf(user.getCpf())
                .email(user.getEmail())
                .balance(user.getBalance())
                .createdAt(user.getCreatedAt())
                .build();

        log.info("Login manual realizado com sucesso para: {}", user.getCpf());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .build();
    }

    @Transactional(readOnly = true)
    public void debugUser(String identifier, String rawPassword) {
        try {
            User user = userService.findByIdentifier(identifier);
            log.info("=== DEBUG USER ===");
            log.info("Usuario encontrado: {}", user.getCpf());
            log.info("Email: {}", user.getEmail());
            log.info("Password en DB: {}", user.getPassword());
            log.info("Password raw: {}", rawPassword);
            log.info("Matches: {}", passwordEncoder.matches(rawPassword, user.getPassword()));
            log.info("=== END DEBUG ===");
        } catch (Exception e) {
            log.error("Error en debug: {}", e.getMessage());
        }
    }
}