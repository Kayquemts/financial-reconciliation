package com.kayque.financial_reconciliation.processor;

import com.kayque.financial_reconciliation.batch.processor.ReconciliationItemProcessor;
import com.kayque.financial_reconciliation.domain.entity.Order;
import com.kayque.financial_reconciliation.domain.dto.ReconciliationResultDTO;
import com.kayque.financial_reconciliation.domain.dto.TransactionDTO;
import com.kayque.financial_reconciliation.domain.enums.ReconciliationStatus;
import com.kayque.financial_reconciliation.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconciliationItemProcessorTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ReconciliationItemProcessor processor;

    @Test
    void shouldReconcileWhenAmountAndStatusMatch() {
        Order order = createOrder("TRX-001", new BigDecimal("150.00"));
        when(orderRepository.findById("TRX-001")).thenReturn(Optional.of(order));

        TransactionDTO transaction = new TransactionDTO("TRX-001", "2025-01-15", new BigDecimal("150.00"), "APPROVED");

        ReconciliationResultDTO result = processor.process(transaction);

        assert result != null;
        assertThat(result.getResult()).isEqualTo(ReconciliationStatus.RECONCILED);
    }

    @Test
    void shouldDetectAmountMismatch() {
        Order order = createOrder("TRX-002", new BigDecimal("100.00"));
        when(orderRepository.findById("TRX-002")).thenReturn(Optional.of(order));

        TransactionDTO transaction = new TransactionDTO("TRX-002", "2025-01-15", new BigDecimal("105.00"), "APPROVED");

        ReconciliationResultDTO result = processor.process(transaction);

        assert result != null;
        assertThat(result.getResult()).isEqualTo(ReconciliationStatus.AMOUNT_MISMATCH);
    }

    @Test
    void shouldDetectStatusMismatch() {
        Order order = createOrder("TRX-003", new BigDecimal("50.00"));
        when(orderRepository.findById("TRX-003")).thenReturn(Optional.of(order));

        TransactionDTO transaction = new TransactionDTO("TRX-003", "2025-01-15", new BigDecimal("50.00"), "REFUNDED");

        ReconciliationResultDTO result = processor.process(transaction);

        assert result != null;
        assertThat(result.getResult()).isEqualTo(ReconciliationStatus.STATUS_MISMATCH);
    }

    @Test
    void shouldMarkAsNotFoundWhenOrderDoesNotExistInDatabase() {
        when(orderRepository.findById("TRX-999")).thenReturn(Optional.empty());

        TransactionDTO transaction = new TransactionDTO("TRX-999", "2025-01-15", new BigDecimal("10.00"), "APPROVED");

        ReconciliationResultDTO result = processor.process(transaction);

        assert result != null;
        assertThat(result.getResult()).isEqualTo(ReconciliationStatus.NOT_FOUND);
    }

    @Test
    void shouldNotDivergeDueToBigDecimalScaleDifference() {
        Order order = createOrder("TRX-004", new BigDecimal("100.0"));
        when(orderRepository.findById("TRX-004")).thenReturn(Optional.of(order));

        TransactionDTO transaction = new TransactionDTO("TRX-004", "2025-01-15", new BigDecimal("100.00"), "APPROVED");

        ReconciliationResultDTO result = processor.process(transaction);

        assert result != null;
        assertThat(result.getResult()).isEqualTo(ReconciliationStatus.RECONCILED);
    }

    private Order createOrder(String id, BigDecimal amount) {
        Order order = new Order();
        order.setId(id);
        order.setOrder_amount(amount);
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}