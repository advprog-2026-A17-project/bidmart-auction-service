package id.ac.ui.cs.advprog.bidmartauctionservice.config;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.repository.AuctionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner initDatabase(AuctionRepository auctionRepository) {
        return args -> {
            if (auctionRepository.count() == 0) {
                LocalDateTime now = LocalDateTime.now();

                Auction item1 = new Auction();
                item1.setItemId("ITM-001");
                item1.setItemName("MacBook Pro M3 Max");
                item1.setCurrentHighestBid(2500.0);
                item1.setMinIncrement(50.0);
                item1.setReservePrice(3000.0);
                item1.setStartTime(now);
                item1.setEndTime(now.plusDays(2));
                item1.setStatus("ACTIVE");

                Auction item2 = new Auction();
                item2.setItemId("ITM-002");
                item2.setItemName("Sony PlayStation 5 Pro");
                item2.setCurrentHighestBid(600.0);
                item2.setMinIncrement(20.0);
                item2.setReservePrice(700.0);
                item2.setStartTime(now);
                item2.setEndTime(now.plusHours(5));
                item2.setStatus("ACTIVE");

                Auction item3 = new Auction();
                item3.setItemId("ITM-003");
                item3.setItemName("Herman Miller Aeron");
                item3.setCurrentHighestBid(850.0);
                item3.setMinIncrement(25.0);
                item3.setReservePrice(1000.0);
                item3.setStartTime(now.minusDays(1));
                item3.setEndTime(now.minusHours(1));
                item3.setStatus("CLOSED");

                auctionRepository.save(item1);
                auctionRepository.save(item2);
                auctionRepository.save(item3);

                System.out.println("DataSeeder: Initial active auctions have been populated into the database.");
            }
        };
    }
}