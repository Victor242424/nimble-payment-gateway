package com.nimble.payment_gateway.unit;

import com.nimble.payment_gateway.enums.ChargeStatus;
import com.nimble.payment_gateway.exception.BusinessException;
import com.nimble.payment_gateway.model.dto.request.CreateChargeRequest;
import com.nimble.payment_gateway.model.dto.response.ChargeResponse;
import com.nimble.payment_gateway.model.entity.Charge;
import com.nimble.payment_gateway.model.entity.User;
import com.nimble.payment_gateway.repository.ChargeRepository;
import com.nimble.payment_gateway.service.ChargeService;
import com.nimble.payment_gateway.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChargeServiceTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ChargeService chargeService;

    private User originator;
    private User recipient;
    private CreateChargeRequest request;

    @BeforeEach
    void setUp() {
        originator = User.builder()
                .id(UUID.randomUUID())
                .name("João Silva")
                .cpf("12345678909")
                .email("joao@email.com")
                .balance(BigDecimal.valueOf(1000))
                .build();

        recipient = User.builder()
                .id(UUID.randomUUID())
                .name("Maria Santos")
                .cpf("98765432100")
                .email("maria@email.com")
                .balance(BigDecimal.ZERO)
                .build();

        request = CreateChargeRequest.builder()
                .recipientCpf(recipient.getCpf())
                .amount(BigDecimal.valueOf(100))
                .description("Pagamento de serviço")
                .build();
    }

    @Test
    void shouldCreateChargeSuccessfully() {
        when(userService.findById(originator.getId())).thenReturn(originator);
        when(userService.findByCpf(request.getRecipientCpf())).thenReturn(recipient);

        Charge savedCharge = Charge.builder()
                .id(UUID.randomUUID())
                .originator(originator)
                .recipient(recipient)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(ChargeStatus.PENDING)
                .build();

        when(chargeRepository.save(any(Charge.class))).thenReturn(savedCharge);

        ChargeResponse response = chargeService.createCharge(originator.getId(), request);

        assertNotNull(response);
        assertEquals(request.getAmount(), response.getAmount());
        assertEquals(ChargeStatus.PENDING, response.getStatus());
        verify(chargeRepository, times(1)).save(any(Charge.class));
    }

    @Test
    void shouldThrowExceptionWhenCreatingChargeForSelf() {
        when(userService.findById(originator.getId())).thenReturn(originator);
        when(userService.findByCpf(request.getRecipientCpf())).thenReturn(originator);

        assertThrows(BusinessException.class,
                () -> chargeService.createCharge(originator.getId(), request));
        verify(chargeRepository, never()).save(any(Charge.class));
    }
}
