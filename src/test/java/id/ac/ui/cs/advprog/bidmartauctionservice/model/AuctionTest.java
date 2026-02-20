package id.ac.ui.cs.advprog.bidmartauctionservice.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {
    private Auction auction;

    @BeforeEach
    void setUp() {
        this.auction = new Auction();
    }

    @Test
    void testAuctionFields() {
        LocalDateTime now = LocalDateTime.now();
        auction.setId(1L);
        auction.setItemId("ITEM-123");
        auction.setItemName("MacBook Pro");
        auction.setCurrentHighestBid(1500.0);
        auction.setMinIncrement(50.0);
        auction.setEndTime(now);
        auction.setStatus("ACTIVE");

        assertEquals(1L, auction.getId());
        assertEquals("ITEM-123", auction.getItemId());
        assertEquals("MacBook Pro", auction.getItemName());
        assertEquals(1500.0, auction.getCurrentHighestBid());
        assertEquals(50.0, auction.getMinIncrement());
        assertEquals(now, auction.getEndTime());
        assertEquals("ACTIVE", auction.getStatus());
    }
}