package com.kayque.financial_reconciliation.repository;

import com.kayque.financial_reconciliation.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, String> {
}
