package com.nimble.payment_gateway.repository;

import com.nimble.payment_gateway.enums.ChargeStatus;
import com.nimble.payment_gateway.model.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, UUID> {

    @Query("SELECT c FROM Charge c WHERE c.originator.id = :userId AND c.status = :status")
    List<Charge> findByOriginatorIdAndStatus(UUID userId, ChargeStatus status);

    @Query("SELECT c FROM Charge c WHERE c.recipient.id = :userId AND c.status = :status")
    List<Charge> findByRecipientIdAndStatus(UUID userId, ChargeStatus status);

    @Query("SELECT c FROM Charge c WHERE c.originator.id = :userId")
    List<Charge> findByOriginatorId(UUID userId);

    @Query("SELECT c FROM Charge c WHERE c.recipient.id = :userId")
    List<Charge> findByRecipientId(UUID userId);
}
