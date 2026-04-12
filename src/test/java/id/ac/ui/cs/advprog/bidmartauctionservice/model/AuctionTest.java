package id.ac.ui.cs.advprog.bidmartauctionservice.model;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuctionTest {

    @Test
    void testAuctionBuilderAndGetters() {
        UUID id = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant endTime = now.plus(1, ChronoUnit.DAYS);

        Auction auction = Auction.builder()
                .id(id)
                .listingId(listingId)
                .sellerId(sellerId)
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("500.00"))
                .currentHighestBid(new BigDecimal("150.00"))
                .startTime(now)
                .endTime(endTime)
                .status(AuctionStatus.ACTIVE)
                .build();

        assertEquals(id, auction.getId());
        assertEquals(listingId, auction.getListingId());
        assertEquals(sellerId, auction.getSellerId());
        assertEquals(new BigDecimal("100.00"), auction.getStartingPrice());
        assertEquals(new BigDecimal("10.00"), auction.getMinimumIncrement());
        assertEquals(new BigDecimal("500.00"), auction.getReservePrice());
        assertEquals(new BigDecimal("150.00"), auction.getCurrentHighestBid());
        assertEquals(now, auction.getStartTime());
        assertEquals(endTime, auction.getEndTime());
        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
    }

    @Test
    void testAuctionSetters() {
        Auction auction = new Auction();
        UUID newId = UUID.randomUUID();
        auction.setId(newId);
        auction.setStatus(AuctionStatus.CLOSED);

        assertEquals(newId, auction.getId());
        assertEquals(AuctionStatus.CLOSED, auction.getStatus());
    }
}