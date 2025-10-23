package com.nimble.payment_gateway.unit;

import com.nimble.payment_gateway.enums.ChargeStatus;
import com.nimble.payment_gateway.enums.PaymentMethod;
import com.nimble.payment_gateway.exception.BusinessException;
import com.nimble.payment_gateway.exception.InsufficientBalanceException;
import com.nimble.payment_gateway.model.dto.request.PaymentRequest;
import com.nimble.payment_gateway.model.dto.response.PaymentResponse;
import com.nimble.payment_gateway.model.entity.Charge;
import com.nimble.payment_gateway.model.entity.Transaction;
import com.nimble.payment_gateway.model.entity.User;
import com.nimble.payment_gateway.repository.ChargeRepository;
import com.nimble.payment_gateway.repository.TransactionRepository;
import com.nimble.payment_gateway.repository.UserRepository;
import com.nimble.payment_gateway.service.AuthorizerService;
import com.nimble.payment_gateway.service.ChargeService;
import com.nimble.payment_gateway.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ChargeService chargeService;

    @Mock
    private AuthorizerService authorizerService;

    @InjectMocks
    private PaymentService paymentService;

    private User payer;
    private User recipient;
    private Charge charge;
    private PaymentRequest paymentRequest;

    @BeforeEach
    void setUp() {
        payer = User.builder()
                .id(UUID.randomUUID())
                .name("Maria Santos")
                .cpf("98765432100")
                .email("maria@email.com")
                .balance(BigDecimal.valueOf(1000))
                .build();

        recipient = User.builder()
                .id(UUID.randomUUID())
                .name("João Silva")
                .cpf("12345678909")
                .email("joao@email.com")
                .balance(BigDecimal.ZERO)
                .build();

        charge = Charge.builder()
                .id(UUID.randomUUID())
                .originator(recipient)
                .recipient(payer)
                .amount(BigDecimal.valueOf(100))
                .status(ChargeStatus.PENDING)
                .build();

        paymentRequest = PaymentRequest.builder()
                .chargeId(charge.getId())
                .paymentMethod(PaymentMethod.BALANCE)
                .build();
    }

    @Test
    void shouldPayChargeWithBalanceSuccessfully() {
        when(chargeService.findById(charge.getId())).thenReturn(charge);
        when(userRepository.findByIdWithLock(payer.getId())).thenReturn(Optional.of(payer));
        when(userRepository.findByIdWithLock(recipient.getId())).thenReturn(Optional.of(recipient));

        Transaction savedTransaction = Transaction.builder()
                .id(UUID.randomUUID())
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

        PaymentResponse response = paymentService.payCharge(payer.getId(), paymentRequest);

        assertNotNull(response);
        assertEquals("APPROVED", response.getStatus());
        assertEquals(PaymentMethod.BALANCE, response.getPaymentMethod());
        verify(chargeRepository, times(1)).save(any(Charge.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenInsufficientBalance() {
        payer.setBalance(BigDecimal.valueOf(50));

        when(chargeService.findById(charge.getId())).thenReturn(charge);
        when(userRepository.findByIdWithLock(payer.getId())).thenReturn(Optional.of(payer));

        assertThrows(InsufficientBalanceException.class,
                () -> paymentService.payCharge(payer.getId(), paymentRequest));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void shouldThrowExceptionWhenChargeIsNotPending() {
        charge.setStatus(ChargeStatus.PAID);

        when(chargeService.findById(charge.getId())).thenReturn(charge);
        when(userRepository.findByIdWithLock(payer.getId())).thenReturn(Optional.of(payer));

        assertThrows(BusinessException.class,
                () -> paymentService.payCharge(payer.getId(), paymentRequest));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}