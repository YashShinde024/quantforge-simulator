package com.quantforge.simulator;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioPositionRepository extends JpaRepository<PortfolioPosition, Long> {
    List<PortfolioPosition> findByUserId(Long userId);
    Optional<PortfolioPosition> findByUserIdAndSymbol(Long userId, String symbol);
}