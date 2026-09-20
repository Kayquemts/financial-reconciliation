package com.kayque.financial_reconciliation.writer;

import com.kayque.financial_reconciliation.batch.writer.OrderReconciliationItemWriter;
import com.kayque.financial_reconciliation.domain.entity.Order;
import com.kayque.financial_reconciliation.domain.dto.ReconciliationResultDTO;
import com.kayque.financial_reconciliation.domain.enums.ReconciliationStatus;
import com.kayque.financial_reconciliation.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderReconciliationItemWriterTest {

    @Mock
    private OrderRepository orderRepository;

    private OrderReconciliationItemWriter writer;

    @Test
    void shouldUpdateOrdersExcludingNotFoundItems() {
        writer = new OrderReconciliationItemWriter(orderRepository);

        Order order1 = existingOrder("TRX-001");
        Order order2 = existingOrder("TRX-002");

        when(orderRepository.findAllById(List.of("TRX-001", "TRX-002")))
                .thenReturn(List.of(order1, order2));

        ReconciliationResultDTO reconciled = resultFor("TRX-001", ReconciliationStatus.RECONCILED);
        ReconciliationResultDTO mismatch = resultFor("TRX-002", ReconciliationStatus.AMOUNT_MISMATCH);
        ReconciliationResultDTO notFound = resultFor("TRX-999", ReconciliationStatus.NOT_FOUND);

        writer.write(new Chunk<>(List.of(reconciled, mismatch, notFound)));

        verify(orderRepository, never()).findAllById(argThat(ids -> ((Collection<String>) ids).contains("TRX-999")));

        ArgumentCaptor<List<Order>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).saveAll(captor.capture());

        List<Order> saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved).extracting(Order::getId).containsExactlyInAnyOrder("TRX-001", "TRX-002");
        assertThat(saved).allMatch(o -> o.getReconciledAt() != null);

        Order savedOrder1 = saved.stream().filter(o -> o.getId().equals("TRX-001")).findFirst().orElseThrow();
        assertThat(savedOrder1.getReconciliationStatus()).isEqualTo(ReconciliationStatus.RECONCILED);
    }

    @Test
    void shouldPreserveOriginalFieldsNotOverwrittenByReconciliation() {
        writer = new OrderReconciliationItemWriter(orderRepository);

        Order existing = existingOrder("TRX-001");
        LocalDateTime originalCreatedAt = existing.getCreatedAt();

        when(orderRepository.findAllById(List.of("TRX-001"))).thenReturn(List.of(existing));

        writer.write(new Chunk<>(List.of(resultFor("TRX-001", ReconciliationStatus.RECONCILED))));

        ArgumentCaptor<List<Order>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).saveAll(captor.capture());

        Order saved = captor.getValue().get(0);
        assertThat(saved.getCreatedAt()).isEqualTo(originalCreatedAt);
        assertThat(saved.getStatus()).isEqualTo("PENDING");
    }

    @Test
    void shouldSkipOrderThatDisappearedBetweenProcessingAndWriting() {
        writer = new OrderReconciliationItemWriter(orderRepository);

        when(orderRepository.findAllById(List.of("TRX-404"))).thenReturn(List.of());

        writer.write(new Chunk<>(List.of(resultFor("TRX-404", ReconciliationStatus.RECONCILED))));

        ArgumentCaptor<List<Order>> captor = ArgumentCaptor.forClass(List.class);
        verify(orderRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).isEmpty();
    }

    @Test
    void shouldNotTouchRepositoryWhenAllItemsAreNotFound() {
        writer = new OrderReconciliationItemWriter(orderRepository);

        writer.write(new Chunk<>(List.of(resultFor("TRX-999", ReconciliationStatus.NOT_FOUND))));

        verifyNoInteractions(orderRepository);
    }

    private Order existingOrder(String id) {
        Order order = new Order();
        order.setId(id);
        order.setOrder_amount(new BigDecimal("150.00"));
        order.setStatus("PENDING");
        order.setCreatedAt(LocalDateTime.now().minusDays(2));
        return order;
    }

    private ReconciliationResultDTO resultFor(String transactionId, ReconciliationStatus status) {
        return new ReconciliationResultDTO(
                transactionId,
                new BigDecimal("150.00"),
                new BigDecimal("150.00"),
                "PENDING",
                "APPROVED",
                status,
                "test observation",
                LocalDateTime.now()
        );
    }
}