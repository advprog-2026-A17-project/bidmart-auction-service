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
    void testFindByIdWithLock() {
        Auction auction = new Auction();
        auction.setItemId("ITM-01");
        auction.setItemName("Test Item");
        auction.setCurrentHighestBid(100.0);
        auction.setMinIncrement(10.0);
        auction.setReservePrice(500.0);
        auction.setStartTime(LocalDateTime.now());
        auction.setEndTime(LocalDateTime.now().plusHours(1));
        auction.setStatus("ACTIVE");

        Auction saved = auctionRepository.save(auction);
        Optional<Auction> found = auctionRepository.findByIdWithLock(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Test Item", found.get().getItemName());
    }
}