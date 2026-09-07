package com.kayque.financial_reconciliation.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {

    private String transactionId;
    private String transactionDate;
    private BigDecimal amount;
    private String gatewayStatus;
}
