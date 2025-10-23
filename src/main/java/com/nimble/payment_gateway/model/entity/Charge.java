package com.nimble.payment_gateway.model.entity;

import com.nimble.payment_gateway.enums.ChargeStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "charges", indexes = {
        @Index(name = "idx_charge_originator", columnList = "originator_id"),
        @Index(name = "idx_charge_recipient", columnList = "recipient_id"),
        @Index(name = "idx_charge_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "originator_id", nullable = false)
    private User originator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ChargeStatus status = ChargeStatus.PENDING;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public boolean isPending() {
        return this.status == ChargeStatus.PENDING;
    }

    public boolean isPaid() {
        return this.status == ChargeStatus.PAID;
    }

    public boolean isCancelled() {
        return this.status == ChargeStatus.CANCELLED;
    }

    public void markAsPaid() {
        this.status = ChargeStatus.PAID;
    }

    public void markAsCancelled() {
        this.status = ChargeStatus.CANCELLED;
    }
}