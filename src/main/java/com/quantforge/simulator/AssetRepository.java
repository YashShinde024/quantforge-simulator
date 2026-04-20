package com.quantforge.simulator;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findBySymbol(String symbol);
}