package com.nimble.payment_gateway.unit;

import com.nimble.payment_gateway.exception.BusinessException;
import com.nimble.payment_gateway.exception.InvalidCpfException;
import com.nimble.payment_gateway.model.dto.request.UserRegistrationRequest;
import com.nimble.payment_gateway.model.dto.response.UserResponse;
import com.nimble.payment_gateway.model.entity.User;
import com.nimble.payment_gateway.repository.UserRepository;
import com.nimble.payment_gateway.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserRegistrationRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = UserRegistrationRequest.builder()
                .name("João Silva")
                .cpf("12345678909")
                .email("joao@email.com")
                .password("senha123")
                .build();
    }

    @Test
    void shouldRegisterUserSuccessfully() {
        when(userRepository.existsByCpf(validRequest.getCpf())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("hashedPassword");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .name(validRequest.getName())
                .cpf(validRequest.getCpf())
                .email(validRequest.getEmail())
                .password("hashedPassword")
                .balance(BigDecimal.ZERO)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        UserResponse response = userService.registerUser(validRequest);

        assertNotNull(response);
        assertEquals(validRequest.getName(), response.getName());
        assertEquals(validRequest.getCpf(), response.getCpf());
        assertEquals(validRequest.getEmail(), response.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCpfIsInvalid() {
        validRequest.setCpf("12345678900");

        assertThrows(InvalidCpfException.class, () -> userService.registerUser(validRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenCpfAlreadyExists() {
        when(userRepository.existsByCpf(validRequest.getCpf())).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.registerUser(validRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        when(userRepository.existsByCpf(validRequest.getCpf())).thenReturn(false);
        when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

        assertThrows(BusinessException.class, () -> userService.registerUser(validRequest));
        verify(userRepository, never()).save(any(User.class));
    }
}
