package id.ac.ui.cs.advprog.bidmartauctionservice.repository;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.Auction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AuctionRepositoryTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Test
    void testSaveAndFindByIdWithLock() {
        Auction auction = new Auction(null, "ITM-X", "Monitor", 200.0, 20.0, 1000.0,
                LocalDateTime.now(), LocalDateTime.now().plusHours(2), "ACTIVE");

        Auction saved = auctionRepository.save(auction);
        Optional<Auction> found = auctionRepository.findByIdWithLock(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Monitor", found.get().getItemName());
    }
}