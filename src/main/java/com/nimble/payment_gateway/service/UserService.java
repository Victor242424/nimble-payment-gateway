package com.nimble.payment_gateway.service;

import com.nimble.payment_gateway.exception.BusinessException;
import com.nimble.payment_gateway.exception.InvalidCpfException;
import com.nimble.payment_gateway.exception.ResourceNotFoundException;
import com.nimble.payment_gateway.model.dto.request.UserRegistrationRequest;
import com.nimble.payment_gateway.model.dto.response.UserResponse;
import com.nimble.payment_gateway.model.entity.User;
import com.nimble.payment_gateway.repository.UserRepository;
import com.nimble.payment_gateway.util.CpfValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse registerUser(UserRegistrationRequest request) {
        log.info("Registrando novo usuário com CPF: {}", request.getCpf());

        boolean test = CpfValidator.isValid(request.getCpf());

        if (!CpfValidator.isValid(request.getCpf())) {
            throw new InvalidCpfException("CPF inválido");
        }

        if (userRepository.existsByCpf(request.getCpf())) {
            throw new BusinessException("CPF já cadastrado");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email já cadastrado");
        }

        User user = User.builder()
                .name(request.getName())
                .cpf(request.getCpf())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ADMIN")
                .balance(BigDecimal.ZERO)
                .build();

        user = userRepository.save(user);
        log.info("Usuário registrado com sucesso: {}", user.getId());

        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public User findByCpf(String cpf) {
        return userRepository.findByCpf(cpf)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public User findByIdentifier(String identifier) {
        if (identifier.matches("\\d{11}")) {
            return findByCpf(identifier);
        } else {
            return findByEmail(identifier);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(UUID userId) {
        User user = findById(userId);
        return mapToResponse(user);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(UUID userId) {
        User user = findById(userId);
        return user.getBalance();
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .cpf(user.getCpf())
                .email(user.getEmail())
                .balance(user.getBalance())
                .createdAt(user.getCreatedAt())
                .build();
    }
}