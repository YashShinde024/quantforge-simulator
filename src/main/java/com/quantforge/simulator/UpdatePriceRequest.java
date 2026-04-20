package com.quantforge.simulator;

import lombok.Data;

@Data
public class UpdatePriceRequest {
    private String symbol;
    private Double price;
}