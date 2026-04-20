package com.quantforge.simulator;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AssetRepository assetRepository;
    private final UserRepository userRepository;
    private final UserWalletRepository userWalletRepository;

    @Override
    public void run(String... args) {
        assetRepository.findBySymbol("AAPL").orElseGet(() -> {
            Asset a = new Asset();
            a.setSymbol("AAPL");
            a.setName("Apple Inc");
            a.setCurrentPrice(210.0);
            return assetRepository.save(a);
        });

        userRepository.findByNameIgnoreCase("admin").ifPresent(admin -> {
            userWalletRepository.findByUserId(admin.getId()).orElseGet(() -> {
                UserWallet w = new UserWallet();
                w.setUserId(admin.getId());
                w.setBalance(100000.0); // 100k starting cash
                return userWalletRepository.save(w);
            });
        });
    }
}