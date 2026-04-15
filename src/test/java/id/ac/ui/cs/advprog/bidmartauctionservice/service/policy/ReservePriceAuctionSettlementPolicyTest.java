package id.ac.ui.cs.advprog.bidmartauctionservice.service.policy;

import id.ac.ui.cs.advprog.bidmartauctionservice.model.entity.Auction;
import id.ac.ui.cs.advprog.bidmartauctionservice.model.enums.AuctionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReservePriceAuctionSettlementPolicyTest {

    private final ReservePriceAuctionSettlementPolicy policy = new ReservePriceAuctionSettlementPolicy();

    @Test
    void determineFinalStatus_ReturnsWon_WhenHighestBidMeetsReserve() {
        Auction auction = Auction.builder()
                .id(UUID.randomUUID())
                .reservePrice(new BigDecimal("500.00"))
                .currentHighestBid(new BigDecimal("500.00"))
                .build();

        assertEquals(AuctionStatus.WON, policy.determineFinalStatus(auction));
    }

    @Test
    void determineFinalStatus_ReturnsUnsold_WhenHighestBidIsBelowReserve() {
        Auction auction = Auction.builder()
                .id(UUID.randomUUID())
                .reservePrice(new BigDecimal("500.00"))
                .currentHighestBid(new BigDecimal("499.99"))
                .build();

        assertEquals(AuctionStatus.UNSOLD, policy.determineFinalStatus(auction));
    }

    @Test
    void determineFinalStatus_ReturnsUnsold_WhenNoBidExists() {
        Auction auction = Auction.builder()
                .id(UUID.randomUUID())
                .reservePrice(new BigDecimal("500.00"))
                .currentHighestBid(null)
                .build();

        assertEquals(AuctionStatus.UNSOLD, policy.determineFinalStatus(auction));
    }
}
