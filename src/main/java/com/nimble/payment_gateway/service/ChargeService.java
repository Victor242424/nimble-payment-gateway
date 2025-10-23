package com.nimble.payment_gateway.service;

import com.nimble.payment_gateway.enums.ChargeStatus;
import com.nimble.payment_gateway.exception.BusinessException;
import com.nimble.payment_gateway.exception.ResourceNotFoundException;
import com.nimble.payment_gateway.model.dto.request.CreateChargeRequest;
import com.nimble.payment_gateway.model.dto.response.ChargeResponse;
import com.nimble.payment_gateway.model.entity.Charge;
import com.nimble.payment_gateway.model.entity.User;
import com.nimble.payment_gateway.repository.ChargeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargeService {

    private final ChargeRepository chargeRepository;
    private final UserService userService;

    @Transactional
    public ChargeResponse createCharge(UUID originatorId, CreateChargeRequest request) {
        log.info("Criando cobrança para CPF: {}", request.getRecipientCpf());

        User originator = userService.findById(originatorId);
        User recipient = userService.findByCpf(request.getRecipientCpf());

        if (originator.getId().equals(recipient.getId())) {
            throw new BusinessException("Não é possível criar cobrança para si mesmo");
        }

        Charge charge = Charge.builder()
                .originator(originator)
                .recipient(recipient)
                .amount(request.getAmount())
                .description(request.getDescription())
                .status(ChargeStatus.PENDING)
                .build();

        charge = chargeRepository.save(charge);
        log.info("Cobrança criada com sucesso: {}", charge.getId());

        return mapToResponse(charge);
    }

    @Transactional(readOnly = true)
    public Charge findById(UUID chargeId) {
        return chargeRepository.findById(chargeId)
                .orElseThrow(() -> new ResourceNotFoundException("Cobrança não encontrada"));
    }

    @Transactional(readOnly = true)
    public List<ChargeResponse> getSentCharges(UUID userId, ChargeStatus status) {
        List<Charge> charges = status != null
                ? chargeRepository.findByOriginatorIdAndStatus(userId, status)
                : chargeRepository.findByOriginatorId(userId);

        return charges.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ChargeResponse> getReceivedCharges(UUID userId, ChargeStatus status) {
        List<Charge> charges = status != null
                ? chargeRepository.findByRecipientIdAndStatus(userId, status)
                : chargeRepository.findByRecipientId(userId);

        return charges.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void cancelCharge(UUID chargeId, UUID userId) {
        log.info("Cancelando cobrança: {}", chargeId);

        Charge charge = findById(chargeId);

        if (!charge.getOriginator().getId().equals(userId) &&
                !charge.getRecipient().getId().equals(userId)) {
            throw new BusinessException("Você não tem permissão para cancelar esta cobrança");
        }

        if (charge.isCancelled()) {
            throw new BusinessException("Cobrança já está cancelada");
        }

        charge.markAsCancelled();
        chargeRepository.save(charge);
        log.info("Cobrança cancelada: {}", chargeId);
    }

    private ChargeResponse mapToResponse(Charge charge) {
        return ChargeResponse.builder()
                .id(charge.getId())
                .originator(ChargeResponse.UserSummary.builder()
                        .id(charge.getOriginator().getId())
                        .name(charge.getOriginator().getName())
                        .cpf(charge.getOriginator().getCpf())
                        .build())
                .recipient(ChargeResponse.UserSummary.builder()
                        .id(charge.getRecipient().getId())
                        .name(charge.getRecipient().getName())
                        .cpf(charge.getRecipient().getCpf())
                        .build())
                .amount(charge.getAmount())
                .description(charge.getDescription())
                .status(charge.getStatus())
                .createdAt(charge.getCreatedAt())
                .updatedAt(charge.getUpdatedAt())
                .build();
    }
}