package com.quantforge.simulator;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeOrderRepository extends JpaRepository<TradeOrder, Long> {
    List<TradeOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<TradeOrder> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
    List<TradeOrder> findByStatusOrderByCreatedAtAsc(String status);
}