package id.ac.ui.cs.advprog.bidmartauctionservice.config;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initDatabase(AuctionRepository auctionRepository) {
        return args -> {
            // Hanya injeksi data jika tabel pangkalan data masih kosong
            if (auctionRepository.count() == 0) {
                Instant now = Instant.now();
                UUID dummySellerId = UUID.randomUUID();

                Auction item1 = Auction.builder()
                        .listingId(UUID.randomUUID())
                        .sellerId(dummySellerId)
                        .startingPrice(new BigDecimal("2000.00"))
                        .currentHighestBid(new BigDecimal("2500.00"))
                        .minimumIncrement(new BigDecimal("50.00"))
                        .reservePrice(new BigDecimal("3000.00"))
                        .startTime(now)
                        .endTime(now.plus(Duration.ofDays(2)))
                        .status(AuctionStatus.ACTIVE)
                        .build();

                Auction item2 = Auction.builder()
                        .listingId(UUID.randomUUID())
                        .sellerId(dummySellerId)
                        .startingPrice(new BigDecimal("500.00"))
                        .currentHighestBid(new BigDecimal("600.00"))
                        .minimumIncrement(new BigDecimal("20.00"))
                        .reservePrice(new BigDecimal("700.00"))
                        .startTime(now)
                        .endTime(now.plus(Duration.ofHours(5)))
                        .status(AuctionStatus.ACTIVE)
                        .build();

                auctionRepository.save(item1);
                auctionRepository.save(item2);

                System.out.println("✅ Data Seeder: Successfully injected dummy auctions into H2 Database.");
            }
        };
    }
}