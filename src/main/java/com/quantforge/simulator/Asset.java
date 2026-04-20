package com.quantforge.simulator;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "assets")
@Data
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "symbol", unique = true, nullable = false)
    private String symbol;   // e.g. AAPL, TSLA

    @Column(nullable = false)
    private String name;     // e.g. Apple Inc

    @Column(name = "current_price" , nullable = false)
    private Double currentPrice;
}