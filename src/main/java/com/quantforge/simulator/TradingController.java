package com.quantforge.simulator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trading")
@RequiredArgsConstructor
public class TradingController {

    private final AssetRepository assetRepository;
    private final TradeOrderRepository tradeOrderRepository;
    private final PortfolioPositionRepository portfolioPositionRepository;
    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("Unauthenticated request");
        }

        String username = authentication.getName();

        return userRepository.findByNameIgnoreCase(username)
                .or(() -> userRepository.findByEmailIgnoreCase(username))
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("No DB user mapped for logged-in username: " + username));
    }

    private UserWallet getWalletOrThrow(Long userId) {
        return userWalletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for userId: " + userId));
    }

    @GetMapping("/assets")
    public List<Asset> getAssets() {
        return assetRepository.findAll();
    }

    @GetMapping("/wallet")
    public UserWallet getWallet(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return getWalletOrThrow(userId);
    }

    @PostMapping("/wallet/deposit")
    public UserWallet deposit(@RequestParam Double amount, Authentication authentication) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("Deposit amount must be > 0");
        }

        Long userId = getCurrentUserId(authentication);
        UserWallet wallet = getWalletOrThrow(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        return userWalletRepository.save(wallet);
    }

    @PostMapping("/orders")
    public TradeOrder placeOrder(@RequestBody PlaceOrderRequest request, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        Asset asset = assetRepository.findBySymbol(request.getSymbol().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Asset not found: " + request.getSymbol()));

        if (request.getQuantity() == null || request.getQuantity() <= 0) {
            throw new RuntimeException("Quantity must be > 0");
        }
        if (request.getSide() == null) {
            throw new RuntimeException("Order side is required");
        }
        if (request.getOrderType() == null) {
            request.setOrderType(OrderType.MARKET);
        }
        if (request.getOrderType() == OrderType.LIMIT &&
                (request.getLimitPrice() == null || request.getLimitPrice() <= 0)) {
            throw new RuntimeException("Valid limitPrice is required for LIMIT order");
        }

        double marketPrice = asset.getCurrentPrice();
        double executionPrice = marketPrice;
        boolean shouldExecuteNow;

        if (request.getOrderType() == OrderType.MARKET) {
            shouldExecuteNow = true;
        } else {
            double lp = request.getLimitPrice();
            shouldExecuteNow = (request.getSide() == OrderSide.BUY) ? (marketPrice <= lp) : (marketPrice >= lp);
            executionPrice = lp; // simplified fill price
        }

        TradeOrder order = new TradeOrder();
        order.setUserId(userId);
        order.setSymbol(asset.getSymbol());
        order.setSide(request.getSide());
        order.setQuantity(request.getQuantity());
        order.setOrderType(request.getOrderType());
        order.setLimitPrice(request.getLimitPrice());
        order.setCreatedAt(LocalDateTime.now());

        if (!shouldExecuteNow) {
            order.setStatus("OPEN");
            order.setPrice(null);
            return tradeOrderRepository.save(order);
        }

        UserWallet wallet = getWalletOrThrow(userId);

        PortfolioPosition position = portfolioPositionRepository
                .findByUserIdAndSymbol(userId, asset.getSymbol())
                .orElseGet(() -> {
                    PortfolioPosition p = new PortfolioPosition();
                    p.setUserId(userId);
                    p.setSymbol(asset.getSymbol());
                    p.setQuantity(0);
                    p.setAvgPrice(0.0);
                    return p;
                });

        int qty = request.getQuantity();
        double orderValue = executionPrice * qty;

        if (request.getSide() == OrderSide.BUY) {
            if (wallet.getBalance() < orderValue) {
                throw new RuntimeException("Insufficient wallet balance");
            }

            wallet.setBalance(wallet.getBalance() - orderValue);

            int oldQty = position.getQuantity();
            double oldAvg = position.getAvgPrice();
            int newQty = oldQty + qty;
            double newAvg = ((oldQty * oldAvg) + (qty * executionPrice)) / newQty;

            position.setQuantity(newQty);
            position.setAvgPrice(newAvg);

            userWalletRepository.save(wallet);
            portfolioPositionRepository.save(position);

        } else {
            int oldQty = position.getQuantity();
            if (oldQty < qty) {
                throw new RuntimeException("Not enough quantity to sell");
            }

            wallet.setBalance(wallet.getBalance() + orderValue);

            int remaining = oldQty - qty;
            if (remaining == 0) {
                portfolioPositionRepository.delete(position);
            } else {
                position.setQuantity(remaining);
                portfolioPositionRepository.save(position);
            }

            userWalletRepository.save(wallet);
        }

        order.setStatus("EXECUTED");
        order.setPrice(executionPrice);
        return tradeOrderRepository.save(order);
    }

    @GetMapping("/orders/open")
    public List<TradeOrder> getOpenOrders(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return tradeOrderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "OPEN");
    }

    @GetMapping("/portfolio")
    public List<PortfolioPosition> getPortfolio(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return portfolioPositionRepository.findByUserId(userId);
    }

    @GetMapping("/orders")
    public List<TradeOrder> getOrders(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return tradeOrderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @GetMapping("/portfolio/pnl")
    public List<PortfolioPnlResponse> getPortfolioPnl(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        List<PortfolioPosition> positions = portfolioPositionRepository.findByUserId(userId);

        List<PortfolioPnlResponse> result = new ArrayList<>();

        for (PortfolioPosition p : positions) {
            Asset asset = assetRepository.findBySymbol(p.getSymbol()).orElse(null);

            double currentPrice = (asset != null) ? asset.getCurrentPrice() : p.getAvgPrice();
            double invested = p.getQuantity() * p.getAvgPrice();
            double currentValue = p.getQuantity() * currentPrice;
            double pnl = currentValue - invested;
            double pnlPct = invested == 0 ? 0 : (pnl / invested) * 100.0;

            result.add(new PortfolioPnlResponse(
                    p.getSymbol(),
                    p.getQuantity(),
                    p.getAvgPrice(),
                    currentPrice,
                    invested,
                    currentValue,
                    pnl,
                    pnlPct
            ));
        }

        return result;
    }

    @PostMapping("/admin/price")
    public Asset updatePrice(@RequestBody UpdatePriceRequest request) {
        // Secured by SecurityConfig: /api/trading/admin/** requires ADMIN role
        if (request.getSymbol() == null || request.getSymbol().isBlank()) {
            throw new RuntimeException("Symbol is required");
        }
        if (request.getPrice() == null || request.getPrice() <= 0) {
            throw new RuntimeException("Valid price is required");
        }

        Asset asset = assetRepository.findBySymbol(request.getSymbol().toUpperCase())
                .orElseThrow(() -> new RuntimeException("Asset not found: " + request.getSymbol()));

        asset.setCurrentPrice(request.getPrice());
        return assetRepository.save(asset);
    }

    @DeleteMapping("/orders/{orderId}")
    public String cancelOrder(@PathVariable Long orderId, Authentication authentication) {
        Long userId = getCurrentUserId(authentication);

        TradeOrder order = tradeOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("You can cancel only your own orders");
        }

        if (!"OPEN".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Only OPEN orders can be cancelled");
        }

        order.setStatus("CANCELLED");
        tradeOrderRepository.save(order);

        return "Order cancelled successfully";
    }

    @Data
    @AllArgsConstructor
    static class PortfolioPnlResponse {
        private String symbol;
        private Integer quantity;
        private Double avgPrice;
        private Double currentPrice;
        private Double investedValue;
        private Double currentValue;
        private Double pnl;
        private Double pnlPercent;
    }
}