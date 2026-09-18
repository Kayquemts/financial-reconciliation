package com.kayque.financial_reconciliation.repository;

import com.kayque.financial_reconciliation.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
}
