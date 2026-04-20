package com.quantforge.simulator;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrderSide {
    BUY,
    SELL;

    @JsonCreator
    public static OrderSide from(String value) {
        return OrderSide.valueOf(value.trim().toUpperCase());
    }
}