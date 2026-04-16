package id.ac.ui.cs.advprog.bidmartauctionservice.repository;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Bid;
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
class BidRepositoryTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private BidRepository bidRepository;

    @Test
    void findHighestBidShouldBeDeterministicWhenAmountsAreEqual() {
        Auction auction = auctionRepository.save(Auction.builder()
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("500.00"))
                .startTime(Instant.now().minusSeconds(3600))
                .endTime(Instant.now().plusSeconds(3600))
                .status(AuctionStatus.ACTIVE)
                .build());

        Bid earlierBid = bidRepository.save(Bid.builder()
                .auction(auction)
                .bidderId(UUID.randomUUID())
                .bidAmount(new BigDecimal("200.00"))
                .bidTime(Instant.now().minusSeconds(5))
                .build());

        bidRepository.save(Bid.builder()
                .auction(auction)
                .bidderId(UUID.randomUUID())
                .bidAmount(new BigDecimal("200.00"))
                .bidTime(Instant.now())
                .build());

        Optional<Bid> selected = bidRepository
                .findFirstByAuctionIdOrderByBidAmountDescBidTimeAsc(auction.getId());

        assertTrue(selected.isPresent());
        assertEquals(earlierBid.getId(), selected.get().getId());
    }
}
