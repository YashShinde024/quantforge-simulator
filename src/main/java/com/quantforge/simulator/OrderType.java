package com.quantforge.simulator;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum OrderType {
    MARKET,
    LIMIT;

    @JsonCreator
    public static OrderType from(String value) {
        return OrderType.valueOf(value.trim().toUpperCase());
    }
}