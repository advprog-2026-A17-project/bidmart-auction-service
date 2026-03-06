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
            }
        };
    }
}