package com.kayque.financial_reconciliation.batch.processor;

import com.kayque.financial_reconciliation.domain.dto.ReconciliationResultDTO;
import com.kayque.financial_reconciliation.domain.entity.Order;
import com.kayque.financial_reconciliation.domain.dto.TransactionDTO;
import com.kayque.financial_reconciliation.domain.enums.ReconciliationStatus;
import com.kayque.financial_reconciliation.domain.exceptions.InvalidTransactionDataException;
import com.kayque.financial_reconciliation.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReconciliationItemProcessor implements ItemProcessor<TransactionDTO, ReconciliationResultDTO> {

    private final OrderRepository orderRepository;
    private static final BigDecimal TOLERANCE_VALUE = new BigDecimal("0.01");

    private static final Map<String, String> EXPECTED_STATUS = Map.of(
            "APPROVED", "PENDING",
            "CANCELLED", "CANCELLED"
    );

    @Override
    public ReconciliationResultDTO process(TransactionDTO transaction) {
        validate(transaction);
        Optional<Order> orderOpt = orderRepository.findById(transaction.getTransactionId());

        if (orderOpt.isEmpty()) {
            log.warn("Transaction {} present in the gateway but not found in the internal database.", transaction.getTransactionId());
            return buildResult(transaction, null, ReconciliationStatus.NOT_FOUND,
                    "Transaction not found in the internal database");
        }

        Order order = orderOpt.get();

        boolean isAmountDivergent = isAmountDivergent(order.getOrder_amount(), transaction.getAmount());
        boolean isStatusDivergent = isStatusDivergent(order.getStatus(), transaction.getGatewayStatus());

        ReconciliationStatus result;
        String observation;

        if (isAmountDivergent && isStatusDivergent) {
            result = ReconciliationStatus.AMOUNT_AND_STATUS_MISMATCH;
            observation = "Amount and status mismatch between database and gateway";
        } else if (isAmountDivergent) {
            result = ReconciliationStatus.AMOUNT_MISMATCH;
            observation = "Amount mismatch between database and gateway";
        } else if (isStatusDivergent) {
            result = ReconciliationStatus.STATUS_MISMATCH;
            observation = "Status mismatch between database and gateway";
        } else {
            result = ReconciliationStatus.RECONCILED;
            observation = "Transaction successfully reconciled";
        }

        return buildResult(transaction, order, result, observation);
    }

    private void validate(TransactionDTO transaction) {
        if (transaction.getAmount() == null || transaction.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidTransactionDataException(
                    "Transaction " + transaction.getTransactionId() + " has an invalid amount: " + transaction.getAmount());
        }
    }

    private boolean isAmountDivergent(BigDecimal bankAmount, BigDecimal gatewayAmount) {
        if (bankAmount == null || gatewayAmount == null) {
            return true;
        }
        return bankAmount.subtract(gatewayAmount).abs().compareTo(TOLERANCE_VALUE) > 0;
    }

    private boolean isStatusDivergent(String bankStatus, String gatewayStatus) {
        String expected = EXPECTED_STATUS.get(gatewayStatus);
        if (expected == null) {
            return true;
        }
        return !expected.equalsIgnoreCase(bankStatus);
    }

    private ReconciliationResultDTO buildResult(TransactionDTO transaction, Order order,
                                                ReconciliationStatus result, String observation) {
        return new ReconciliationResultDTO(
                transaction.getTransactionId(),
                order != null ? order.getOrder_amount() : null,
                transaction.getAmount(),
                order != null ? order.getStatus() : null,
                transaction.getGatewayStatus(),
                result,
                observation,
                LocalDateTime.now()
        );
    }
}