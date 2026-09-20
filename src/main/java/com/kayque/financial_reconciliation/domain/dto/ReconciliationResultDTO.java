package com.kayque.financial_reconciliation.domain.dto;

import com.kayque.financial_reconciliation.domain.enums.ReconciliationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReconciliationResultDTO {

    private String transactionId;

    private BigDecimal databaseAmount;
    private BigDecimal gatewayAmount;

    private String databaseStatus;
    private String gatewayStatus;

    private ReconciliationStatus result;

    private String observation;

    private LocalDateTime processingDate;
}