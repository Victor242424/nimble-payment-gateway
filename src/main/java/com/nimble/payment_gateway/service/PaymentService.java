package com.nimble.payment_gateway.service;

import com.nimble.payment_gateway.enums.PaymentMethod;
import com.nimble.payment_gateway.enums.TransactionType;
import com.nimble.payment_gateway.exception.BusinessException;
import com.nimble.payment_gateway.exception.InsufficientBalanceException;
import com.nimble.payment_gateway.exception.PaymentAuthorizationException;
import com.nimble.payment_gateway.model.dto.request.DepositRequest;
import com.nimble.payment_gateway.model.dto.request.PaymentRequest;
import com.nimble.payment_gateway.model.dto.response.PaymentResponse;
import com.nimble.payment_gateway.model.entity.Charge;
import com.nimble.payment_gateway.model.entity.Transaction;
import com.nimble.payment_gateway.model.entity.User;
import com.nimble.payment_gateway.repository.ChargeRepository;
import com.nimble.payment_gateway.repository.TransactionRepository;
import com.nimble.payment_gateway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final ChargeRepository chargeRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ChargeService chargeService;
    private final AuthorizerService authorizerService;

    @Transactional
    public PaymentResponse payCharge(UUID payerId, PaymentRequest request) {
        log.info("Processando pagamento da cobrança: {}", request.getChargeId());

        Charge charge = chargeService.findById(request.getChargeId());
        User payer = userRepository.findByIdWithLock(payerId)
                .orElseThrow(() -> new BusinessException("Usuário pagador não encontrado"));

        validateChargePayment(charge, payer);

        if (request.getPaymentMethod() == PaymentMethod.BALANCE) {
            return payWithBalance(charge, payer);
        } else {
            return payWithCreditCard(charge, payer, request.getCreditCard());
        }
    }

    @Transactional
    public PaymentResponse deposit(UUID userId, DepositRequest request) {
        log.info("Processando depósito de {} para usuário: {}", request.getAmount(), userId);

        if (!authorizerService.authorize()) {
            throw new PaymentAuthorizationException("Depósito não autorizado pelo sistema externo");
        }

        User user = userRepository.findByIdWithLock(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        user.addBalance(request.getAmount());
        userRepository.save(user);

        Transaction transaction = Transaction.builder()
                .user(user)
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .paymentMethod(null)
                .description("Depósito de saldo")
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Depósito realizado com sucesso: {}", transaction.getId());

        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .amount(request.getAmount())
                .paymentMethod(null)
                .status("APPROVED")
                .message("Depósito realizado com sucesso")
                .transactionDate(transaction.getCreatedAt())
                .build();
    }

    @Transactional
    public void cancelPaidCharge(UUID chargeId, UUID userId) {
        log.info("Cancelando cobrança paga: {}", chargeId);

        Charge charge = chargeService.findById(chargeId);

        if (!charge.getOriginator().getId().equals(userId)) {
            throw new BusinessException("Apenas o originador pode cancelar a cobrança");
        }

        if (!charge.isPaid()) {
            throw new BusinessException("Apenas cobranças pagas podem ser estornadas");
        }

        java.util.List<Transaction> transactions = transactionRepository.findByChargeId(chargeId);
        if (transactions.isEmpty()) {
            throw new BusinessException("Transações não encontradas para esta cobrança");
        }

        Transaction paymentTransaction = transactions.get(0);

        if (paymentTransaction.getPaymentMethod() == PaymentMethod.BALANCE) {
            refundBalancePayment(charge);
        } else {
            refundCreditCardPayment(charge);
        }

        charge.markAsCancelled();
        chargeRepository.save(charge);

        log.info("Cobrança cancelada e estornada: {}", chargeId);
    }

    private void validateChargePayment(Charge charge, User payer) {
        if (!charge.isPending()) {
            throw new BusinessException("Esta cobrança não está pendente");
        }

        if (!charge.getRecipient().getId().equals(payer.getId())) {
            throw new BusinessException("Você não é o destinatário desta cobrança");
        }
    }

    private PaymentResponse payWithBalance(Charge charge, User payer) {
        log.info("Pagando cobrança com saldo");

        if (!payer.hasEnoughBalance(charge.getAmount())) {
            throw new InsufficientBalanceException("Saldo insuficiente para realizar o pagamento");
        }

        User recipient = userRepository.findByIdWithLock(charge.getOriginator().getId())
                .orElseThrow(() -> new BusinessException("Destinatário não encontrado"));

        payer.subtractBalance(charge.getAmount());
        recipient.addBalance(charge.getAmount());

        userRepository.save(payer);
        userRepository.save(recipient);

        charge.markAsPaid();
        chargeRepository.save(charge);

        Transaction transaction = Transaction.builder()
                .user(payer)
                .charge(charge)
                .amount(charge.getAmount())
                .type(TransactionType.PAYMENT)
                .paymentMethod(PaymentMethod.BALANCE)
                .description("Pagamento de cobrança com saldo")
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Pagamento com saldo realizado com sucesso");

        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .chargeId(charge.getId())
                .amount(charge.getAmount())
                .paymentMethod(PaymentMethod.BALANCE)
                .status("APPROVED")
                .message("Pagamento realizado com sucesso")
                .transactionDate(transaction.getCreatedAt())
                .build();
    }

    private PaymentResponse payWithCreditCard(Charge charge, User payer,
                                              PaymentRequest.CreditCardData creditCard) {
        log.info("Pagando cobrança com cartão de crédito");

        if (creditCard == null || creditCard.getCardNumber() == null) {
            throw new BusinessException("Dados do cartão de crédito são obrigatórios");
        }

        if (!authorizerService.authorize()) {
            throw new PaymentAuthorizationException("Pagamento não autorizado");
        }

        User recipient = userRepository.findByIdWithLock(charge.getOriginator().getId())
                .orElseThrow(() -> new BusinessException("Destinatário não encontrado"));

        recipient.addBalance(charge.getAmount());
        userRepository.save(recipient);

        charge.markAsPaid();
        chargeRepository.save(charge);

        Transaction transaction = Transaction.builder()
                .user(payer)
                .charge(charge)
                .amount(charge.getAmount())
                .type(TransactionType.PAYMENT)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .description("Pagamento de cobrança com cartão de crédito")
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Pagamento com cartão realizado com sucesso");

        return PaymentResponse.builder()
                .transactionId(transaction.getId())
                .chargeId(charge.getId())
                .amount(charge.getAmount())
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .status("APPROVED")
                .message("Pagamento realizado com sucesso")
                .transactionDate(transaction.getCreatedAt())
                .build();
    }

    private void refundBalancePayment(Charge charge) {
        User payer = userRepository.findByIdWithLock(charge.getRecipient().getId())
                .orElseThrow(() -> new BusinessException("Pagador não encontrado"));
        User recipient = userRepository.findByIdWithLock(charge.getOriginator().getId())
                .orElseThrow(() -> new BusinessException("Destinatário não encontrado"));

        if (!recipient.hasEnoughBalance(charge.getAmount())) {
            throw new BusinessException("Destinatário não possui saldo suficiente para estorno");
        }

        recipient.subtractBalance(charge.getAmount());
        payer.addBalance(charge.getAmount());

        userRepository.save(payer);
        userRepository.save(recipient);

        Transaction refundTransaction = Transaction.builder()
                .user(payer)
                .charge(charge)
                .amount(charge.getAmount())
                .type(TransactionType.REFUND)
                .paymentMethod(PaymentMethod.BALANCE)
                .description("Estorno de cobrança cancelada")
                .build();

        transactionRepository.save(refundTransaction);
    }

    private void refundCreditCardPayment(Charge charge) {
        if (!authorizerService.authorize()) {
            throw new PaymentAuthorizationException("Estorno não autorizado pelo sistema externo");
        }

        User recipient = userRepository.findByIdWithLock(charge.getOriginator().getId())
                .orElseThrow(() -> new BusinessException("Destinatário não encontrado"));

        if (!recipient.hasEnoughBalance(charge.getAmount())) {
            throw new BusinessException("Destinatário não possui saldo suficiente para estorno");
        }

        recipient.subtractBalance(charge.getAmount());
        userRepository.save(recipient);

        Transaction refundTransaction = Transaction.builder()
                .user(charge.getRecipient())
                .charge(charge)
                .amount(charge.getAmount())
                .type(TransactionType.REFUND)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .description("Estorno de cobrança cancelada via cartão")
                .build();

        transactionRepository.save(refundTransaction);
    }
}