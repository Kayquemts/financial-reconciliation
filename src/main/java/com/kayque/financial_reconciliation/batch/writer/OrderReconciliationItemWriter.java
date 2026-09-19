package com.kayque.financial_reconciliation.batch.writer;

import com.kayque.financial_reconciliation.domain.entity.Order;
import com.kayque.financial_reconciliation.domain.dto.ReconciliationResultDTO;
import com.kayque.financial_reconciliation.domain.enums.ReconciliationStatus;
import com.kayque.financial_reconciliation.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderReconciliationItemWriter implements ItemWriter<ReconciliationResultDTO> {

    private final OrderRepository orderRepository;

    @Override
    public void write(Chunk<? extends ReconciliationResultDTO> chunk) {

        List<ReconciliationResultDTO> updatable = chunk.getItems().stream()
                .filter(result -> result.getResult() != ReconciliationStatus.NOT_FOUND)
                .collect(Collectors.toList());

        if (updatable.isEmpty()) {
            return;
        }

        List<String> ids = updatable.stream().map(ReconciliationResultDTO::getTransactionId).toList();

        Map<String, Order> existingOrders = orderRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Order::getId, Function.identity()));

        List<Order> ordersToUpdate = updatable.stream()
                .map(result -> applyReconciliation(existingOrders.get(result.getTransactionId()), result))
                .filter(java.util.Objects::nonNull)
                .toList();

        orderRepository.saveAll(ordersToUpdate);
        log.info("Updated {} orders with reconciliation status ({} skipped as NOT_FOUND)",
                ordersToUpdate.size(), chunk.size() - updatable.size());
    }

    private Order applyReconciliation(Order order, ReconciliationResultDTO result) {
        if (order == null) {
            log.warn("Order {} vanished between processing and writing, skipping.", result.getTransactionId());
            return null;
        }

        order.setReconciliationStatus(result.getResult());
        order.setReconciledAt(LocalDateTime.now());
        return order;
    }
}