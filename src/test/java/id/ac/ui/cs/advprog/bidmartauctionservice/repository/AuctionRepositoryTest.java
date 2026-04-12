package id.ac.ui.cs.advprog.bidmartauctionservice.repository;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AuctionRepositoryTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Test
    void testSaveAndFindByIdWithPessimisticWriteLock() {
        Auction auction = Auction.builder()
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("200.00"))
                .minimumIncrement(new BigDecimal("20.00"))
                .reservePrice(new BigDecimal("1000.00"))
                .startTime(Instant.now())
                .endTime(Instant.now().plusSeconds(3600))
                .status(AuctionStatus.ACTIVE)
                .build();

        Auction saved = auctionRepository.save(auction);

        Optional<Auction> found = auctionRepository.findByIdWithPessimisticWriteLock(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(AuctionStatus.ACTIVE, found.get().getStatus());
        assertEquals(new BigDecimal("200.00"), found.get().getStartingPrice());
    }
}