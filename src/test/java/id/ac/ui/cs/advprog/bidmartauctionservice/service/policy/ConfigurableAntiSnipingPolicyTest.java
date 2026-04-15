package id.ac.ui.cs.advprog.bidmartauctionservice.service.policy;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigurableAntiSnipingPolicyTest {

    @Test
    void applyForBid_ExtendsAndTransitionsToExtended_WhenWithinThreshold() {
        ConfigurableAntiSnipingPolicy policy = new ConfigurableAntiSnipingPolicy(180, 180);
        Instant bidTime = Instant.now();
        Auction auction = buildAuction(AuctionStatus.ACTIVE, bidTime.plusSeconds(170));

        policy.applyForBid(auction, bidTime);

        assertEquals(AuctionStatus.EXTENDED, auction.getStatus());
        assertTrue(!auction.getEndTime().isBefore(bidTime.plusSeconds(180)));
    }

    @Test
    void applyForBid_DoesNothing_WhenOutsideThreshold() {
        ConfigurableAntiSnipingPolicy policy = new ConfigurableAntiSnipingPolicy(120, 120);
        Instant bidTime = Instant.now();
        Instant originalEndTime = bidTime.plusSeconds(180);
        Auction auction = buildAuction(AuctionStatus.ACTIVE, originalEndTime);

        policy.applyForBid(auction, bidTime);

        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
        assertEquals(originalEndTime, auction.getEndTime());
    }

    private Auction buildAuction(AuctionStatus status, Instant endTime) {
        return Auction.builder()
                .id(UUID.randomUUID())
                .listingId(UUID.randomUUID())
                .sellerId(UUID.randomUUID())
                .startingPrice(new BigDecimal("100.00"))
                .minimumIncrement(new BigDecimal("10.00"))
                .reservePrice(new BigDecimal("500.00"))
                .startTime(Instant.now().minusSeconds(100))
                .endTime(endTime)
                .status(status)
                .build();
    }
}
