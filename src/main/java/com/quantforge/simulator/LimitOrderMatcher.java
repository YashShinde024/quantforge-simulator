package com.quantforge.simulator;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LimitOrderMatcher {

    private final TradeOrderRepository tradeOrderRepository;
    private final AssetRepository assetRepository;
    private final PortfolioPositionRepository portfolioPositionRepository;
    private final UserWalletRepository userWalletRepository;

    @Scheduled(fixedDelay = 5000) // every 5s
    public void matchOpenLimitOrders() {
        var openOrders = tradeOrderRepository.findByStatusOrderByCreatedAtAsc("OPEN");

        for (TradeOrder order : openOrders) {
            if (order.getOrderType() != OrderType.LIMIT) continue;
            if (order.getLimitPrice() == null) continue;

            Asset asset = assetRepository.findBySymbol(order.getSymbol()).orElse(null);
            if (asset == null) continue;

            double market = asset.getCurrentPrice();
            double limit = order.getLimitPrice();

            boolean match = (order.getSide() == OrderSide.BUY) ? (market <= limit) : (market >= limit);
            if (!match) continue;

            UserWallet wallet = userWalletRepository.findByUserId(order.getUserId()).orElse(null);
            if (wallet == null) continue;

            PortfolioPosition position = portfolioPositionRepository
                    .findByUserIdAndSymbol(order.getUserId(), order.getSymbol())
                    .orElseGet(() -> {
                        PortfolioPosition p = new PortfolioPosition();
                        p.setUserId(order.getUserId());
                        p.setSymbol(order.getSymbol());
                        p.setQuantity(0);
                        p.setAvgPrice(0.0);
                        return p;
                    });

            int qty = order.getQuantity();
            double execPrice = limit; // execute at limit price
            double value = execPrice * qty;

            if (order.getSide() == OrderSide.BUY) {
                if (wallet.getBalance() < value) {
                    // keep OPEN (or mark REJECTED if you prefer)
                    continue;
                }

                wallet.setBalance(wallet.getBalance() - value);

                int oldQty = position.getQuantity();
                double oldAvg = position.getAvgPrice();
                int newQty = oldQty + qty;
                double newAvg = ((oldQty * oldAvg) + (qty * execPrice)) / newQty;

                position.setQuantity(newQty);
                position.setAvgPrice(newAvg);

                userWalletRepository.save(wallet);
                portfolioPositionRepository.save(position);

            } else { // SELL
                int oldQty = position.getQuantity();
                if (oldQty < qty) {
                    continue; // keep OPEN (or REJECTED)
                }

                wallet.setBalance(wallet.getBalance() + value);

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
            order.setPrice(execPrice);
            tradeOrderRepository.save(order);
        }
    }
}