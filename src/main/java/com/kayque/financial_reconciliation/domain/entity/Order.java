package com.kayque.financial_reconciliation.domain.entity;

import com.kayque.financial_reconciliation.domain.enums.ReconciliationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "tb_order")
public class Order {

    @Id
    private String id;

    @Column(name = "order_amount", nullable = false)
    private BigDecimal order_amount;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "reconciliation_status")
    private ReconciliationStatus reconciliationStatus;

    @Column(name = "reconciled_at")
    private LocalDateTime reconciledAt;
}