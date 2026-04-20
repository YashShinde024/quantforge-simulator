package com.quantforge.simulator;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "trade_orders")
@Data
public class TradeOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String symbol;

    @Enumerated(EnumType.STRING)
    private OrderSide side;

    private Integer quantity;

    // execution price (for EXECUTED orders)
    private Double price;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    // requested limit price (for LIMIT orders)
    private Double limitPrice;

    // OPEN / EXECUTED / REJECTED
    private String status;

    private LocalDateTime createdAt;
}