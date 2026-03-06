package id.ac.ui.cs.advprog.bidmartauctionservice.config;

<<<<<<< Updated upstream
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
=======
import id.ac.ui.cs.advprog.bidmartauctionservice.model.Auction;
>>>>>>> Stashed changes
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

<<<<<<< Updated upstream
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
=======
import java.time.LocalDateTime;
>>>>>>> Stashed changes

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initDatabase(AuctionRepository auctionRepository) {
        return args -> {
<<<<<<< Updated upstream
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

                System.out.println("DataSeeder: Initial active auctions have been populated into the H2 database.");
=======
            // Hanya injeksi data jika tabel pangkalan data masih kosong
            if (auctionRepository.count() == 0) {

                Auction auction1 = new Auction();
                // Catatan: Sesuaikan nama setter atribut di bawah ini (misal: setItemName atau setTitle)
                // dengan atribut yang ada di dalam model Auction Anda.

                auction1.setStatus("ACTIVE");
                auction1.setCurrentHighestBid(150000.0);
                auction1.setMinIncrement(10000.0);
                auction1.setEndTime(LocalDateTime.now().plusDays(2));
                auctionRepository.save(auction1);

                Auction auction2 = new Auction();
                auction2.setStatus("ACTIVE");
                auction2.setCurrentHighestBid(300000.0);
                auction2.setMinIncrement(25000.0);
                auction2.setEndTime(LocalDateTime.now().plusHours(5));
                auctionRepository.save(auction2);

                Auction auction3 = new Auction();
                auction3.setStatus("EXTENDED");
                auction3.setCurrentHighestBid(5000000.0);
                auction3.setMinIncrement(100000.0);
                auction3.setEndTime(LocalDateTime.now().plusMinutes(15));
                auctionRepository.save(auction3);

                System.out.println("✅ Data Seeder: Successfully injected dummy auctions into H2 Database.");
>>>>>>> Stashed changes
            }
        };
    }
}