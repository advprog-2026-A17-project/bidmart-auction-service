package id.ac.ui.cs.advprog.bidmartauctionservice.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AuctionTest {

    @Test
    void testAuctionAllArgsConstructorAndGetters() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusDays(1);
        Auction auction = new Auction(1L, "ITM-1", "Item A", 100.0, 10.0, 500.0, start, end, "ACTIVE");

        assertEquals(1L, auction.getId());
        assertEquals("ITM-1", auction.getItemId());
        assertEquals("Item A", auction.getItemName());
        assertEquals(100.0, auction.getCurrentHighestBid());
        assertEquals(10.0, auction.getMinIncrement());
        assertEquals(500.0, auction.getReservePrice());
        assertEquals(start, auction.getStartTime());
        assertEquals(end, auction.getEndTime());
        assertEquals("ACTIVE", auction.getStatus());
    }

    @Test
    void testAuctionNoArgsConstructorAndSetters() {
        Auction auction = new Auction();
        auction.setId(2L);
        auction.setStatus("CLOSED");

        assertEquals(2L, auction.getId());
        assertEquals("CLOSED", auction.getStatus());
    }
}