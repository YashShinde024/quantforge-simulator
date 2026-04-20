package com.quantforge.simulator;

import lombok.Data;

@Data
public class PlaceOrderRequest {
    private String symbol;
    private OrderSide side;      // BUY / SELL
    private Integer quantity;
    private OrderType orderType; // MARKET / LIMIT
    private Double limitPrice;   // required for LIMIT
}